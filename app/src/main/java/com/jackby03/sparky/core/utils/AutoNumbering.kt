package com.jackby03.sparky.core.utils

import com.jackby03.sparky.domain.model.component.CircuitComponent
import com.jackby03.sparky.domain.model.component.ComponentType

/**
 * Generates the next available label for a component type (e.g. R1, R2, C1).
 *
 * Scans existing component labels and finds the next unused integer suffix.
 */
object AutoNumbering {

    fun nextLabel(type: ComponentType, existing: List<CircuitComponent>): String {
        val prefix = type.symbol
        val usedNumbers = existing
            .map { it.label }
            .filter { it.startsWith(prefix) }
            .mapNotNull { it.removePrefix(prefix).toIntOrNull() }
            .toSet()

        var n = 1
        while (n in usedNumbers) n++
        return "$prefix$n"
    }
}
