package com.jackby03.sparky.engine.solver

import com.jackby03.sparky.domain.model.circuit.Circuit
import com.jackby03.sparky.domain.model.component.Capacitor
import com.jackby03.sparky.domain.model.component.CurrentSource
import com.jackby03.sparky.domain.model.component.Diode
import com.jackby03.sparky.domain.model.component.Ground
import com.jackby03.sparky.domain.model.component.Inductor
import com.jackby03.sparky.domain.model.component.Resistor
import com.jackby03.sparky.domain.model.component.VoltageSource
import com.jackby03.sparky.engine.model.Matrix
import com.jackby03.sparky.engine.stamp.CapacitorStamp
import com.jackby03.sparky.engine.stamp.CurrentSourceStamp
import com.jackby03.sparky.engine.stamp.DiodeStamp
import com.jackby03.sparky.engine.stamp.InductorStamp
import com.jackby03.sparky.engine.stamp.ResistorStamp
import com.jackby03.sparky.engine.stamp.VoltageSourceStamp

data class MnaSystem(
    val G: Matrix,
    val I: DoubleArray,
    val nodeIndex: Map<String, Int>,
    val branchIndex: Map<String, Int>,
    val groundNodeId: String
)

/**
 * Builds the MNA (Modified Nodal Analysis) system [G·x = I] from a [Circuit].
 *
 * Branch variables (extra unknowns beyond node voltages) are added for every
 * element that enforces a voltage constraint: VoltageSource, Inductor (0 V
 * short), and Diode (forward-voltage source in piecewise-linear model).
 *
 * Capacitors are open-circuited at DC and contribute no stamp.
 */
object MnaMatrixBuilder {

    fun build(circuit: Circuit): MnaSystem {
        val groundNode    = circuit.nodes.first { it.isGround }
        val nonGroundNodes = circuit.nodes.filter { !it.isGround }

        val voltageSources = circuit.components.filterIsInstance<VoltageSource>()
        val inductors      = circuit.components.filterIsInstance<Inductor>()
        val diodes         = circuit.components.filterIsInstance<Diode>()

        // Node indices: 0 .. (nonGroundNodes.size - 1)
        val nodeIndex: Map<String, Int> = nonGroundNodes
            .mapIndexed { idx, node -> node.id to idx }
            .toMap()

        // Branch indices start after node section
        val branchOffset = nonGroundNodes.size
        val branchIndex  = mutableMapOf<String, Int>()
        var branchCounter = branchOffset

        voltageSources.forEach { src -> branchIndex[src.id] = branchCounter++ }
        inductors.forEach      { l   -> branchIndex[l.id]   = branchCounter++ }
        diodes.forEach         { d   -> branchIndex[d.id]   = branchCounter++ }

        val size = branchCounter
        val G = Matrix(size, size)
        val I = DoubleArray(size)

        circuit.components.forEach { component ->
            val stamp = when (component) {
                is Resistor -> ResistorStamp(
                    nodeId1 = component.terminalNodeIds[0],
                    nodeId2 = component.terminalNodeIds[1],
                    resistanceOhm = component.resistanceOhm
                )
                is VoltageSource -> VoltageSourceStamp(
                    componentId  = component.id,
                    posNodeId    = component.terminalNodeIds[0],
                    negNodeId    = component.terminalNodeIds[1],
                    voltageVolts = component.voltageVolts
                )
                is CurrentSource -> CurrentSourceStamp(
                    posNodeId      = component.terminalNodeIds[0],
                    negNodeId      = component.terminalNodeIds[1],
                    currentAmperes = component.currentAmperes
                )
                is Inductor -> InductorStamp(
                    componentId = component.id,
                    nodeId1     = component.terminalNodeIds[0],
                    nodeId2     = component.terminalNodeIds[1]
                )
                is Diode -> DiodeStamp(
                    componentId    = component.id,
                    anodeNodeId    = component.terminalNodeIds[0],
                    cathodeNodeId  = component.terminalNodeIds[1],
                    forwardVoltage = component.forwardVoltageVolts
                )
                is Capacitor -> CapacitorStamp()
                is Ground    -> null
                is com.jackby03.sparky.domain.model.component.SymbolComponent -> null
            }
            stamp?.stamp(G, I, nodeIndex, branchIndex)
        }

        return MnaSystem(G, I, nodeIndex, branchIndex.toMap(), groundNode.id)
    }
}
