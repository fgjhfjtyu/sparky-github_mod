package com.jackby03.sparky.presentation.screen.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jackby03.sparky.core.result.onError
import com.jackby03.sparky.domain.repository.CircuitRepository
import com.jackby03.sparky.domain.usecase.circuit.CreateCircuitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProjectsViewModel @Inject constructor(
    private val circuitRepository: CircuitRepository,
    private val createCircuit: CreateCircuitUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProjectsState())
    val state: StateFlow<ProjectsState> = _state.asStateFlow()

    init {
        observeCircuits()
    }

    fun onCreateCircuit(name: String) {
        viewModelScope.launch {
            createCircuit(name).onError { e ->
                _state.update { it.copy(error = e.message) }
            }
        }
        _state.update { it.copy(showCreateDialog = false) }
    }

    fun onDeleteCircuit(circuitId: String) {
        viewModelScope.launch {
            circuitRepository.delete(circuitId)
        }
    }

    fun showCreateDialog() = _state.update { it.copy(showCreateDialog = true) }

    fun dismissCreateDialog() = _state.update { it.copy(showCreateDialog = false) }

    fun dismissError() = _state.update { it.copy(error = null) }

    private fun observeCircuits() {
        circuitRepository.observeAll()
            .onEach { circuits -> _state.update { it.copy(circuits = circuits, isLoading = false) } }
            .catch { e -> _state.update { it.copy(error = e.message, isLoading = false) } }
            .launchIn(viewModelScope)
    }
}
