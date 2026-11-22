package com.jackby03.sparky.domain

import com.jackby03.sparky.core.result.AppResult
import com.jackby03.sparky.domain.model.circuit.Circuit
import com.jackby03.sparky.domain.model.component.ComponentType
import com.jackby03.sparky.domain.model.component.Resistor
import com.jackby03.sparky.domain.usecase.circuit.AddComponentUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AddComponentUseCaseTest {

    private val useCase = AddComponentUseCase()

    @Test
    fun `adds component to empty circuit`() {
        val circuit = Circuit(name = "Test")
        val result = useCase(circuit, Resistor(x = 100f, y = 100f), ComponentType.RESISTOR)

        assertTrue(result is AppResult.Success)
        val updated = (result as AppResult.Success).data
        assertEquals(1, updated.components.size)
    }

    @Test
    fun `auto-numbers first resistor as R1`() {
        val circuit = Circuit(name = "Test")
        val result = useCase(circuit, Resistor(x = 100f, y = 100f), ComponentType.RESISTOR)
        val updated = (result as AppResult.Success).data
        assertEquals("R1", updated.components.first().label)
    }

    @Test
    fun `auto-numbers second resistor as R2`() {
        val circuit = Circuit(name = "Test")
        val r1 = useCase(circuit, Resistor(x = 100f, y = 100f), ComponentType.RESISTOR)
        val circuitWith1 = (r1 as AppResult.Success).data
        val r2 = useCase(circuitWith1, Resistor(x = 200f, y = 100f), ComponentType.RESISTOR)
        val updated = (r2 as AppResult.Success).data
        assertEquals("R2", updated.components.last().label)
    }

    @Test
    fun `creates two terminal nodes on placement`() {
        val circuit = Circuit(name = "Test")
        val result = useCase(circuit, Resistor(x = 100f, y = 100f), ComponentType.RESISTOR)
        val updated = (result as AppResult.Success).data
        assertEquals(2, updated.nodes.size)
    }

    @Test
    fun `terminal node IDs are wired into component`() {
        val circuit = Circuit(name = "Test")
        val result = useCase(circuit, Resistor(x = 100f, y = 100f), ComponentType.RESISTOR)
        val updated = (result as AppResult.Success).data
        val component = updated.components.first()
        val nodeIds = updated.nodes.map { it.id }
        assertTrue(component.terminalNodeIds.all { it in nodeIds })
    }
}
