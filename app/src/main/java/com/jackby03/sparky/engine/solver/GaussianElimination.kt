package com.jackby03.sparky.engine.solver

import com.jackby03.sparky.core.extensions.isNearZero
import com.jackby03.sparky.engine.model.Matrix

/**
 * Solves [A][x] = [b] using Gaussian elimination with partial pivoting.
 * Returns the solution vector or null if the matrix is singular.
 */
object GaussianElimination {

    fun solve(A: Matrix, b: DoubleArray): DoubleArray? {
        val n = b.size
        val aug = buildAugmented(A, b)

        for (col in 0 until n) {
            val pivotRow = findPivot(aug, col, n) ?: return null
            aug.swapRows(col, pivotRow)

            val pivot = aug[col, col]
            if (pivot.isNearZero()) return null

            scaleRow(aug, col, n, pivot)
            eliminateBelow(aug, col, n)
        }

        return backSubstitute(aug, n)
    }

    private fun buildAugmented(A: Matrix, b: DoubleArray): Matrix {
        val n = b.size
        val aug = Matrix(n, n + 1)
        for (r in 0 until n) {
            for (c in 0 until n) aug[r, c] = A[r, c]
            aug[r, n] = b[r]
        }
        return aug
    }

    private fun findPivot(aug: Matrix, col: Int, n: Int): Int? {
        var maxRow = col
        var maxVal = Math.abs(aug[col, col])
        for (row in col + 1 until n) {
            val v = Math.abs(aug[row, col])
            if (v > maxVal) { maxVal = v; maxRow = row }
        }
        return if (aug[maxRow, col].isNearZero()) null else maxRow
    }

    private fun scaleRow(aug: Matrix, row: Int, n: Int, pivot: Double) {
        for (c in row..n) aug[row, c] = aug[row, c] / pivot
    }

    private fun eliminateBelow(aug: Matrix, pivotRow: Int, n: Int) {
        for (row in pivotRow + 1 until n) {
            val factor = aug[row, pivotRow]
            for (c in pivotRow..n) aug[row, c] -= factor * aug[pivotRow, c]
        }
    }

    private fun backSubstitute(aug: Matrix, n: Int): DoubleArray {
        val x = DoubleArray(n)
        for (row in n - 1 downTo 0) {
            x[row] = aug[row, n]
            for (c in row + 1 until n) x[row] -= aug[row, c] * x[c]
        }
        return x
    }
}
