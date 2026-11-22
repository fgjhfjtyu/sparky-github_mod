package com.jackby03.sparky.engine.validator

import com.jackby03.sparky.domain.model.circuit.Circuit
import com.jackby03.sparky.domain.model.component.Resistor
import com.jackby03.sparky.domain.model.component.VoltageSource
import com.jackby03.sparky.domain.model.component.CurrentSource
import javax.inject.Inject

class CircuitValidator @Inject constructor() {

    fun validate(circuit: Circuit): List<ValidationError> = buildList {
        if (circuit.components.isEmpty()) add(ValidationError.NoComponents)
        if (!circuit.hasGround()) add(ValidationError.NoGround)

        circuit.components.forEach { component ->
            component.terminalNodeIds.forEachIndexed { index, nodeId ->
                if (nodeId.isBlank()) {
                    add(ValidationError.UnconnectedTerminal(component.label, index))
                }
            }
        }

        circuit.components.forEach { component ->
            when (component) {
                is Resistor ->
                    if (component.resistanceOhm <= 0)
                        add(ValidationError.InvalidValue(component.label, "resistance must be > 0"))
                is VoltageSource ->
                    if (component.voltageVolts.isNaN())
                        add(ValidationError.InvalidValue(component.label, "voltage is NaN"))
                is CurrentSource ->
                    if (component.currentAmperes.isNaN())
                        add(ValidationError.InvalidValue(component.label, "current is NaN"))
                else -> Unit
            }
        }
    }
}
