package com.jackby03.sparky.presentation.screen.simulation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jackby03.sparky.core.result.onError
import com.jackby03.sparky.core.result.onSuccess
import com.jackby03.sparky.domain.usecase.circuit.LoadCircuitUseCase
import com.jackby03.sparky.domain.usecase.simulation.RunSimulationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SimulationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val loadCircuit: LoadCircuitUseCase,
    private val runSimulation: RunSimulationUseCase
) : ViewModel() {

    private val circuitId: String = checkNotNull(savedStateHandle["circuitId"])
    private val _state = MutableStateFlow(SimulationState())
    val state: StateFlow<SimulationState> = _state.asStateFlow()

    init {
        loadAndRun()
    }

    fun rerun() = loadAndRun()

    fun dismissError() = _state.update { it.copy(error = null) }

    private fun loadAndRun() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, isRunning = false, result = null) }
            loadCircuit(circuitId)
                .onSuccess { circuit ->
                    _state.update { it.copy(circuit = circuit, isLoading = false, isRunning = true) }
                    runSimulation(circuit, _state.value.config)
                        .onSuccess { result -> _state.update { it.copy(result = result, isRunning = false) } }
                        .onError { e -> _state.update { it.copy(error = e.message, isRunning = false) } }
                }
                .onError { e -> _state.update { it.copy(error = e.message, isLoading = false) } }
        }
    }
}
