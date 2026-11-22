package com.jackby03.sparky.domain.model.simulation

data class SimulationResult(
    val type: SimulationType,
    val nodeVoltages: Map<String, Double>,
    val branchCurrents: Map<String, Double>,
    val isConverged: Boolean,
    val iterationCount: Int,
    val elapsedMs: Long,
    val timestampMs: Long = System.currentTimeMillis()
) {
    val voltageAt: (nodeId: String) -> Double? = { nodeVoltages[it] }
    val currentThrough: (componentId: String) -> Double? = { branchCurrents[it] }
}
