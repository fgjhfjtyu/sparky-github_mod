package com.jackby03.sparky.domain.usecase.circuit

import com.jackby03.sparky.core.result.AppException
import com.jackby03.sparky.core.result.AppResult
import com.jackby03.sparky.domain.model.circuit.Circuit
import com.jackby03.sparky.domain.repository.CircuitRepository
import javax.inject.Inject

class CreateCircuitUseCase @Inject constructor(
    private val repository: CircuitRepository
) {
    suspend operator fun invoke(name: String, description: String = ""): AppResult<Circuit> =
        try {
            val circuit = Circuit(name = name.trim(), description = description.trim())
            repository.save(circuit)
            AppResult.Success(circuit)
        } catch (e: Exception) {
            AppResult.Error(AppException.DatabaseException("Failed to create circuit", e))
        }
}
