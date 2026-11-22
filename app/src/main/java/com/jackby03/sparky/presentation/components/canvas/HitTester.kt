package com.jackby03.sparky.presentation.components.canvas

import androidx.compose.ui.geometry.Offset
import com.jackby03.sparky.domain.model.circuit.Circuit
import com.jackby03.sparky.domain.model.circuit.Node
import com.jackby03.sparky.domain.model.component.CircuitComponent
import com.jackby03.sparky.domain.model.component.Ground
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private const val COMPONENT_HIT_RADIUS = 32f   // px — bounding radius for component body
private const val TERMINAL_HIT_RADIUS  = 22f   // px — snap radius to a terminal node
private const val HALF = 24f                    // half-size of component symbol (px)

/**
 * Translates screen-space tap/drag positions into circuit element hits,
 * accounting for the current canvas [zoom] and [offset].
 */
object HitTester {

    /** Converts a screen-space [Offset] to canvas (world) coordinates. */
    fun toCanvas(screen: Offset, zoom: Float, offset: Offset): Offset =
        Offset((screen.x - offset.x) / zoom, (screen.y - offset.y) / zoom)

    /**
     * Finds the topmost component whose hit-box contains [canvasPos].
     * Returns null if no component was tapped.
     */
    fun findComponent(canvasPos: Offset, circuit: Circuit): CircuitComponent? =
        circuit.components.lastOrNull { component ->
            distanceTo(canvasPos, Offset(component.x, component.y)) <= COMPONENT_HIT_RADIUS
        }

    /**
     * Finds the node closest to [canvasPos] within [TERMINAL_HIT_RADIUS].
     * Used for wire-drawing snap-to-terminal behavior.
     */
    fun findNearestNode(canvasPos: Offset, circuit: Circuit): Node? =
        circuit.nodes
            .map { node -> node to distanceTo(canvasPos, Offset(node.x, node.y)) }
            .filter { (_, dist) -> dist <= TERMINAL_HIT_RADIUS }
            .minByOrNull { (_, dist) -> dist }
            ?.first

    /**
     * Returns the two terminal positions of [component] in canvas coordinates,
     * taking its current [CircuitComponent.rotation] into account.
     */
    fun terminalPositions(component: CircuitComponent): Pair<Offset, Offset> {
        val center = Offset(component.x, component.y)
        val raw0: Offset
        val raw1: Offset

        when (component) {
            is Ground -> {
                raw0 = Offset(0f, -HALF)   // single wire terminal (top)
                raw1 = Offset(0f, 0f)      // ground reference point (center)
            }
            else -> {
                raw0 = Offset(-HALF, 0f)   // left / positive terminal
                raw1 = Offset(HALF,  0f)   // right / negative terminal
            }
        }

        return rotateAround(raw0, component.rotation, center) to
               rotateAround(raw1, component.rotation, center)
    }

    private fun rotateAround(localOffset: Offset, degrees: Float, pivot: Offset): Offset {
        if (degrees == 0f) return pivot + localOffset
        val rad = Math.toRadians(degrees.toDouble())
        val cos = cos(rad).toFloat()
        val sin = sin(rad).toFloat()
        val dx  = localOffset.x
        val dy  = localOffset.y
        return Offset(pivot.x + dx * cos - dy * sin, pivot.y + dx * sin + dy * cos)
    }

    private fun distanceTo(a: Offset, b: Offset): Float {
        val dx = a.x - b.x
        val dy = a.y - b.y
        return sqrt(dx * dx + dy * dy)
    }
}
