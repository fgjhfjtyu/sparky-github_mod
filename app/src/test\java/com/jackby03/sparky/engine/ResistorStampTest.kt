package com.jackby03.sparky.engine

import com.jackby03.sparky.engine.model.Matrix
import com.jackby03.sparky.engine.stamp.ResistorStamp
import org.junit.Assert.assertEquals
import org.junit.Test

class ResistorStampTest {

    private val tol = 1e-12

    @Test
    fun `resistor between two non-ground nodes stamps conductance correctly`() {
        val G = Matrix(2, 2)
        val I = DoubleArray(2)
        val nodeIndex = mapOf("A" to 0, "B" to 1)

        // 1 kΩ resistor → conductance = 1e-3 S
        ResistorStamp("A", "B", 1_000.0).stamp(G, I, nodeIndex, emptyMap())

        // Diagonal: +G
        assertEquals(1e-3, G[0, 0], tol)
        assertEquals(1e-3, G[1, 1], tol)
        // Off-diagonal: -G
        assertEquals(-1e-3, G[0, 1], tol)
        assertEquals(-1e-3, G[1, 0], tol)
    }

    @Test
    fun `resistor from node to ground stamps only diagonal`() {
        val G = Matrix(1, 1)
        val I = DoubleArray(1)
        // "GND" is ground → nodeIndex does not contain it
        val nodeIndex = mapOf("A" to 0)

        ResistorStamp("A", "GND", 500.0).stamp(G, I, nodeIndex, emptyMap())

        assertEquals(2e-3, G[0, 0], tol)
        assertEquals(0.0, I[0], tol)
    }

    @Test
    fun `very small resistance (short circuit approximation)`() {
        val G = Matrix(2, 2)
        val I = DoubleArray(2)
        val nodeIndex = mapOf("A" to 0, "B" to 1)

        // 1 µΩ → conductance = 1e6
        ResistorStamp("A", "B", 1e-6).stamp(G, I, nodeIndex, emptyMap())

        assertEquals(1e6, G[0, 0], tol)
        assertEquals(-1e6, G[0, 1], tol)
    }
}
