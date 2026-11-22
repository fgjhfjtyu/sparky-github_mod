package com.jackby03.sparky.engine.model

import com.jackby03.sparky.core.extensions.isNearZero

/**
 * Dense matrix backed by a flat DoubleArray for cache-friendly access.
 * Used internally by the MNA solver.
 */
class Matrix(val rows: Int, val cols: Int) {

    private val data = DoubleArray(rows * cols)

    operator fun get(row: Int, col: Int): Double = data[row * cols + col]

    operator fun set(row: Int, col: Int, value: Double) {
        data[row * cols + col] = value
    }

    fun add(row: Int, col: Int, value: Double) {
        data[row * cols + col] += value
    }

    fun row(row: Int): DoubleArray = DoubleArray(cols) { col -> this[row, col] }

    fun swapRows(r1: Int, r2: Int) {
        if (r1 == r2) return
        for (col in 0 until cols) {
            val tmp = this[r1, col]
            this[r1, col] = this[r2, col]
            this[r2, col] = tmp
        }
    }

    fun isDegenerate(): Boolean {
        for (row in 0 until rows) {
            if ((0 until cols).all { col -> this[row, col].isNearZero() }) return true
        }
        return false
    }

    override fun toString(): String = buildString {
        for (row in 0 until rows) {
            append(row(row).joinToString(prefix = "[ ", postfix = " ]", separator = "\t") { "%.4f".format(it) })
            appendLine()
        }
    }
}
