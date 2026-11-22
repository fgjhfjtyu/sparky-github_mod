package com.jackby03.sparky.data

import com.jackby03.sparky.data.mapper.toDomain
import com.jackby03.sparky.data.mapper.toEntity
import com.jackby03.sparky.domain.model.circuit.Circuit
import com.jackby03.sparky.domain.model.circuit.Node
import com.jackby03.sparky.domain.model.circuit.Wire
import com.jackby03.sparky.domain.model.component.Capacitor
import com.jackby03.sparky.domain.model.component.Diode
import com.jackby03.sparky.domain.model.component.Ground
import com.jackby03.sparky.domain.model.component.Inductor
import com.jackby03.sparky.domain.model.component.Resistor
import com.jackby03.sparky.domain.model.component.VoltageSource
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Roundtrip tests: Circuit → CircuitEntity → Circuit
 *
 * Verifies that all component types, node positions, wire topology,
 * and component values survive JSON serialization without data loss.
 */
class CircuitMapperTest {

    @Test
    fun `empty circuit roundtrips correctly`() {
        val circuit = Circuit(id = "c1", name = "Empty", description = "desc")
        val restored = circuit.toEntity().toDomain()

        assertEquals(circuit.id,          restored.id)
        assertEquals(circuit.name,        restored.name)
        assertEquals(circuit.description, restored.description)
        assertEquals(0, restored.components.size)
        assertEquals(0, restored.nodes.size)
        assertEquals(0, restored.wires.size)
    }

    @Test
    fun `resistor value survives roundtrip`() {
        val r = Resistor(id = "r1", label = "R1", x = 100f, y = 200f,
            resistanceOhm = 4_700.0, terminalNodeIds = listOf("n1", "n2"))
        val circuit = Circuit(name = "Test", components = listOf(r))
        val restored = circuit.toEntity().toDomain().components.first() as Resistor

        assertEquals("r1",    restored.id)
        assertEquals("R1",    restored.label)
        assertEquals(100f,    restored.x, 0f)
        assertEquals(200f,    restored.y, 0f)
        assertEquals(4_700.0, restored.resistanceOhm, 1e-9)
        assertEquals(listOf("n1", "n2"), restored.terminalNodeIds)
    }

    @Test
    fun `all component types survive roundtrip`() {
        val components = listOf(
            Resistor(id = "r1",  label = "R1",  terminalNodeIds = listOf("n1", "n2"), resistanceOhm = 1_000.0),
            Capacitor(id = "c1", label = "C1",  terminalNodeIds = listOf("n2", "n3"), capacitanceFarad = 1e-6),
            Inductor(id = "l1",  label = "L1",  terminalNodeIds = listOf("n3", "n4"), inductanceHenry = 1e-3),
            VoltageSource(id = "v1", label = "V1", terminalNodeIds = listOf("n4", "n5"), voltageVolts = 12.0),
            Diode(id = "d1",     label = "D1",  terminalNodeIds = listOf("n5", "n6"),
                forwardVoltageVolts = 0.7, saturationCurrentAmperes = 1e-14),
            Ground(id = "g1",    label = "GND", terminalNodeIds = listOf("n6"))
        )
        val circuit = Circuit(name = "All types", components = components)
        val restored = circuit.toEntity().toDomain()

        assertEquals(6, restored.components.size)
        assertEquals("RESISTOR",       restored.components[0]::class.simpleName?.uppercase() ?: "")
        val diode = restored.components[4] as Diode
        assertEquals(0.7,  diode.forwardVoltageVolts,   1e-9)
        assertEquals(1e-14, diode.saturationCurrentAmperes, 1e-20)
    }

    @Test
    fun `node positions survive roundtrip`() {
        val nodes = listOf(
            Node(id = "n1", x = 0f, y = 0f, isGround = true),
            Node(id = "n2", x = 100f, y = 50f, label = "mid")
        )
        val circuit = Circuit(name = "Nodes", nodes = nodes)
        val restored = circuit.toEntity().toDomain()

        assertEquals(2, restored.nodes.size)
        assertEquals(true,  restored.nodes[0].isGround)
        assertEquals(100f,  restored.nodes[1].x, 0f)
        assertEquals(50f,   restored.nodes[1].y, 0f)
        assertEquals("mid", restored.nodes[1].label)
    }

    @Test
    fun `wire topology survives roundtrip`() {
        val wire = Wire(id = "w1", startNodeId = "n1", endNodeId = "n2")
        val circuit = Circuit(name = "Wired", wires = listOf(wire))
        val restored = circuit.toEntity().toDomain()

        assertEquals(1,    restored.wires.size)
        assertEquals("w1", restored.wires[0].id)
        assertEquals("n1", restored.wires[0].startNodeId)
        assertEquals("n2", restored.wires[0].endNodeId)
    }

    @Test
    fun `component rotation survives roundtrip`() {
        val r = Resistor(id = "r1", label = "R1", rotation = 90f, terminalNodeIds = listOf("n1", "n2"))
        val circuit = Circuit(name = "Rotated", components = listOf(r))
        val restored = (circuit.toEntity().toDomain().components.first() as Resistor)

        assertEquals(90f, restored.rotation, 0f)
    }
}
