package com.jackby03.sparky.presentation.components.canvas

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import com.jackby03.sparky.domain.model.circuit.Node
import com.jackby03.sparky.domain.model.circuit.Wire
import com.jackby03.sparky.presentation.theme.NeonCyan
import com.jackby03.sparky.presentation.theme.NeonOrange

private const val WIRE_STROKE = 3f
private const val GHOST_STROKE = 2f

/**
 * Draws a [wire] on the canvas, optionally color-coding it by [voltageLevel].
 *
 * When [voltageLevel] is non-null, the wire color is interpolated between cyan
 * (0 V) and neon-orange (high voltage). This provides an instant visual cue
 * for the DC operating-point result.
 */
fun DrawScope.drawWire(
    wire: Wire,
    nodeById: (String) -> Node?,
    wireColor: Color,
    voltageLevel: Double? = null,
    strokeWidth: Float = WIRE_STROKE
) {
    val start = nodeById(wire.startNodeId) ?: return
    val end   = nodeById(wire.endNodeId)   ?: return

    val color = if (voltageLevel != null) voltageColor(voltageLevel) else wireColor

    val path = Path().apply {
        moveTo(start.x, start.y)
        wire.waypoints.forEach { wp -> lineTo(wp.x, wp.y) }
        lineTo(end.x, end.y)
    }
    drawPath(path, color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
}

/**
 * Draws an animated ghost wire from [start] to [end] while the user is
 * dragging out a new connection.
 */
fun DrawScope.drawGhostWire(start: Offset, end: Offset, color: Color) {
    // Manhattan routing: horizontal segment, then vertical
    val midX = end.x
    val midY = start.y

    val path = Path().apply {
        moveTo(start.x, start.y)
        lineTo(midX, midY)
        lineTo(end.x, end.y)
    }
    drawPath(
        path  = path,
        color = color.copy(alpha = 0.7f),
        style = Stroke(width = GHOST_STROKE, cap = StrokeCap.Round)
    )
    // Endpoint indicator dot
    drawCircle(color = color.copy(alpha = 0.7f), radius = 5f, center = end)
}

/**
 * Draws a directional current arrow at the midpoint of the wire,
 * pointing from [start] to [end].
 *
 * The arrow is only drawn when [currentAmperes] is non-null and non-zero.
 * Its size and opacity scale with the absolute current magnitude.
 */
fun DrawScope.drawCurrentArrow(
    startNode: androidx.compose.ui.geometry.Offset,
    endNode: androidx.compose.ui.geometry.Offset,
    currentAmperes: Double,
    color: Color
) {
    if (currentAmperes == 0.0) return

    val mid = Offset((startNode.x + endNode.x) / 2f, (startNode.y + endNode.y) / 2f)
    val dx = endNode.x - startNode.x
    val dy = endNode.y - startNode.y
    val len = kotlin.math.sqrt(dx * dx + dy * dy)
    if (len < 1f) return

    val ux = dx / len     // unit vector along wire
    val uy = dy / len
    val nx = -uy          // normal (perpendicular)
    val ny =  ux

    val arrowLen = 12f
    val arrowWing = 6f

    // Flip direction based on current sign (negative = flows backwards)
    val sign = if (currentAmperes > 0) 1f else -1f

    val tip   = Offset(mid.x + ux * arrowLen * sign / 2, mid.y + uy * arrowLen * sign / 2)
    val base  = Offset(mid.x - ux * arrowLen * sign / 2, mid.y - uy * arrowLen * sign / 2)
    val left  = Offset(base.x + nx * arrowWing, base.y + ny * arrowWing)
    val right = Offset(base.x - nx * arrowWing, base.y - ny * arrowWing)

    val alpha = (kotlin.math.log10(1 + kotlin.math.abs(currentAmperes) * 1000) / 3.0)
        .coerceIn(0.2, 1.0).toFloat()
    val arrowColor = color.copy(alpha = alpha)

    val path = androidx.compose.ui.graphics.Path().apply {
        moveTo(tip.x, tip.y)
        lineTo(left.x, left.y)
        lineTo(right.x, right.y)
        close()
    }
    drawPath(path, arrowColor)
}

/** Maps a voltage level to a color on the cyan → orange gradient. */
private fun voltageColor(voltage: Double, referenceMax: Double = 12.0): Color {
    val t = (voltage / referenceMax).coerceIn(0.0, 1.0).toFloat()
    return lerp(NeonCyan, NeonOrange, t)
}
