package com.jackby03.sparky.presentation.components.canvas

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.pow
import com.jackby03.sparky.domain.model.component.CircuitComponent
import com.jackby03.sparky.core.constants.AppConstants.Canvas as CanvasConst
import com.jackby03.sparky.core.extensions.clamp
import com.jackby03.sparky.core.utils.SnapGrid
import com.jackby03.sparky.domain.model.circuit.Circuit
import com.jackby03.sparky.domain.model.simulation.SimulationResult
import com.jackby03.sparky.presentation.screen.editor.WireDrawState
import com.jackby03.sparky.presentation.theme.SparkCanvasColors
import com.jackby03.sparky.presentation.theme.canvasColors

private const val SNAP_GUIDE_THRESHOLD = 8f

// Fling parameters
private const val FLING_MIN_VELOCITY = 200f     // px/s — minimum speed to trigger fling
private const val FLING_MAX_VELOCITY = 5_000f   // px/s — cap to prevent runaway
private const val FLING_FRICTION     = 0.94f    // per-frame decay at 60 fps (~40% / sec)

/**
 * The main circuit editing canvas.
 *
 * All gestures are handled in a single [awaitEachGesture] block so that tap,
 * component-drag, canvas-pan and pinch-to-zoom are mutually exclusive.
 * This eliminates the double-pan bug that occurs when multiple [pointerInput]
 * modifiers process the same pointer events simultaneously.
 */
