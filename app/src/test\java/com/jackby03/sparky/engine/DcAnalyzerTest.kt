package com.jackby03.sparky.engine

import com.jackby03.sparky.domain.model.circuit.Circuit
import com.jackby03.sparky.domain.model.circuit.Node
import com.jackby03.sparky.domain.model.component.Ground
import com.jackby03.sparky.domain.model.component.Resistor
import com.jackby03.sparky.domain.model.component.VoltageSource
import com.jackby03.sparky.domain.model.simulation.DcConfig
import com.jackby03.sparky.engine.model.FailureReason
import com.jackby03.sparky.engine.model.SolverResult
import com.jackby03.sparky.engine.solver.DcAnalyzer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

/**
 * End-to-end DC analysis tests.
 *
 * Uses a simple voltage divider: V+ — R1(1kΩ) — R2(1kΩ) — GND
 *
 *   5 V source, ground on negative terminal.
 *   Expected mid-node voltage: 2.5 V.
 *   Current through both resistors: 2.5 mA.
 */
class DcAnalyzerTest {

    private val tol = 1e-6

    /** nodeA = positive terminal of source, nodeB = mid-point, nodeGND = ground. */
    private fun buildVoltageDivider(): Circuit {
        val nodeGND = Node(id = "GND", x = 0f, y = 0f, isGround = true)
        val nodeA   = Node(id = "A",   x = 0f, y = 0f)
        val nodeB   = Node(id = "B",   x = 0f, y = 0f)

        val source = VoltageSource(
            id              = "V1",
            terminalNodeIds = listOf("A", "GND"),
            voltageVolts    = 5.0
        )
        val r1 = Resistor(
            id              = "R1",
            terminalNodeIds = listOf("A", "B"),
            resistanceOhm   = 1_000.0
        )
        val r2 = Resistor(
            id              = "R2",
            terminalNodeIds = listOf("B", "GND"),
            resistanceOhm   = 1_000.0
        )
        val gnd = Ground(id = "GND_COMP", terminalNodeIds = listOf("GND"))

        return Circuit(
            name       = "Divider",
            components = listOf(source, r1, r2, gnd),
            nodes      = listOf(nodeGND, nodeA, nodeB)
        )
    }

    @Test
    fun `voltage divider midpoint is 2_5V`() {
        val circuit = buildVoltageDivider()
        val result  = DcAnalyzer().analyze(circuit, DcConfig())

        assertTrue("Expected Success but got $result", result is SolverResult.Success)
        val success = result as SolverResult.Success

        val vA = success.nodeVoltages["A"] ?: error("No voltage for node A")
        val vB = success.nodeVoltages["B"] ?: error("No voltage for node B")

        assertEquals("Node A should be 5 V", 5.0, vA, tol)
        assertEquals("Node B should be 2.5 V", 2.5, vB, tol)
    }

    @Test
    fun `voltage divider current through source is 2_5mA`() {
        val circuit = buildVoltageDivider()
        val result  = DcAnalyzer().analyze(circuit, DcConfig()) as SolverResult.Success

        val current = result.branchCurrents["V1"] ?: error("No current for V1")
        assertEquals(2.5e-3, abs(current), tol)
    }

    @Test
    fun `circuit without ground returns failure`() {
        val nodeA = Node(id = "A", x = 0f, y = 0f)
        val r = Resistor(id = "R1", terminalNodeIds = listOf("A", "B"))
        val circuit = Circuit(name = "No Ground", components = listOf(r), nodes = listOf(nodeA))

        val result = DcAnalyzer().analyze(circuit, DcConfig())

        assertTrue(result is SolverResult.Failure)
        assertEquals(FailureReason.NO_GROUND_NODE, (result as SolverResult.Failure).reason)
    }
}
