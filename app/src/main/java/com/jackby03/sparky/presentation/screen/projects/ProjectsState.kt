package com.jackby03.sparky.presentation.screen.projects

import com.jackby03.sparky.domain.model.circuit.Circuit

data class ProjectsState(
    val circuits: List<Circuit> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val showCreateDialog: Boolean = false
)
