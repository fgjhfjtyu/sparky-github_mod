package com.jackby03.sparky.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jackby03.sparky.presentation.screen.editor.EditorScreen
import com.jackby03.sparky.presentation.screen.export.ExportScreen
import com.jackby03.sparky.presentation.screen.projects.ProjectsScreen
import com.jackby03.sparky.presentation.screen.settings.SettingsScreen
import com.jackby03.sparky.presentation.screen.simulation.SimulationScreen

@Composable
fun AppNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Screen.Projects.route
    ) {
        composable(Screen.Projects.route) {
            ProjectsScreen(
                onOpenCircuit = { id -> navController.navigate(Screen.Editor.buildRoute(id)) },
                onOpenSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.Editor.route,
            arguments = listOf(navArgument("circuitId") { type = NavType.StringType })
        ) { backStack ->
            val circuitId = backStack.arguments?.getString("circuitId") ?: return@composable
            EditorScreen(
                circuitId = circuitId,
                onRunSimulation = { navController.navigate(Screen.Simulation.buildRoute(circuitId)) },
                onExport = { navController.navigate(Screen.Export.buildRoute(circuitId)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Simulation.route,
            arguments = listOf(navArgument("circuitId") { type = NavType.StringType })
        ) { backStack ->
            val circuitId = backStack.arguments?.getString("circuitId") ?: return@composable
            SimulationScreen(
                circuitId = circuitId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Export.route,
            arguments = listOf(navArgument("circuitId") { type = NavType.StringType })
        ) { backStack ->
            val circuitId = backStack.arguments?.getString("circuitId") ?: return@composable
            ExportScreen(
                circuitId = circuitId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
