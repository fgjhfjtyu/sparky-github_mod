package com.jackby03.sparky.data.mapper

import com.jackby03.sparky.domain.model.component.CircuitComponent
import com.jackby03.sparky.domain.model.component.Capacitor
import com.jackby03.sparky.domain.model.component.CurrentSource
import com.jackby03.sparky.domain.model.component.Diode
import com.jackby03.sparky.domain.model.component.Ground
import com.jackby03.sparky.domain.model.component.Inductor
import com.jackby03.sparky.domain.model.component.Resistor
import com.jackby03.sparky.domain.model.component.VoltageSource
import com.jackby03.sparky.domain.model.component.SymbolComponent
import kotlinx.serialization.Serializable

@Serializable
data class SerializableComponent(
    val type: String,
    val id: String,
    val label: String,
    val x: Float,
    val y: Float,
    val rotation: Float,
    val terminalNodeIds: List<String>,
    val value1: Double = 0.0,
    val value2: Double = 0.0
)

fun CircuitComponent.toSerializable(): SerializableComponent = SerializableComponent(
    type = when (this) {
        is Resistor      -> "RESISTOR"
        is Capacitor     -> "CAPACITOR"
        is Inductor      -> "INDUCTOR"
        is VoltageSource -> "VOLTAGE_SOURCE"
        is CurrentSource -> "CURRENT_SOURCE"
        is Diode         -> "DIODE"
        is Ground        -> "GROUND"
        is SymbolComponent -> "SYMBOL:${this.componentType}:${this.symbol}"
    },
    id = id, label = label, x = x, y = y, rotation = rotation,
    terminalNodeIds = terminalNodeIds,
    value1 = when (this) {
        is Resistor      -> resistanceOhm
        is Capacitor     -> capacitanceFarad
        is Inductor      -> inductanceHenry
        is VoltageSource -> voltageVolts
        is CurrentSource -> currentAmperes
        is Diode         -> forwardVoltageVolts
        is Ground        -> 0.0
        is SymbolComponent -> 0.0
    },
    value2 = if (this is Diode) saturationCurrentAmperes else 0.0
)

fun SerializableComponent.toDomain(): CircuitComponent = when (type) {
    "RESISTOR"       -> Resistor(id, label, x, y, rotation, terminalNodeIds, value1)
    "CAPACITOR"      -> Capacitor(id, label, x, y, rotation, terminalNodeIds, value1)
    "INDUCTOR"       -> Inductor(id, label, x, y, rotation, terminalNodeIds, value1)
    "VOLTAGE_SOURCE" -> VoltageSource(id, label, x, y, rotation, terminalNodeIds, value1)
    "CURRENT_SOURCE" -> CurrentSource(id, label, x, y, rotation, terminalNodeIds, value1)
    "DIODE"          -> Diode(id, label, x, y, rotation, terminalNodeIds, value1, value2)
    else -> if (type.startsWith("SYMBOL:")) {
        val parts = type.split(":", limit = 3)
        SymbolComponent(parts.getOrElse(1) { "UNKNOWN" }, parts.getOrElse(2) { "SYM" }, id, label, x, y, rotation, terminalNodeIds)
    } else Ground(id, label, x, y, rotation, terminalNodeIds)
}
