package com.jackby03.sparky.domain

import com.jackby03.sparky.core.result.AppResult
import com.jackby03.sparky.domain.model.circuit.Circuit
import com.jackby03.sparky.domain.model.circuit.Node
import com.jackby03.sparky.domain.model.component.Ground
import com.jackby03.sparky.domain.model.component.Resistor
import com.jackby03.sparky.domain.model.component.VoltageSource
import com.jackby03.sparky.domain.usecase.circuit.ValidateCircuitUseCase
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidateCircuitUseCaseTest {

    private val useCase = ValidateCircuitUseCase()

    @Test
    fun `empty circuit fails validation`() {
        val result = useCase(Circuit(name = "Empty"))
        assertTrue(result is AppResult.Error)
    }

    @Test
    fun `circuit without ground fails`() {
        val nodeA = Node(id = "A", x = 0f, y = 0f)
        val nodeB = Node(id = "B", x = 0f, y = 0f)
        val r = Resistor(id = "R1", terminalNodeIds = listOf("A", "B"))
        val circuit = Circuit(name = "No GND", components = listOf(r), nodes = listOf(nodeA, nodeB))
        val result = useCase(circuit)
        assertTrue(result is AppResult.Error)
    }

    @Test
    fun `circuit with components and ground passes`() {
        val gndNode = Node(id = "GND", x = 0f, y = 0f, isGround = true)
        val nodeA   = Node(id = "A",   x = 0f, y = 0f)
        val source  = VoltageSource(id = "V1", terminalNodeIds = listOf("A", "GND"), voltageVolts = 5.0)
        val gnd     = Ground(id = "GND_COMP", terminalNodeIds = listOf("GND"))
        val circuit = Circuit(
            name       = "Valid",
            components = listOf(source, gnd),
            nodes      = listOf(gndNode, nodeA)
        )
        val result = useCase(circuit)
        assertTrue(result is AppResult.Success)
    }
}