@Composable
fun CircuitCanvas(
    circuit: Circuit,
    selectedComponentId: String?,
    wireMode: Boolean = false,
    wireDrawState: WireDrawState?,
    simulationResult: SimulationResult?,
    onComponentSelected: (String?) -> Unit,
    onComponentMoved: (String, Float, Float) -> Unit,
    onCanvasTransformChanged: (Float, Offset) -> Unit,
    onTapNode: (nodeId: String, canvasPos: Offset) -> Unit,
    onWireGhostMoved: (Offset) -> Unit,
    onSnapGuidesChanged: (guideX: Float?, guideY: Float?) -> Unit = { _, _ -> },
    snapGuideX: Float? = null,
    snapGuideY: Float? = null,
    modifier: Modifier = Modifier,
    initialZoom: Float = CanvasConst.DEFAULT_ZOOM,
    initialOffset: Offset = Offset.Zero,
    transformVersion: Int = 0
) {
    val colors = canvasColors
    val scope  = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    // rememberUpdatedState lets the pointerInput(Unit) block always see the
    // latest circuit/wireDrawState without restarting the gesture coroutine.
    // Using circuit as a pointerInput key used to cancel the drag mid-gesture
    // every time onComponentMoved updated the circuit state.
    val latestCircuit       = rememberUpdatedState(circuit)
    val latestWireMode      = rememberUpdatedState(wireMode)
    val latestWireDrawState = rememberUpdatedState(wireDrawState)

    // Zoom has two sources:
    // 1. Gestures (pinch) → must be immediate, no animation lag
    // 2. External (buttons, zoom-to-fit) → smooth spring animation
    // We keep a raw zoom value that gestures write directly, and an animated
    // value driven by external changes. The rendered zoom picks whichever
    // source is currently active.
    var zoomRaw by remember { mutableFloatStateOf(initialZoom) }
    var zoomAnimTarget by remember { mutableFloatStateOf(initialZoom) }
    val zoomAnim by animateFloatAsState(
        targetValue   = zoomAnimTarget,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label         = "canvasZoomAnim"
    )
    // During a pinch gesture zoomRaw != zoomAnimTarget, so we use zoomRaw.
    // When idle (or after external change) they converge and we use zoomAnim.
    var isGestureZooming by remember { mutableStateOf(false) }
    val zoom = if (isGestureZooming) zoomRaw else zoomAnim

    var offset by remember { mutableStateOf(initialOffset) }

    // Sync when zoom/offset is changed externally (zoom buttons, zoom-to-fit,
    // zoom-to-selection).  Keyed on transformVersion — a counter that only
    // increments for external changes — so gesture-reported echoes never
    // trigger this effect and cause the feedback-loop ghosting.
    LaunchedEffect(transformVersion) {
        if (transformVersion == 0) return@LaunchedEffect   // skip initial composition
        zoomRaw          = initialZoom
        zoomAnimTarget   = initialZoom
        isGestureZooming = false
        offset           = initialOffset
    }

    var draggingId       by remember { mutableStateOf<String?>(null) }
    // dragUnlocked: false until long-press fires; canvas pan is always true.
    // Stored outside awaitEachGesture so the longPressJob coroutine can write it.
    var dragUnlocked     by remember { mutableStateOf(false) }
    // 0f→1f while the long-press countdown is running; resets on release or unlock.
    var longPressProgress by remember { mutableFloatStateOf(0f) }
    // Animated scale: pops to 1.1 when drag unlocks, bounces back to settle.
    val dragScale by animateFloatAsState(
        targetValue   = if (dragUnlocked && draggingId != null) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessHigh
        ),
        label = "dragScale"
    )
    val velocityTracker = remember { VelocityTracker() }
    var flingJob     by remember { mutableStateOf<Job?>(null) }
    var longPressJob by remember { mutableStateOf<Job?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            // key = Unit so the gesture block never restarts due to circuit changes.
            .pointerInput(Unit) {
                val touchSlop   = viewConfiguration.touchSlop
                val longPressMs = viewConfiguration.longPressTimeoutMillis

                awaitEachGesture {
                    // ── 1. First pointer down ────────────────────────────────────
                    val firstDown = awaitFirstDown(requireUnconsumed = false)
                    longPressJob?.cancel()
                    longPressProgress = 0f
                    flingJob?.cancel()
                    velocityTracker.resetTracking()
                    velocityTracker.addPosition(firstDown.uptimeMillis, firstDown.position)

                    val startCanvasPos = HitTester.toCanvas(firstDown.position, zoom, offset)
                    val inWireMode = latestWireMode.value || latestWireDrawState.value != null
                    val hitComp = if (!inWireMode)
                        HitTester.findComponent(startCanvasPos, latestCircuit.value) else null
                    draggingId   = hitComp?.id
                    // Canvas pan: instantly unlocked. Component: requires long press.
                    // Wire mode: no pan, no drag — finger movement only updates ghost wire.
                    dragUnlocked = (hitComp == null && !inWireMode)

                    // Capture initial component position once.
                    // Cumulative delta avoids reading stale circuit state mid-drag.
                    val dragInitX    = hitComp?.x ?: 0f
                    val dragInitY    = hitComp?.y ?: 0f
                    val dragStartPos = firstDown.position

                    if (hitComp != null) {
                        longPressJob = scope.launch {
                            // Tick progress 0→1 frame-by-frame over longPressMs duration.
                            val durationMs     = longPressMs.toFloat()
                            var firstFrameNanos = -1L
                            while (true) {
                                val frameNanos = withFrameNanos { it }
                                if (firstFrameNanos < 0L) firstFrameNanos = frameNanos
                                val elapsedMs = (frameNanos - firstFrameNanos) / 1_000_000f
                                longPressProgress = (elapsedMs / durationMs).coerceIn(0f, 1f)
                                if (longPressProgress >= 1f) break
                            }
                            dragUnlocked = true
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            // Hold the completed ring briefly so the user sees it finish,
                            // then clear it — the glow takes over while dragging.
                            delay(150)
                            longPressProgress = 0f
                        }
                    }

                    var moved           = false
                    var twoFingerActive = false
                    var skipNextSingle  = false
                    var prevCentroid    = firstDown.position
                    var prevSpan        = 0f
                    var lastFingerPos   = firstDown.position

                    // ── 2. Event loop ────────────────────────────────────────────
                    gestures@ while (true) {
                        val event  = awaitPointerEvent()
                        val active = event.changes.filter { it.pressed }

                        if (active.isEmpty()) break@gestures

                        if (active.size >= 2) {
                            // ── Two-finger: pinch-to-zoom + pan ───────────────────
                            draggingId     = null
                            skipNextSingle = false

                            val p1       = active[0].position
                            val p2       = active[1].position
                            val centroid = (p1 + p2) / 2f
                            val span     = (p2 - p1).getDistance()

                            if (!twoFingerActive) {
                                twoFingerActive  = true
                                isGestureZooming = true
                                prevCentroid     = centroid
                                prevSpan         = span
                                velocityTracker.resetTracking()
                            } else if (prevSpan > 0f) {
                                val zoomRatio     = span / prevSpan
                                val effectiveZoom = if (kotlin.math.abs(zoomRatio - 1f) < 0.02f) 1f
                                                    else zoomRatio
                                val newZoom = (zoomRaw * effectiveZoom)
                                    .clamp(CanvasConst.MIN_ZOOM, CanvasConst.MAX_ZOOM)
                                val pan = centroid - prevCentroid
                                offset  = centroid + (offset - centroid) * (newZoom / zoomRaw) + pan
                                zoomRaw = newZoom
                                onCanvasTransformChanged(newZoom, offset)
                            }
                            prevCentroid = centroid
                            prevSpan     = span
                            active.forEach { it.consume() }

                        } else {
                            // ── Single finger ─────────────────────────────────────
                            if (twoFingerActive) {
                                twoFingerActive = false
                                skipNextSingle  = true
                                velocityTracker.resetTracking()
                                active.forEach { it.consume() }
                                continue@gestures
                            }
                            if (skipNextSingle) {
                                skipNextSingle = false
                                active.forEach { it.consume() }
                                continue@gestures
                            }

                            val change     = active[0]
                            lastFingerPos  = change.position
                            val dragAmount = change.position - change.previousPosition

                            // If finger moves before long-press fires, cancel it and fall through
                            // to canvas pan (draggingId cleared → pan branch handles movement).
                            if (dragAmount.getDistance() > touchSlop && longPressJob?.isActive == true) {
                                longPressJob?.cancel()
                                longPressJob = null
                                longPressProgress = 0f
                                draggingId   = null   // degrade to canvas pan
                                dragUnlocked = true
                            }

                            when {
                                latestWireDrawState.value != null -> {
                                    val cp = HitTester.toCanvas(change.position, zoom, offset)
                                    onWireGhostMoved(cp)
                                }
                                draggingId != null && dragUnlocked -> {
                                    // Drag component: cumulative delta from initial position.
                                    // Keeps the component under the finger at any zoom level.
                                    val totalDelta = change.position - dragStartPos
                                    val newX = dragInitX + totalDelta.x / zoom
                                    val newY = dragInitY + totalDelta.y / zoom
                                    val (sx, sy) = SnapGrid.snapPosition(newX, newY)
                                    onComponentMoved(draggingId!!, sx, sy)
                                    val others = latestCircuit.value.components
                                        .filter { it.id != draggingId }
                                    onSnapGuidesChanged(
                                        others.firstOrNull {
                                            kotlin.math.abs(it.x - sx) <= SNAP_GUIDE_THRESHOLD
                                        }?.x,
                                        others.firstOrNull {
                                            kotlin.math.abs(it.y - sy) <= SNAP_GUIDE_THRESHOLD
                                        }?.y
                                    )
                                    moved = true
                                }
                                draggingId == null -> {
                                    // Canvas pan
                                    velocityTracker.addPosition(change.uptimeMillis, change.position)
                                    offset += dragAmount
                                    onCanvasTransformChanged(zoom, offset)
                                    moved = true
                                }
                                // draggingId != null && !moved: waiting for touchSlop
                            }
                            change.consume()
                        }
                    }

                    // ── 3. Gesture ended ─────────────────────────────────────────
                    longPressJob?.cancel()
                    longPressJob     = null
                    longPressProgress = 0f
                    val wasComponentDrag = draggingId != null
                    draggingId   = null
                    dragUnlocked = false
                    onSnapGuidesChanged(null, null)

                    // After a pinch ends, sync the animation target to the
                    // current raw zoom so subsequent animated changes (buttons)
                    // start from the correct value without a visible jump.
                    if (isGestureZooming) {
                        zoomAnimTarget   = zoomRaw
                        isGestureZooming = false
                    }

                    when {
                        latestWireDrawState.value != null && !twoFingerActive -> {
                            // ── Wire drawing active: complete wire on lift ──────
                            val endCanvasPos = HitTester.toCanvas(lastFingerPos, zoom, offset)
                            val node = HitTester.findNearestNode(endCanvasPos, latestCircuit.value)
                            if (node != null) onTapNode(node.id, endCanvasPos)
                        }
                        latestWireMode.value && latestWireDrawState.value == null
                                && !moved && !twoFingerActive -> {
                            // ── Wire mode (FAB active, no wire started yet): tap to
                            //    pick the first terminal node ────────────────────
                            val node = HitTester.findNearestNode(startCanvasPos, latestCircuit.value)
                            if (node != null) onTapNode(node.id, startCanvasPos)
                        }
                        !moved && !twoFingerActive -> {
                            // ── Normal tap (wire mode off) ─────────────────────
                            val hit = HitTester.findComponent(startCanvasPos, latestCircuit.value)
                            if (hit != null) {
                                onComponentSelected(hit.id)
                            } else {
                                onComponentSelected(null)
                            }
                        }
                        !wasComponentDrag && !twoFingerActive &&
                                !latestWireMode.value -> {
                            // ── Fling ─────────────────────────────────────────────
                            val velocity = velocityTracker.calculateVelocity()
                            val vxStart  = velocity.x.coerceIn(-FLING_MAX_VELOCITY, FLING_MAX_VELOCITY)
                            val vyStart  = velocity.y.coerceIn(-FLING_MAX_VELOCITY, FLING_MAX_VELOCITY)
                            if (vxStart.absoluteValue > FLING_MIN_VELOCITY ||
                                vyStart.absoluteValue > FLING_MIN_VELOCITY) {
                                flingJob = scope.launch {
                                    var vx = vxStart
                                    var vy = vyStart
                                    var prevNanos = 0L
                                    while (vx.absoluteValue > 0.5f || vy.absoluteValue > 0.5f) {
                                        val nanos = withFrameNanos { it }
                                        if (prevNanos != 0L) {
                                            val dtSec  = (nanos - prevNanos) / 1_000_000_000f
                                            val factor = FLING_FRICTION.toDouble()
                                                .pow(dtSec.toDouble() * 60.0).toFloat()
                                            vx *= factor
                                            vy *= factor
                                            offset = Offset(
                                                offset.x + vx * dtSec,
                                                offset.y + vy * dtSec
                                            )
                                            onCanvasTransformChanged(zoom, offset)
                                        }
                                        prevNanos = nanos
                                    }
                                }
                            }
                        }
                    }
                }
            }
    ) {
        // ── Grid ──────────────────────────────────────────────────────────────────
        GridOverlay(
            zoom      = zoom,
            offsetX   = offset.x,
            offsetY   = offset.y,
            gridColor = colors.grid,
            modifier  = Modifier.fillMaxSize()
        )

        // ── Single unified circuit layer ─────────────────────────────────────────
        // Everything is drawn in ONE Canvas with ONE graphicsLayer so that all
        // elements move atomically during pan/zoom — no inter-frame ghosting.
        CircuitLayer(
            circuit              = circuit,
            simulationResult     = simulationResult,
            selectedComponentId  = selectedComponentId,
            wireDrawState        = wireDrawState,
            snapGuideX           = snapGuideX,
            snapGuideY           = snapGuideY,
            zoom                 = zoom,
            offset               = offset,
            colors               = colors,
            longPressComponentId = draggingId,
            longPressProgress    = longPressProgress,
            isDragUnlocked       = dragUnlocked,
            dragScale            = dragScale,
        )
    }
}

