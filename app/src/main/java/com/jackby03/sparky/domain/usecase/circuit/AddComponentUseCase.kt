package com.jackby03.sparky.domain.usecase.circuit

import com.jackby03.sparky.core.result.AppResult
import com.jackby03.sparky.core.utils.AutoNumbering
import com.jackby03.sparky.domain.model.circuit.Circuit
import com.jackby03.sparky.domain.model.circuit.Node
import com.jackby03.sparky.domain.model.component.CircuitComponent
import com.jackby03.sparky.domain.model.component.ComponentType
import com.jackby03.sparky.domain.model.component.Ground
import java.util.UUID
import javax.inject.Inject

class AddComponentUseCase @Inject constructor() {

    /**
     * Adds [component] to [circuit] with:
     * - Auto-generated label (R1, R2, C1, ...)
     * - Two terminal nodes placed at the component's pin positions
     * - Terminal node IDs wired into the component
     */
    operator fun invoke(
        circuit: Circuit,
        component: CircuitComponent,
        type: ComponentType
    ): AppResult<Circuit> {
        val label = AutoNumbering.nextLabel(type, circuit.components)
        val (t0Id, t1Id, newNodes) = buildTerminals(component)
        val labeled = component.withLabel(label).withTerminals(t0Id, t1Id)

        val updated = circuit.copy(
            components = circuit.components + labeled,
            nodes      = circuit.nodes + newNodes,
            updatedAt  = System.currentTimeMillis()
        )
        return AppResult.Success(updated)
    }

    private fun buildTerminals(component: CircuitComponent): Triple<String, String, List<Node>> {
        val (pin0, pin1) = terminalPositions(component)
        val node0 = Node(id = UUID.randomUUID().toString(), x = pin0.first, y = pin0.second)
        val node1 = Node(id = UUID.randomUUID().toString(), x = pin1.first, y = pin1.second,
            isGround = component is Ground)
        return Triple(node0.id, node1.id, listOf(node0, node1))
    }

    /** Returns (x, y) for pin0 and pin1 in canvas coordinates at rotation = 0. */
    private fun terminalPositions(c: CircuitComponent): Pair<Pair<Float, Float>, Pair<Float, Float>> {
        val half = 24f // matches ComponentRenderer HALF constant
        return when (c) {
            is Ground -> (c.x to c.y - half) to (c.x to c.y)
            else      -> (c.x - half to c.y) to (c.x + half to c.y)
        }
    }
}

/** Extension: replace terminalNodeIds while keeping other fields intact. */
private fun CircuitComponent.withTerminals(t0: String, t1: String): CircuitComponent = when (this) {
    is com.jackby03.sparky.domain.model.component.Resistor      -> copy(terminalNodeIds = listOf(t0, t1))
    is com.jackby03.sparky.domain.model.component.Capacitor     -> copy(terminalNodeIds = listOf(t0, t1))
    is com.jackby03.sparky.domain.model.component.Inductor      -> copy(terminalNodeIds = listOf(t0, t1))
    is com.jackby03.sparky.domain.model.component.VoltageSource -> copy(terminalNodeIds = listOf(t0, t1))
    is com.jackby03.sparky.domain.model.component.CurrentSource -> copy(terminalNodeIds = listOf(t0, t1))
    is com.jackby03.sparky.domain.model.component.Diode         -> copy(terminalNodeIds = listOf(t0, t1))
    is Ground                                                    -> copy(terminalNodeIds = listOf(t0))
    is com.jackby03.sparky.domain.model.component.SymbolComponent -> copy(terminalNodeIds = listOf(t0, t1))
}
