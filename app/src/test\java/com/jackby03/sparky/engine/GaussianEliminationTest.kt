package com.jackby03.sparky.engine

import com.jackby03.sparky.engine.model.Matrix
import com.jackby03.sparky.engine.solver.GaussianElimination
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.math.abs

class GaussianEliminationTest {

    private fun assertSolution(expected: DoubleArray, actual: DoubleArray?, tol: Double = 1e-9) {
        checkNotNull(actual) { "Expected a solution but got null" }
        check(expected.size == actual.size) { "Size mismatch: ${expected.size} vs ${actual.size}" }
        for (i in expected.indices) {
            check(abs(expected[i] - actual[i]) < tol) {
                "Index $i: expected ${expected[i]} but was ${actual[i]}"
            }
        }
    }

    @Test
    fun `single equation trivial`() {
        // 2x = 6  →  x = 3
        val G = Matrix(1, 1).apply { set(0, 0, 2.0) }
        val I = doubleArrayOf(6.0)
        assertSolution(doubleArrayOf(3.0), GaussianElimination.solve(G, I))
    }

    @Test
    fun `2x2 system`() {
        // x + y = 3
        // x - y = 1
        // → x = 2, y = 1
        val G = Matrix(2, 2).apply {
            set(0, 0, 1.0); set(0, 1, 1.0)
            set(1, 0, 1.0); set(1, 1, -1.0)
        }
        val I = doubleArrayOf(3.0, 1.0)
        assertSolution(doubleArrayOf(2.0, 1.0), GaussianElimination.solve(G, I))
    }

    @Test
    fun `3x3 system`() {
        // 2x + y - z = 8
        // -3x - y + 2z = -11
        // -2x + y + 2z = -3
        // → x = 2, y = 3, z = -1
        val G = Matrix(3, 3).apply {
            set(0, 0, 2.0); set(0, 1, 1.0);  set(0, 2, -1.0)
            set(1, 0,-3.0); set(1, 1,-1.0);  set(1, 2, 2.0)
            set(2, 0,-2.0); set(2, 1, 1.0);  set(2, 2, 2.0)
        }
        val I = doubleArrayOf(8.0, -11.0, -3.0)
        assertSolution(doubleArrayOf(2.0, 3.0, -1.0), GaussianElimination.solve(G, I))
    }

    @Test
    fun `singular matrix returns null`() {
        // x + y = 1
        // x + y = 2  (inconsistent)
        val G = Matrix(2, 2).apply {
            set(0, 0, 1.0); set(0, 1, 1.0)
            set(1, 0, 1.0); set(1, 1, 1.0)
        }
        val I = doubleArrayOf(1.0, 2.0)
        assertNull(GaussianElimination.solve(G, I))
    }

    @Test
    fun `zero matrix returns null`() {
        val G = Matrix(2, 2)
        val I = doubleArrayOf(1.0, 2.0)
        assertNull(GaussianElimination.solve(G, I))
    }
}
