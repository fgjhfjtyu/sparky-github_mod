package com.jackby03.sparky.presentation.components.canvas

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.jackby03.sparky.core.symbols.Glyph
import com.jackby03.sparky.core.symbols.Prim
import com.jackby03.sparky.core.symbols.SymbolLibrary
import com.jackby03.sparky.core.utils.ValueParser
import com.jackby03.sparky.domain.model.component.Capacitor
import com.jackby03.sparky.domain.model.component.CircuitComponent
import com.jackby03.sparky.domain.model.component.CurrentSource
import com.jackby03.sparky.domain.model.component.Diode
import com.jackby03.sparky.domain.model.component.Ground
import com.jackby03.sparky.domain.model.component.Inductor
import com.jackby03.sparky.domain.model.component.Resistor
import com.jackby03.sparky.domain.model.component.VoltageSource
import com.jackby03.sparky.domain.model.component.SymbolComponent

private const val HALF = 24f
private const val STROKE = 2.5f

fun DrawScope.drawComponent(
    component: CircuitComponent,
    componentColor: Color,
    isSelected: Boolean,
    selectedColor: Color
) {
    val color = if (isSelected) selectedColor else componentColor
    val center = Offset(component.x, component.y)

    rotate(degrees = component.rotation, pivot = center) {
        when (component) {
            is Resistor      -> drawResistor(center, color)
            is Capacitor     -> drawCapacitor(center, color)
            is Inductor      -> drawInductor(center, color)
            is VoltageSource -> drawVoltageSource(center, color)
            is CurrentSource -> drawCurrentSource(center, color)
            is Diode         -> drawDiode(center, color)
            is Ground        -> drawGround(center, color)
            is SymbolComponent -> drawGlyph(SymbolLibrary.glyphFor(component.componentType, component.symbol), center, color)
        }
    }
}

private fun DrawScope.drawResistor(center: Offset, color: Color) {
    val stroke = Stroke(width = STROKE, cap = StrokeCap.Round)
    // Lead wires
    drawLine(color, Offset(center.x - HALF, center.y), Offset(center.x - HALF / 2, center.y), STROKE)
    drawLine(color, Offset(center.x + HALF / 2, center.y), Offset(center.x + HALF, center.y), STROKE)
    // Rectangle body
    drawRect(
        color = color,
        topLeft = Offset(center.x - HALF / 2, center.y - 8f),
        size = Size(HALF.toFloat(), 16f),
        style = stroke
    )
}

private fun DrawScope.drawCapacitor(center: Offset, color: Color) {
    val stroke = Stroke(width = STROKE, cap = StrokeCap.Round)
    drawLine(color, Offset(center.x - HALF, center.y), Offset(center.x - 6f, center.y), STROKE)
    drawLine(color, Offset(center.x + 6f, center.y), Offset(center.x + HALF, center.y), STROKE)
    drawLine(color, Offset(center.x - 6f, center.y - 16f), Offset(center.x - 6f, center.y + 16f), STROKE)
    drawLine(color, Offset(center.x + 6f, center.y - 16f), Offset(center.x + 6f, center.y + 16f), STROKE)
}

private fun DrawScope.drawInductor(center: Offset, color: Color) {
    drawLine(color, Offset(center.x - HALF, center.y), Offset(center.x - HALF / 2, center.y), STROKE)
    drawLine(color, Offset(center.x + HALF / 2, center.y), Offset(center.x + HALF, center.y), STROKE)
    drawArc(color, startAngle = 0f, sweepAngle = 180f, useCenter = false,
        topLeft = Offset(center.x - HALF / 2, center.y - 8f), size = Size(16f, 16f),
        style = Stroke(STROKE))
    drawArc(color, startAngle = 0f, sweepAngle = 180f, useCenter = false,
        topLeft = Offset(center.x - HALF / 2 + 16f, center.y - 8f), size = Size(16f, 16f),
        style = Stroke(STROKE))
}

private fun DrawScope.drawVoltageSource(center: Offset, color: Color) {
    val stroke = Stroke(width = STROKE)
    drawLine(color, Offset(center.x - HALF, center.y), Offset(center.x - 16f, center.y), STROKE)
    drawLine(color, Offset(center.x + 16f, center.y), Offset(center.x + HALF, center.y), STROKE)
    drawCircle(color = color, radius = 16f, center = center, style = stroke)
    drawLine(color, Offset(center.x - 8f, center.y), Offset(center.x + 8f, center.y), STROKE)
    drawLine(color, Offset(center.x, center.y - 8f), Offset(center.x, center.y + 8f), STROKE)
}

private fun DrawScope.drawCurrentSource(center: Offset, color: Color) {
    drawCircle(color = color, radius = 16f, center = center, style = Stroke(STROKE))
    drawLine(color, Offset(center.x - HALF, center.y), Offset(center.x - 16f, center.y), STROKE)
    drawLine(color, Offset(center.x + 16f, center.y), Offset(center.x + HALF, center.y), STROKE)
    drawLine(color, Offset(center.x - 6f, center.y), Offset(center.x + 6f, center.y), STROKE)
}