// ── Unified circuit layer ────────────────────────────────────────────────────
//
// All circuit elements (wires, components, selection, guides, ghost wire) are
// drawn in a SINGLE Canvas with a SINGLE graphicsLayer.  This guarantees every
// element moves atomically during pan/zoom — no inter-frame ghosting.

@Composable
private fun CircuitLayer(
    circuit: Circuit,
    simulationResult: SimulationResult?,
    selectedComponentId: String?,
    wireDrawState: WireDrawState?,
    snapGuideX: Float?,
    snapGuideY: Float?,
    zoom: Float,
    offset: Offset,
    colors: SparkCanvasColors,
    longPressComponentId: String? = null,
    longPressProgress: Float = 0f,
    isDragUnlocked: Boolean = false,
    dragScale: Float = 1f,
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = zoom; scaleY = zoom
                translationX = offset.x; translationY = offset.y
                transformOrigin = TransformOrigin(0f, 0f)
            }
    ) {
        // Viewport bounds in canvas coords
        val vpLeft   = -offset.x / zoom
        val vpTop    = -offset.y / zoom
        val vpRight  = vpLeft + size.width  / zoom
        val vpBottom = vpTop  + size.height / zoom
        val cullPad  = 64f

        // ── 1. Wires (bottom layer) ────────────────────────────────────────────
        circuit.wires.forEach { wire ->
            val startNode    = circuit.nodeById(wire.startNodeId)
            val endNode      = circuit.nodeById(wire.endNodeId)
            val voltageLevel = simulationResult?.let { result ->
                val startV = result.nodeVoltages[wire.startNodeId] ?: 0.0
                val endV   = result.nodeVoltages[wire.endNodeId]   ?: 0.0
                (startV + endV) / 2.0
            }
            drawWire(wire, { id -> circuit.nodeById(id) }, colors.wire, voltageLevel)

            if (simulationResult != null && startNode != null && endNode != null) {
                val startV = simulationResult.nodeVoltages[wire.startNodeId] ?: 0.0
                val endV   = simulationResult.nodeVoltages[wire.endNodeId]   ?: 0.0
                val deltaV = startV - endV
                if (deltaV != 0.0) {
                    drawCurrentArrow(
                        startNode      = Offset(startNode.x, startNode.y),
                        endNode        = Offset(endNode.x,   endNode.y),
                        currentAmperes = deltaV,
                        color          = colors.simLabel
                    )
                }
            }
        }

        // Highlight wires connected to the selected component
        selectedComponentId?.let { id ->
            val component = circuit.components.find { it.id == id }
            if (component != null) {
                val terminalIds = component.terminalNodeIds.toSet()
                circuit.wires
                    .filter { it.startNodeId in terminalIds || it.endNodeId in terminalIds }
                    .forEach { wire ->
                        drawWire(
                            wire        = wire,
                            nodeById    = { nId -> circuit.nodeById(nId) },
                            wireColor   = colors.wireActive,
                            strokeWidth = 5f
                        )
                    }
            }
        }

        // ── 2. Components ──────────────────────────────────────────────────────
        val labelOffsets = computeLabelOffsets(circuit.components)

        circuit.components.forEach { component ->
            val isSelected = component.id == selectedComponentId
            val isBeingDragged = component.id == longPressComponentId

            // Long-press glow (behind the component)
            if (isBeingDragged && isDragUnlocked) {
                val center = Offset(component.x, component.y)
                val r = 54f
                drawCircle(colors.wireActive.copy(alpha = 0.10f), r * 1.5f, center)
                drawCircle(colors.wireActive.copy(alpha = 0.45f), r, center,
                    style = Stroke(width = 1.5f / zoom))
            }

            // Component body + terminals + label
            if (isBeingDragged && dragScale != 1f) {
                val pivot = Offset(component.x, component.y)
                withTransform({ scale(dragScale, dragScale, pivot) }) {
                    drawComponent(component, colors.component, isSelected, colors.wireActive)
                    drawTerminalDots(component, colors.terminal)
                    drawComponentLabel(component, colors.simLabel.copy(alpha = 0.75f),
                        labelOffsets[component.id] ?: 0f)
                }
            } else {
                drawComponent(component, colors.component, isSelected, colors.wireActive)
                drawTerminalDots(component, colors.terminal)
                drawComponentLabel(component, colors.simLabel.copy(alpha = 0.75f),
                    labelOffsets[component.id] ?: 0f)
            }

            // Long-press progress ring (on top of component)
            if (isBeingDragged && !isDragUnlocked && longPressProgress > 0f) {
                val center = Offset(component.x, component.y)
                val r  = 54f
                val sw = 3f / zoom
                drawCircle(colors.wireActive.copy(alpha = 0.18f), r, center,
                    style = Stroke(width = sw))
                drawArc(
                    color      = colors.wireActive.copy(alpha = 0.90f),
                    startAngle = -90f,
                    sweepAngle = longPressProgress * 360f,
                    useCenter  = false,
                    topLeft    = Offset(center.x - r, center.y - r),
                    size       = Size(r * 2f, r * 2f),
                    style      = Stroke(width = sw, cap = StrokeCap.Round)
                )
            }
        }

        // ── 3. Simulation voltage labels ───────────────────────────────────────
        if (simulationResult != null) {
            circuit.nodes.forEach { node ->
                val voltage = simulationResult.nodeVoltages[node.id] ?: return@forEach
                drawVoltageLabel(Offset(node.x, node.y), voltage, colors.simLabel)
            }
        }

        // ── 4. Ghost wire ──────────────────────────────────────────────────────
        wireDrawState?.let { state ->
            drawGhostWire(state.startPos, state.currentPos, colors.wireActive)
        }

        // ── 5. Snap alignment guides ───────────────────────────────────────────
        val guideColor = colors.wireActive.copy(alpha = 0.5f)
        snapGuideX?.let { x ->
            drawLine(guideColor, Offset(x, vpTop - cullPad), Offset(x, vpBottom + cullPad),
                strokeWidth = 1f / zoom,
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                    floatArrayOf(8f, 6f), 0f))
        }
        snapGuideY?.let { y ->
            drawLine(guideColor, Offset(vpLeft - cullPad, y), Offset(vpRight + cullPad, y),
                strokeWidth = 1f / zoom,
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                    floatArrayOf(8f, 6f), 0f))
        }
    }
}

