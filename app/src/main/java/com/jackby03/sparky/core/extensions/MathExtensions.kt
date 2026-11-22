package com.jackby03.sparky.core.extensions

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

fun Double.isNearZero(threshold: Double = 1e-12): Boolean = abs(this) < threshold

fun Double.clamp(min: Double, max: Double): Double = maxOf(min, minOf(max, this))

fun Float.clamp(min: Float, max: Float): Float = maxOf(min, minOf(max, this))

fun DoubleArray.magnitude(): Double = sqrt(sumOf { it.pow(2) })

fun Double.toEngineeringString(): String = when {
    abs(this) >= 1e9  -> "%.3f G".format(this / 1e9)
    abs(this) >= 1e6  -> "%.3f M".format(this / 1e6)
    abs(this) >= 1e3  -> "%.3f k".format(this / 1e3)
    abs(this) >= 1.0  -> "%.3f".format(this)
    abs(this) >= 1e-3 -> "%.3f m".format(this * 1e3)
    abs(this) >= 1e-6 -> "%.3f µ".format(this * 1e6)
    abs(this) >= 1e-9 -> "%.3f n".format(this * 1e9)
    else              -> "%.3f p".format(this * 1e12)
}
