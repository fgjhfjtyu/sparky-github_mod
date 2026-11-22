package com.jackby03.sparky.presentation.screen.simulation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jackby03.sparky.core.extensions.toEngineeringString
import com.jackby03.sparky.domain.model.simulation.SimulationResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulationScreen(
    circuitId: String,
    onBack: () -> Unit,
    viewModel: SimulationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Simulation") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::rerun) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Re-run simulation")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading || state.isRunning ->
                    Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Text("Running simulation…", style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp))
                    }
                state.error != null ->
                    Text(state.error!!, color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center))
                state.result != null ->
                    ResultPanel(result = state.result!!)
            }
        }
    }
}

@Composable
private fun ResultPanel(result: SimulationResult) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Text("Node Voltages", style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary)
        }
        items(result.nodeVoltages.entries.toList()) { (nodeId, voltage) ->
            ResultRow(label = nodeId, value = "${voltage.toEngineeringString()}V")
        }
        item {
            Text("Branch Currents", style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(top = 8.dp))
        }
        items(result.branchCurrents.entries.toList()) { (compId, current) ->
            ResultRow(label = compId, value = "${current.toEngineeringString()}A")
        }
        item {
            Text(
                text = "Converged in ${result.iterationCount} iteration(s) · ${result.elapsedMs} ms",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(value, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary)
        }
    }
}
