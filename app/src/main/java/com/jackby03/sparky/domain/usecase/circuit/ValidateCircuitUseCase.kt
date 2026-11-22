package com.jackby03.sparky.domain.usecase.circuit

import com.jackby03.sparky.core.result.AppException
import com.jackby03.sparky.core.result.AppResult
import com.jackby03.sparky.domain.model.circuit.Circuit
import com.jackby03.sparky.engine.validator.CircuitValidator
import javax.inject.Inject

class ValidateCircuitUseCase @Inject constructor(
    private val validator: CircuitValidator
) {
    operator fun invoke(circuit: Circuit): AppResult<Unit> {
        val errors = validator.validate(circuit)
        return if (errors.isEmpty()) {
            AppResult.Success(Unit)
        } else {
            AppResult.Error(AppException.ValidationException(errors.joinToString("\n") { it.message }))
        }
    }
}
