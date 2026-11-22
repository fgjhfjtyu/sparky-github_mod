package com.jackby03.sparky.presentation.navigation

sealed class Screen(val route: String) {
    data object Projects   : Screen("projects")
    data object Settings   : Screen("settings")
    data object Editor     : Screen("editor/{circuitId}") {
        fun buildRoute(circuitId: String) = "editor/$circuitId"
    }
    data object Simulation : Screen("simulation/{circuitId}") {
        fun buildRoute(circuitId: String) = "simulation/$circuitId"
    }
    data object Export     : Screen("export/{circuitId}") {
        fun buildRoute(circuitId: String) = "export/$circuitId"
    }
}
