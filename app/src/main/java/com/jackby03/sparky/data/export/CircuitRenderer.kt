package com.jackby03.sparky.data.export

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.jackby03.sparky.domain.model.circuit.Circuit
import com.jackby03.sparky.domain.model.component.Capacitor
import com.jackby03.sparky.domain.model.component.CircuitComponent
import com.jackby03.sparky.domain.model.component.CurrentSource
import com.jackby03.sparky.domain.model.component.Diode
import com.jackby03.sparky.domain.model.component.Ground
import com.jackby03.sparky.domain.model.component.Inductor
import com.jackby03.sparky.domain.model.component.Resistor
import com.jackby03.sparky.domain.model.component.VoltageSource

/**
 * Shared rendering logic for all export formats (PDF, PNG).
 *
 * Uses only [android.graphics] primitives so it can draw onto any
 * [android.graphics.Canvas] — bitmap-backed for images, PDF-backed for documents.
 *
 * Coordinate system: 1 px = 1 canvas unit. The caller is responsible for
 * applying any DPI-based scaling transform before calling [render].
 */
object CircuitRenderer {

    private const val HALF = 24f
    private const val WIRE_STROKE = 3f
    private const val COMP_STROKE = 2.5f
    private const val LABEL_SIZE  = 18f

    private val wirePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color     = Color.CYAN
        strokeWidth = WIRE_STROKE
        style     = Paint.Style.STROKE
        strokeCap = android.graphics.Paint.Cap.ROUND
    }

    private val componentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color       = Color.WHITE
        strokeWidth = COMP_STROKE
        style       = Paint.Style.STROKE
        strokeCap   = android.graphics.Paint.Cap.ROUND
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color    = Color.LTGRAY
        textSize = LABEL_SIZE
        textAlign = android.graphics.Paint.Align.CENTER
    }

    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color    = Color.WHITE
        textSize = 28f
        isFakeBoldText = true
    }

    private val bgPaint = Paint().apply { color = Color.BLACK }

    /**
     * Renders [circuit] onto [canvas] within the bounds [[0, 0, width, height]].
     *
     * @param title  Optional title drawn above the schematic.
     */
    fun render(canvas: Canvas, circuit: Circuit, width: Int, height: Int, title: String? = null) {
        // Background
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        val offsetY = if (title != null) {
            canvas.drawText(title, 24f, 44f, titlePaint)
            60f
        } else 20f

        // Compute bounding box of circuit to auto-fit
        val (scaleX, scaleY, dx, dy) = computeTransform(circuit, width, height - offsetY.toInt())

        canvas.save()
        canvas.translate(dx, dy + offsetY)

        // Draw wires
        circuit.wires.forEach { wire ->
            val startNode = circuit.nodeById(wire.startNodeId) ?: return@forEach
            val endNode   = circuit.nodeById(wire.endNodeId)   ?: return@forEach
            val path = Path().apply {
                moveTo(startNode.x * scaleX, startNode.y * scaleY)
                wire.waypoints.forEach { wp -> lineTo(wp.x * scaleX, wp.y * scaleY) }
                lineTo(endNode.x * scaleX, endNode.y * scaleY)
            }
            canvas.drawPath(path, wirePaint)
        }

        // Draw components
        circuit.components.forEach { component ->
            canvas.save()
            val cx = component.x * scaleX
            val cy = component.y * scaleY
            canvas.rotate(component.rotation, cx, cy)
            drawComponent(canvas, component, cx, cy)
            // Label below component
            canvas.drawText(component.label, cx, cy + HALF + LABEL_SIZE + 4f, labelPaint)
            canvas.restore()
        }

        canvas.restore()
    }

    private fun drawComponent(canvas: Canvas, component: CircuitComponent, cx: Float, cy: Float) {
        val p = componentPaint
        when (component) {
            is Resistor -> {
                canvas.drawLine(cx - HALF, cy, cx - HALF / 2, cy, p)
                canvas.drawLine(cx + HALF / 2, cy, cx + HALF, cy, p)
                canvas.drawRect(RectF(cx - HALF / 2, cy - 8f, cx + HALF / 2, cy + 8f), p)
            }
            is Capacitor -> {
                canvas.drawLine(cx - HALF, cy, cx - 6f, cy, p)
                canvas.drawLine(cx + 6f, cy, cx + HALF, cy, p)
                canvas.drawLine(cx - 6f, cy - 16f, cx - 6f, cy + 16f, p)
                canvas.drawLine(cx + 6f, cy - 16f, cx + 6f, cy + 16f, p)
            }
            is Inductor -> {
                canvas.drawLine(cx - HALF, cy, cx - HALF / 2, cy, p)
                canvas.drawLine(cx + HALF / 2, cy, cx + HALF, cy, p)
                canvas.drawArc(RectF(cx - HALF / 2, cy - 8f, cx - HALF / 2 + 16f, cy + 8f), 0f, 180f, false, p)
                canvas.drawArc(RectF(cx - HALF / 2 + 16f, cy - 8f, cx - HALF / 2 + 32f, cy + 8f), 0f, 180f, false, p)
            }
            is VoltageSource -> {
                canvas.drawLine(cx - HALF, cy, cx - 16f, cy, p)
                canvas.drawLine(cx + 16f, cy, cx + HALF, cy, p)
                canvas.drawCircle(cx, cy, 16f, p)
                canvas.drawLine(cx - 8f, cy, cx + 8f, cy, p)
                canvas.drawLine(cx, cy - 8f, cx, cy + 8f, p)
            }
            is CurrentSource -> {
                canvas.drawLine(cx - HALF, cy, cx - 16f, cy, p)
                canvas.drawLine(cx + 16f, cy, cx + HALF, cy, p)
                canvas.drawCircle(cx, cy, 16f, p)
                canvas.drawLine(cx - 6f, cy, cx + 6f, cy, p)
            }
            is Diode -> {
                val path = Path().apply {
                    moveTo(cx - 12f, cy - 12f)
                    lineTo(cx + 12f, cy)
                    lineTo(cx - 12f, cy + 12f)
                    close()
                }
                canvas.drawPath(path, p)
                canvas.drawLine(cx + 12f, cy - 12f, cx + 12f, cy + 12f, p)
                canvas.drawLine(cx - HALF, cy, cx - 12f, cy, p)
                canvas.drawLine(cx + 12f, cy, cx + HALF, cy, p)
            }
            is com.jackby03.sparky.domain.model.component.SymbolComponent ->
                drawGlyph(canvas, com.jackby03.sparky.core.symbols.SymbolLibrary.glyphFor(component.componentType, component.symbol), cx, cy, p)
            is Ground -> {
                canvas.drawLine(cx, cy - HALF, cx, cy, p)
                canvas.drawLine(cx - 16f, cy, cx + 16f, cy, p)
                canvas.drawLine(cx - 10f, cy + 6f, cx + 10f, cy + 6f, p)
                canvas.drawLine(cx - 4f, cy + 12f, cx + 4f, cy + 12f, p)
            }
        }
    }

    /** Draws a catalog symbol glyph; mirrors the on-screen renderer so exports match the editor. */
    private fun drawGlyph(canvas: Canvas, glyph: com.jackby03.sparky.core.symbols.Glyph, cx: Float, cy: Float, p: Paint) {
        val fillPaint = Paint(p).apply { style = Paint.Style.FILL }
        for (prim in glyph.prims) {
            when (prim) {
                is com.jackby03.sparky.core.symbols.Prim.Line -> canvas.drawLine(cx + prim.x1, cy + prim.y1, cx + prim.x2, cy + prim.y2, p)
                is com.jackby03.sparky.core.symbols.Prim.Circle -> canvas.drawCircle(cx + prim.cx, cy + prim.cy, prim.r, p)
                is com.jackby03.sparky.core.symbols.Prim.Dot -> canvas.drawCircle(cx + prim.cx, cy + prim.cy, prim.r, fillPaint)
                is com.jackby03.sparky.core.symbols.Prim.Arc -> canvas.drawArc(
                    RectF(cx + prim.cx - prim.r, cy + prim.cy - prim.r, cx + prim.cx + prim.r, cy + prim.cy + prim.r),
                    prim.start, prim.sweep, false, p
                )
                is com.jackby03.sparky.core.symbols.Prim.Rect -> canvas.drawRect(RectF(cx + prim.l, cy + prim.t, cx + prim.r, cy + prim.b), p)
                is com.jackby03.sparky.core.symbols.Prim.Poly -> {
                    val path = Path()
                    path.moveTo(cx + prim.pts[0], cy + prim.pts[1])
                    var i = 2
                    while (i + 1 < prim.pts.size) { path.lineTo(cx + prim.pts[i], cy + prim.pts[i + 1]); i += 2 }
                    path.close()
                    canvas.drawPath(path, if (prim.filled) fillPaint else p)
                }
                is com.jackby03.sparky.core.symbols.Prim.Polyline -> {
                    val path = Path()
                    path.moveTo(cx + prim.pts[0], cy + prim.pts[1])
                    var i = 2
                    while (i + 1 < prim.pts.size) { path.lineTo(cx + prim.pts[i], cy + prim.pts[i + 1]); i += 2 }
                    canvas.drawPath(path, p)
                }
                is com.jackby03.sparky.core.symbols.Prim.Label -> {
                    val tp = Paint(fillPaint).apply {
                        textSize = prim.size
                        textAlign = Paint.Align.CENTER
                        typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD)
                    }
                    canvas.drawText(prim.text, cx + prim.x, cy + prim.y, tp)
                }
            }
        }
    }

    /** Returns (scaleX, scaleY, translateX, translateY) to fit circuit into the target area. */
    private fun computeTransform(circuit: Circuit, targetW: Int, targetH: Int): List<Float> {
        if (circuit.components.isEmpty()) return listOf(1f, 1f, 0f, 0f)

        val minX = circuit.components.minOf { it.x } - HALF
        val minY = circuit.components.minOf { it.y } - HALF
        val maxX = circuit.components.maxOf { it.x } + HALF
        val maxY = circuit.components.maxOf { it.y } + HALF

        val rangeX = maxX - minX
        val rangeY = maxY - minY
        if (rangeX <= 0 || rangeY <= 0) return listOf(1f, 1f, 20f, 20f)

        val padding = 40f
        val scale = minOf(
            (targetW - padding * 2) / rangeX,
            (targetH - padding * 2) / rangeY
        ).coerceIn(0.1f, 4f)

        val scaledW = rangeX * scale
        val scaledH = rangeY * scale
        val tx = (targetW - scaledW) / 2f - minX * scale
        val ty = (targetH - scaledH) / 2f - minY * scale

        return listOf(scale, scale, tx, ty)
    }
}