private fun DrawScope.drawDiode(center: Offset, color: Color) {
    val path = androidx.compose.ui.graphics.Path().apply {
        moveTo(center.x - 12f, center.y - 12f)
        lineTo(center.x + 12f, center.y)
        lineTo(center.x - 12f, center.y + 12f)
        close()
    }
    drawPath(path, color, style = Stroke(STROKE))
    drawLine(color, Offset(center.x + 12f, center.y - 12f), Offset(center.x + 12f, center.y + 12f), STROKE)
    drawLine(color, Offset(center.x - HALF, center.y), Offset(center.x - 12f, center.y), STROKE)
    drawLine(color, Offset(center.x + 12f, center.y), Offset(center.x + HALF, center.y), STROKE)
}

/**
 * Draws a catalog symbol from its vector [glyph]. Glyph coordinates are relative to the
 * component centre, so the same definition also drives the PDF/PNG/SVG exporter.
 */
private fun DrawScope.drawGlyph(glyph: Glyph, center: Offset, color: Color) {
    val stroke = Stroke(width = STROKE, cap = StrokeCap.Round, join = StrokeJoin.Round)
    fun pt(x: Float, y: Float) = Offset(center.x + x, center.y + y)

    for (prim in glyph.prims) {
        when (prim) {
            is Prim.Line -> drawLine(color, pt(prim.x1, prim.y1), pt(prim.x2, prim.y2), STROKE, StrokeCap.Round)
            is Prim.Circle -> drawCircle(color, prim.r, pt(prim.cx, prim.cy), style = stroke)
            is Prim.Dot -> drawCircle(color, prim.r, pt(prim.cx, prim.cy))
            is Prim.Arc -> drawArc(
                color = color, startAngle = prim.start, sweepAngle = prim.sweep, useCenter = false,
                topLeft = pt(prim.cx - prim.r, prim.cy - prim.r),
                size = Size(prim.r * 2f, prim.r * 2f), style = stroke
            )
            is Prim.Rect -> drawRect(
                color, pt(prim.l, prim.t), Size(prim.r - prim.l, prim.b - prim.t), style = stroke
            )
            is Prim.Poly -> drawPath(
                Path().apply {
                    moveTo(center.x + prim.pts[0], center.y + prim.pts[1])
                    var i = 2
                    while (i + 1 < prim.pts.size) { lineTo(center.x + prim.pts[i], center.y + prim.pts[i + 1]); i += 2 }
                    close()
                },
                color, style = if (prim.filled) Fill else stroke
            )
            is Prim.Polyline -> drawPath(
                Path().apply {
                    moveTo(center.x + prim.pts[0], center.y + prim.pts[1])
                    var i = 2
                    while (i + 1 < prim.pts.size) { lineTo(center.x + prim.pts[i], center.y + prim.pts[i + 1]); i += 2 }
                },
                color, style = stroke
            )
            is Prim.Label -> drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply {
                    this.color = color.toArgb()
                    textSize = prim.size
                    isAntiAlias = true
                    textAlign = android.graphics.Paint.Align.CENTER
                    typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD)
                }
                canvas.nativeCanvas.drawText(prim.text, center.x + prim.x, center.y + prim.y, paint)
            }
        }
    }
}

private fun DrawScope.drawGround(center: Offset, color: Color) {
    drawLine(color, Offset(center.x, center.y - HALF), Offset(center.x, center.y), STROKE)
    drawLine(color, Offset(center.x - 16f, center.y), Offset(center.x + 16f, center.y), STROKE)
    drawLine(color, Offset(center.x - 10f, center.y + 6f), Offset(center.x + 10f, center.y + 6f), STROKE)
    drawLine(color, Offset(center.x - 4f, center.y + 12f), Offset(center.x + 4f, center.y + 12f), STROKE)
}

// ── Overlay helpers ───────────────────────────────────────────────────────────

/**
 * Draws small dots at each terminal of [component] to hint at connection points.
 */
fun DrawScope.drawTerminalDots(component: CircuitComponent, color: Color) {
    val (t0, t1) = HitTester.terminalPositions(component)
    drawCircle(color = color.copy(alpha = 0.6f), radius = 4f, center = t0)
    if (component !is Ground) {
        drawCircle(color = color.copy(alpha = 0.6f), radius = 4f, center = t1)
    }
}

/**
 * Draws the component label (e.g. "R1", "V2") below the component symbol.
 *
 * @param extraYOffset  Additional downward shift in canvas pixels — used by label
 *                      collision detection to avoid overlapping nearby labels.
 */
fun DrawScope.drawComponentLabel(component: CircuitComponent, color: Color, extraYOffset: Float = 0f) {
    if (component is Ground) return   // GND uses its symbol; no extra label needed
    val labelX = component.x
    val labelY  = component.y + HALF + 18f + extraYOffset
    drawIntoCanvas { canvas ->
        val paint = android.graphics.Paint().apply {
            this.color  = color.toArgb()
            textSize    = 20f
            isAntiAlias = true
            textAlign   = android.graphics.Paint.Align.CENTER
        }
        canvas.nativeCanvas.drawText(component.label, labelX, labelY, paint)
    }
}

/**
 * Draws a voltage annotation badge above [nodePos].
 */
fun DrawScope.drawVoltageLabel(nodePos: Offset, voltage: Double, color: Color) {
    val labelText = ValueParser.format(voltage, "V")
    drawIntoCanvas { canvas ->
        val paint = android.graphics.Paint().apply {
            this.color  = color.toArgb()
            textSize    = 24f
            isAntiAlias = true
            textAlign   = android.graphics.Paint.Align.CENTER
        }
        canvas.nativeCanvas.drawText(labelText, nodePos.x, nodePos.y - 30f, paint)
    }
}
