package com.jackby03.sparky.domain.model.component

import com.jackby03.sparky.core.constants.AppConstants.Defaults
import java.util.UUID

sealed class CircuitComponent {
    abstract val id: String
    abstract val label: String
    abstract val x: Float
    abstract val y: Float
    abstract val rotation: Float
    abstract val terminalNodeIds: List<String>

    fun withPosition(x: Float, y: Float): CircuitComponent = when (this) {
        is Resistor      -> copy(x = x, y = y)
        is Capacitor     -> copy(x = x, y = y)
        is Inductor      -> copy(x = x, y = y)
        is VoltageSource -> copy(x = x, y = y)
        is CurrentSource -> copy(x = x, y = y)
        is Diode         -> copy(x = x, y = y)
        is Ground        -> copy(x = x, y = y)
        is SymbolComponent -> copy(x = x, y = y)
    }

    fun withRotation(rotation: Float): CircuitComponent = when (this) {
        is Resistor      -> copy(rotation = rotation)
        is Capacitor     -> copy(rotation = rotation)
        is Inductor      -> copy(rotation = rotation)
        is VoltageSource -> copy(rotation = rotation)
        is CurrentSource -> copy(rotation = rotation)
        is Diode         -> copy(rotation = rotation)
        is Ground        -> copy(rotation = rotation)
        is SymbolComponent -> copy(rotation = rotation)
    }

    fun withLabel(label: String): CircuitComponent = when (this) {
        is Resistor      -> copy(label = label)
        is Capacitor     -> copy(label = label)
        is Inductor      -> copy(label = label)
        is VoltageSource -> copy(label = label)
        is CurrentSource -> copy(label = label)
        is Diode         -> copy(label = label)
        is Ground        -> copy(label = label)
        is SymbolComponent -> copy(label = label)
    }
}

data class Resistor(
    override val id: String = UUID.randomUUID().toString(),
    override val label: String = "R1",
    override val x: Float = 0f,
    override val y: Float = 0f,
    override val rotation: Float = 0f,
    override val terminalNodeIds: List<String> = listOf("", ""),
    val resistanceOhm: Double = Defaults.RESISTANCE_OHM
) : CircuitComponent()

data class Capacitor(
    override val id: String = UUID.randomUUID().toString(),
    override val label: String = "C1",
    override val x: Float = 0f,
    override val y: Float = 0f,
    override val rotation: Float = 0f,
    override val terminalNodeIds: List<String> = listOf("", ""),
    val capacitanceFarad: Double = Defaults.CAPACITANCE_FARAD
) : CircuitComponent()

data class Inductor(
    override val id: String = UUID.randomUUID().toString(),
    override val label: String = "L1",
    override val x: Float = 0f,
    override val y: Float = 0f,
    override val rotation: Float = 0f,
    override val terminalNodeIds: List<String> = listOf("", ""),
    val inductanceHenry: Double = Defaults.INDUCTANCE_HENRY
) : CircuitComponent()

data class VoltageSource(
    override val id: String = UUID.randomUUID().toString(),
    override val label: String = "V1",
    override val x: Float = 0f,
    override val y: Float = 0f,
    override val rotation: Float = 0f,
    override val terminalNodeIds: List<String> = listOf("", ""),
    val voltageVolts: Double = Defaults.VOLTAGE_VOLTS
) : CircuitComponent()

data class CurrentSource(
    override val id: String = UUID.randomUUID().toString(),
    override val label: String = "I1",
    override val x: Float = 0f,
    override val y: Float = 0f,
    override val rotation: Float = 0f,
    override val terminalNodeIds: List<String> = listOf("", ""),
    val currentAmperes: Double = Defaults.CURRENT_AMPERES
) : CircuitComponent()

data class Diode(
    override val id: String = UUID.randomUUID().toString(),
    override val label: String = "D1",
    override val x: Float = 0f,
    override val y: Float = 0f,
    override val rotation: Float = 0f,
    override val terminalNodeIds: List<String> = listOf("", ""),
    val forwardVoltageVolts: Double = Defaults.FORWARD_VOLTAGE_V,
    val saturationCurrentAmperes: Double = 1e-14
) : CircuitComponent()

data class Ground(
    override val id: String = UUID.randomUUID().toString(),
    override val label: String = "GND",
    override val x: Float = 0f,
    override val y: Float = 0f,
    override val rotation: Float = 0f,
    override val terminalNodeIds: List<String> = listOf("")
) : CircuitComponent()

/** A schematic-only IEC-style placeholder for catalog devices not yet backed by a physical solver model. */
data class SymbolComponent(
    val componentType: String,
    val symbol: String,
    override val id: String = UUID.randomUUID().toString(),
    override val label: String = symbol + "1",
    override val x: Float = 0f,
    override val y: Float = 0f,
    override val rotation: Float = 0f,
    override val terminalNodeIds: List<String> = listOf("", "")
) : CircuitComponent()
