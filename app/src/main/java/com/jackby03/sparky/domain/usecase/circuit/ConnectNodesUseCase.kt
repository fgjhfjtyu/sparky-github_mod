package com.jackby03.sparky.domain.usecase.circuit

import com.jackby03.sparky.core.result.AppException
import com.jackby03.sparky.core.result.AppResult
import com.jackby03.sparky.domain.model.circuit.Circuit
import com.jackby03.sparky.domain.model.circuit.Wire
import javax.inject.Inject

class ConnectNodesUseCase @Inject constructor() {
    operator fun invoke(circuit: Circuit, startNodeId: String, endNodeId: String): AppResult<Circuit> {
        if (startNodeId == endNodeId) {
            return AppResult.Error(AppException.ValidationException("Cannot connect a node to itself"))
        }
        val alreadyConnected = circuit.wires.any { wire ->
            (wire.startNodeId == startNodeId && wire.endNodeId == endNodeId) ||
            (wire.startNodeId == endNodeId && wire.endNodeId == startNodeId)
        }
        if (alreadyConnected) {
            return AppResult.Error(AppException.ValidationException("Nodes are already connected"))
        }
        val wire = Wire(startNodeId = startNodeId, endNodeId = endNodeId)
        val updated = circuit.copy(
            wires = circuit.wires + wire,
            updatedAt = System.currentTimeMillis()
        )
        return AppResult.Success(updated)
    }
}
