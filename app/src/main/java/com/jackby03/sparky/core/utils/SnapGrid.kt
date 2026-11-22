package com.jackby03.sparky.core.utils

import com.jackby03.sparky.core.constants.AppConstants.Canvas

/**
 * Snaps a canvas coordinate to the nearest grid intersection.
 *
 * The grid size is defined in dp-equivalent canvas units. Since the canvas
 * coordinate system is in raw pixels at zoom=1, callers pass the density-
 * independent grid size in pixels (converted at the call site).
 */
object SnapGrid {

    /** Snap a single axis value to the nearest multiple of [gridSize]. */
    fun snap(value: Float, gridSize: Float = Canvas.GRID_SIZE_DP): Float {
        return (Math.round(value / gridSize) * gridSize)
    }

    /** Snap both axes of a position to the grid. */
    fun snapPosition(x: Float, y: Float, gridSize: Float = Canvas.GRID_SIZE_DP): Pair<Float, Float> =
        snap(x, gridSize) to snap(y, gridSize)
}
