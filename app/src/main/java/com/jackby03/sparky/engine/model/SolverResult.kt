package com.jackby03.sparky.engine.model

sealed class SolverResult {
    data class Success(
        val nodeVoltages: Map<String, Double>,
        val branchCurrents: Map<String, Double>,
        val iterationCount: Int
    ) : SolverResult()

    data class Failure(
        val reason: FailureReason,
        val message: String
    ) : SolverResult()
}

enum class FailureReason {
    NO_GROUND_NODE,
    SINGULAR_MATRIX,
    NOT_CONVERGED,
    UNSUPPORTED_ANALYSIS
}
