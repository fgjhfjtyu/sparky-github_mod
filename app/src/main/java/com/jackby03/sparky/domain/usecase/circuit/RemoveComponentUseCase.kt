package com.jackby03.sparky.domain.usecase.circuit

import com.jackby03.sparky.core.result.AppResult
import com.jackby03.sparky.domain.model.circuit.Circuit
import javax.inject.Inject

class RemoveComponentUseCase @Inject constructor() {
    operator fun invoke(circuit: Circuit, componentId: String): AppResult<Circuit> {
        val updated = circuit.copy(
            components = circuit.components.filterNot { it.id == componentId },
            wires = circuit.wires.filterNot { wire ->
                val component = circuit.componentById(componentId)
                component?.terminalNodeIds?.any { it == wire.startNodeId || it == wire.endNodeId } == true
            },
            updatedAt = System.currentTimeMillis()
        )
        return AppResult.Success(updated)
    }
}
