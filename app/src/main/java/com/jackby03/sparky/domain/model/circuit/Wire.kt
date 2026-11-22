package com.jackby03.sparky.domain.model.circuit

import java.util.UUID
import kotlinx.serialization.Serializable

@Serializable
data class Wire(
    val id: String = UUID.randomUUID().toString(),
    val startNodeId: String,
    val endNodeId: String,
    val waypoints: List<Waypoint> = emptyList()
)

@Serializable
data class Waypoint(
    val x: Float,
    val y: Float
)
