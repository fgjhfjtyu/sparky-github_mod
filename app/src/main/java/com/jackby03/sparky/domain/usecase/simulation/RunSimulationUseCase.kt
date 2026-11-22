package com.jackby03.sparky.domain.usecase.simulation

import com.jackby03.sparky.core.result.AppException
import com.jackby03.sparky.core.result.AppResult
import com.jackby03.sparky.domain.model.circuit.Circuit
import com.jackby03.sparky.domain.model.simulation.SimulationConfig
import com.jackby03.sparky.domain.model.simulation.SimulationResult
import com.jackby03.sparky.domain.usecase.circuit.ValidateCircuitUseCase
import com.jackby03.sparky.engine.solver.CircuitSolver
import javax.inject.Inject

class RunSimulationUseCase @Inject constructor(
    private val solver: CircuitSolver,
    private val validateCircuit: ValidateCircuitUseCase
) {
    suspend operator fun invoke(
        circuit: Circuit,
        config: SimulationConfig
    ): AppResult<SimulationResult> {
        val validation = validateCircuit(circuit)
        if (validation is AppResult.Error) return validation

        return try {
            val result = solver.solve(circuit, config)
            AppResult.Success(result)
        } catch (e: Exception) {
            AppResult.Error(AppException.SimulationException("Simulation failed: ${e.message}", e))
        }
    }
}
