package com.jackby03.sparky.presentation.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jackby03.sparky.core.utils.ValueParser
import com.jackby03.sparky.domain.model.component.Capacitor
import com.jackby03.sparky.domain.model.component.CircuitComponent
import com.jackby03.sparky.domain.model.component.CurrentSource
import com.jackby03.sparky.domain.model.component.Diode
import com.jackby03.sparky.domain.model.component.Ground
import com.jackby03.sparky.domain.model.component.Inductor
import com.jackby03.sparky.domain.model.component.Resistor
import com.jackby03.sparky.domain.model.component.VoltageSource
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.roundToInt

@Composable
fun PropertyPanel(
    component: CircuitComponent,
    onLabelChange: (String) -> Unit,
    onValueChange: (String) -> Unit,
    onRotate: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var valueInput by remember(component.id) { mutableStateOf(component.primaryValueFormatted()) }

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
    ) {
        Row {
            Text(
                text  = "Properties",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onRotate) {
                Icon(Icons.AutoMirrored.Filled.RotateRight, contentDescription = "Rotate 90°")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete component",
                    tint = MaterialTheme.colorScheme.error)
            }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = component.label,
            onValueChange = onLabelChange,
            label = { Text("Label") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (component is com.jackby03.sparky.domain.model.component.SymbolComponent) {
            Text(
                text = "Schematic symbol only — electrical simulation is not implemented for this device yet.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (component !is Ground && component !is com.jackby03.sparky.domain.model.component.SymbolComponent) {
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = valueInput,
                onValueChange = { input ->
                    valueInput = input
                    onValueChange(input)
                },
                label = { Text(component.primaryValueLabel()) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Decade selector for resistors: snap between 1 Ω → 1 MΩ in decade steps
            if (component is Resistor) {
                Spacer(Modifier.height(4.dp))
                val decades = remember {
                    listOf(1.0, 10.0, 100.0, 1_000.0, 10_000.0, 100_000.0, 1_000_000.0)
                }
                val decadeIdx = remember(component.resistanceOhm) {
                    val ohm = component.resistanceOhm.coerceAtLeast(0.1)
                    decades.indices.minByOrNull { abs(log10(decades[it]) - log10(ohm)) } ?: 0
                }
                Text(
                    text  = "Decade: ${ValueParser.format(decades[decadeIdx], "Ω")}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value         = decadeIdx.toFloat(),
                    onValueChange = { v ->
                        val res = decades[v.roundToInt()]
                        val formatted = ValueParser.format(res, "Ω")
                        valueInput = formatted
                        onValueChange(formatted)
                    },
                    valueRange = 0f..6f,
                    steps      = 5,   // 7 positions total (endpoints + 5 between)
                    modifier   = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private fun CircuitComponent.primaryValueFormatted(): String = when (this) {
    is Resistor      -> ValueParser.format(resistanceOhm, "Ω")
    is Capacitor     -> ValueParser.format(capacitanceFarad, "F")
    is Inductor      -> ValueParser.format(inductanceHenry, "H")
    is VoltageSource -> ValueParser.format(voltageVolts, "V")
    is CurrentSource -> ValueParser.format(currentAmperes, "A")
    is Diode         -> ValueParser.format(forwardVoltageVolts, "V")
    is Ground        -> ""
    is com.jackby03.sparky.domain.model.component.SymbolComponent -> ""
}

private fun CircuitComponent.primaryValueLabel(): String = when (this) {
    is Resistor      -> "Resistance"
    is Capacitor     -> "Capacitance"
    is Inductor      -> "Inductance"
    is VoltageSource -> "Voltage"
    is CurrentSource -> "Current"
    is Diode         -> "Forward Voltage"
    is Ground        -> ""
    is com.jackby03.sparky.domain.model.component.SymbolComponent -> ""
}
