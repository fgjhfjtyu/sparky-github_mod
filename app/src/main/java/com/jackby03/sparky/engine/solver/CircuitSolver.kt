package com.jackby03.sparky.engine.solver

import com.jackby03.sparky.core.result.AppException
import com.jackby03.sparky.domain.model.circuit.Circuit
import com.jackby03.sparky.domain.model.simulation.SimulationConfig
import com.jackby03.sparky.domain.model.simulation.SimulationResult
import com.jackby03.sparky.domain.model.simulation.SimulationType
import com.jackby03.sparky.engine.model.SolverResult
import javax.inject.Inject

class CircuitSolver @Inject constructor(
    private val dcAnalyzer: DcAnalyzer
) {
    suspend fun solve(circuit: Circuit, config: SimulationConfig): SimulationResult {
        val startMs = System.currentTimeMillis()

        val result = when (config.type) {
            SimulationType.DC -> dcAnalyzer.analyze(circuit, config.dcConfig)
            else -> throw AppException.SimulationException("${config.type.displayName} not yet supported")
        }

        val elapsed = System.currentTimeMillis() - startMs

        return when (result) {
            is SolverResult.Success -> SimulationResult(
                type = config.type,
                nodeVoltages = result.nodeVoltages,
                branchCurrents = result.branchCurrents,
                isConverged = true,
                iterationCount = result.iterationCount,
                elapsedMs = elapsed
            )
            is SolverResult.Failure -> throw AppException.SimulationException(result.message)
        }
    }
}
