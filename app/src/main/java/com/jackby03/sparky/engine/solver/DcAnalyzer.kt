package com.jackby03.sparky.engine.solver

import com.jackby03.sparky.domain.model.circuit.Circuit
import com.jackby03.sparky.domain.model.simulation.DcConfig
import com.jackby03.sparky.engine.model.FailureReason
import com.jackby03.sparky.engine.model.SolverResult

class DcAnalyzer {

    fun analyze(circuit: Circuit, config: DcConfig): SolverResult {
        if (!circuit.hasGround()) {
            return SolverResult.Failure(FailureReason.NO_GROUND_NODE, "Circuit has no ground node")
        }

        val system = MnaMatrixBuilder.build(circuit)

        if (system.G.isDegenerate()) {
            return SolverResult.Failure(FailureReason.SINGULAR_MATRIX, "MNA matrix is singular — check for floating nodes")
        }

        val solution = GaussianElimination.solve(system.G, system.I)
            ?: return SolverResult.Failure(FailureReason.SINGULAR_MATRIX, "System of equations has no unique solution")

        val nodeVoltages = buildNodeVoltageMap(system, solution)
        val branchCurrents = buildBranchCurrentMap(system, solution)

        return SolverResult.Success(
            nodeVoltages = nodeVoltages,
            branchCurrents = branchCurrents,
            iterationCount = 1
        )
    }

    private fun buildNodeVoltageMap(system: MnaSystem, solution: DoubleArray): Map<String, Double> {
        val result = mutableMapOf(system.groundNodeId to 0.0)
        system.nodeIndex.forEach { (nodeId, idx) -> result[nodeId] = solution[idx] }
        return result
    }

    private fun buildBranchCurrentMap(system: MnaSystem, solution: DoubleArray): Map<String, Double> =
        system.branchIndex.mapValues { (_, idx) -> solution[idx] }
}
