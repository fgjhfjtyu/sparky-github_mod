package com.jackby03.sparky.presentation.screen.simulation

import com.jackby03.sparky.domain.model.circuit.Circuit
import com.jackby03.sparky.domain.model.simulation.SimulationConfig
import com.jackby03.sparky.domain.model.simulation.SimulationResult

data class SimulationState(
    val circuit: Circuit? = null,
    val config: SimulationConfig = SimulationConfig(),
    val result: SimulationResult? = null,
    val isRunning: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)
