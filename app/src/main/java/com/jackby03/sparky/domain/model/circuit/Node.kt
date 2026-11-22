package com.jackby03.sparky.domain.model.circuit

import java.util.UUID
import kotlinx.serialization.Serializable

@Serializable
data class Node(
    val id: String = UUID.randomUUID().toString(),
    val x: Float,
    val y: Float,
    val label: String = "",
    val isGround: Boolean = false
)
