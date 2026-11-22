package com.jackby03.sparky.domain.model.simulation

import com.jackby03.sparky.core.constants.AppConstants.Simulation

data class SimulationConfig(
    val type: SimulationType = SimulationType.DC,
    val dcConfig: DcConfig = DcConfig(),
    val acConfig: AcConfig = AcConfig(),
    val transientConfig: TransientConfig = TransientConfig()
)

enum class SimulationType(val displayName: String) {
    DC("DC Analysis"),
    AC("AC Analysis"),
    TRANSIENT("Transient Analysis")
}

data class DcConfig(
    val maxIterations: Int = Simulation.MAX_ITERATIONS,
    val convergenceThreshold: Double = Simulation.CONVERGENCE_THRESHOLD
)

data class AcConfig(
    val startFrequencyHz: Double = 1.0,
    val stopFrequencyHz: Double = 1e6,
    val pointsPerDecade: Int = 10
)

data class TransientConfig(
    val stopTimeSeconds: Double = 1e-3,
    val timeStepSeconds: Double = Simulation.DEFAULT_TIME_STEP_S
)
