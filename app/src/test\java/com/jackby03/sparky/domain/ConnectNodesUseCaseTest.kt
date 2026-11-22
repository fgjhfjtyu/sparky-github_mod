package com.jackby03.sparky.domain

import com.jackby03.sparky.core.result.AppResult
import com.jackby03.sparky.domain.model.circuit.Circuit
import com.jackby03.sparky.domain.model.circuit.Node
import com.jackby03.sparky.domain.usecase.circuit.ConnectNodesUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConnectNodesUseCaseTest {

    private val useCase = ConnectNodesUseCase()

    private fun circuitWithNodes(): Circuit {
        val n1 = Node(id = "n1", x = 0f, y = 0f)
        val n2 = Node(id = "n2", x = 100f, y = 0f)
        return Circuit(name = "Test", nodes = listOf(n1, n2))
    }

    @Test
    fun `connects two nodes`() {
        val circuit = circuitWithNodes()
        val result = useCase(circuit, "n1", "n2")

        assertTrue(result is AppResult.Success)
        assertEquals(1, (result as AppResult.Success).data.wires.size)
    }

    @Test
    fun `wire has correct node IDs`() {
        val circuit = circuitWithNodes()
        val wire = ((useCase(circuit, "n1", "n2")) as AppResult.Success).data.wires.first()

        assertEquals("n1", wire.startNodeId)
        assertEquals("n2", wire.endNodeId)
    }

    @Test
    fun `connecting node to itself returns error`() {
        val circuit = circuitWithNodes()
        val result = useCase(circuit, "n1", "n1")

        assertTrue(result is AppResult.Error)
    }

    @Test
    fun `duplicate connection returns error`() {
        val circuit = circuitWithNodes()
        val connected = (useCase(circuit, "n1", "n2") as AppResult.Success).data
        val duplicate = useCase(connected, "n1", "n2")

        assertTrue(duplicate is AppResult.Error)
    }

    @Test
    fun `reverse duplicate connection returns error`() {
        val circuit = circuitWithNodes()
        val connected = (useCase(circuit, "n1", "n2") as AppResult.Success).data
        val duplicate = useCase(connected, "n2", "n1")

        assertTrue(duplicate is AppResult.Error)
    }
}
