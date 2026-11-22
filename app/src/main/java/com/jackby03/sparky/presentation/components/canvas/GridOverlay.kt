package com.jackby03.sparky.presentation.components.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import com.jackby03.sparky.core.constants.AppConstants

/**
 * Dot-grid overlay inspired by n8n — symmetric dots at each grid intersection.
 *
 * Uses the same `graphicsLayer` transform as the circuit layers so dots are
 * perfectly aligned with components.
 */
@Composable
fun GridOverlay(
    zoom: Float,
    offsetX: Float,
    offsetY: Float,
    gridColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier.graphicsLayer {
            scaleX = zoom; scaleY = zoom
            translationX = offsetX; translationY = offsetY
            transformOrigin = TransformOrigin(0f, 0f)
        }
    ) {
        val gridSize = AppConstants.Canvas.GRID_SIZE_DP

        // Viewport bounds in canvas coordinates
        val vpLeft   = -offsetX / zoom
        val vpTop    = -offsetY / zoom
        val vpRight  = vpLeft + size.width / zoom
        val vpBottom = vpTop + size.height / zoom

        // Snap to nearest grid line outside viewport
        val firstX = (kotlin.math.floor(vpLeft / gridSize) * gridSize).toFloat()
        val firstY = (kotlin.math.floor(vpTop / gridSize) * gridSize).toFloat()

        // Dot radius: constant screen-space size regardless of zoom
        val dotRadius = 1.2f / zoom

        var gx = firstX
        while (gx <= vpRight) {
            var gy = firstY
            while (gy <= vpBottom) {
                drawCircle(
                    color  = gridColor,
                    radius = dotRadius,
                    center = Offset(gx, gy)
                )
                gy += gridSize
            }
            gx += gridSize
        }
    }
}
