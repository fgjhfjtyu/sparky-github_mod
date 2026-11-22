package com.jackby03.sparky.domain.usecase.circuit

import com.jackby03.sparky.core.result.AppException
import com.jackby03.sparky.core.result.AppResult
import com.jackby03.sparky.domain.model.circuit.Circuit
import com.jackby03.sparky.domain.repository.CircuitRepository
import javax.inject.Inject

class SaveCircuitUseCase @Inject constructor(
    private val repository: CircuitRepository
) {
    suspend operator fun invoke(circuit: Circuit): AppResult<Unit> =
        try {
            val updated = circuit.copy(updatedAt = System.currentTimeMillis())
            repository.save(updated)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppException.DatabaseException("Failed to save circuit", e))
        }
}