// ── Label collision detection ─────────────────────────────────────────────────

/**
 * Computes a per-component vertical offset (in canvas pixels) so that component
 * labels never visually overlap each other.
 */
private fun computeLabelOffsets(components: List<CircuitComponent>): Map<String, Float> {
    if (components.size < 2) return emptyMap()

    val offsets = mutableMapOf<String, Float>()
    val sorted  = components.sortedWith(compareBy({ it.x }, { it.y }))

    for (i in sorted.indices) {
        val a  = sorted[i]
        val aY = a.y + 42f + (offsets[a.id] ?: 0f)
        for (j in i + 1 until sorted.size) {
            val b = sorted[j]
            if (b.x - a.x > LABEL_X_THRESHOLD) break
            val bY = b.y + 42f + (offsets[b.id] ?: 0f)
            val dx = kotlin.math.abs(a.x - b.x)
            val dy = kotlin.math.abs(aY - bY)
            if (dx < LABEL_X_THRESHOLD && dy < LABEL_Y_THRESHOLD) {
                offsets[b.id] = (offsets[b.id] ?: 0f) + LABEL_PUSH
            }
        }
    }
    return offsets
}

private const val LABEL_X_THRESHOLD = 56f
private const val LABEL_Y_THRESHOLD = 22f
private const val LABEL_PUSH        = 24f
