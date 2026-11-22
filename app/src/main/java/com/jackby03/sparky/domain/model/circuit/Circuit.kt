package com.jackby03.sparky.domain.model.circuit

import com.jackby03.sparky.domain.model.component.CircuitComponent
import java.util.UUID

data class Circuit(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val components: List<CircuitComponent> = emptyList(),
    val nodes: List<Node> = emptyList(),
    val wires: List<Wire> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun hasGround(): Boolean = nodes.any { it.isGround }

    fun nodeById(id: String): Node? = nodes.find { it.id == id }

    fun componentById(id: String): CircuitComponent? = components.find { it.id == id }
}
