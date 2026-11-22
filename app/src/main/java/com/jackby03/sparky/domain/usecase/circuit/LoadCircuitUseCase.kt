package com.jackby03.sparky.domain.usecase.circuit

import com.jackby03.sparky.core.result.AppException
import com.jackby03.sparky.core.result.AppResult
import com.jackby03.sparky.domain.model.circuit.Circuit
import com.jackby03.sparky.domain.repository.CircuitRepository
import javax.inject.Inject

class LoadCircuitUseCase @Inject constructor(
    private val repository: CircuitRepository
) {
    suspend operator fun invoke(id: String): AppResult<Circuit> =
        try {
            val circuit = repository.getById(id)
            if (circuit != null) AppResult.Success(circuit)
            else AppResult.Error(AppException.DatabaseException("Circuit not found: $id"))
        } catch (e: Exception) {
            AppResult.Error(AppException.DatabaseException("Failed to load circuit", e))
        }
}
