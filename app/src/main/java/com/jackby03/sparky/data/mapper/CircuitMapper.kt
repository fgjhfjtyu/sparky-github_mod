package com.jackby03.sparky.data.mapper

import com.jackby03.sparky.data.local.entity.CircuitEntity
import com.jackby03.sparky.domain.model.circuit.Circuit
import com.jackby03.sparky.domain.model.circuit.Node
import com.jackby03.sparky.domain.model.circuit.Wire
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

fun Circuit.toEntity(): CircuitEntity = CircuitEntity(
    id = id,
    name = name,
    description = description,
    componentsJson = json.encodeToString(components.map { it.toSerializable() }),
    nodesJson = json.encodeToString(nodes),
    wiresJson = json.encodeToString(wires),
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun CircuitEntity.toDomain(): Circuit = Circuit(
    id = id,
    name = name,
    description = description,
    components = json.decodeFromString<List<SerializableComponent>>(componentsJson).map { it.toDomain() },
    nodes = json.decodeFromString(nodesJson),
    wires = json.decodeFromString(wiresJson),
    createdAt = createdAt,
    updatedAt = updatedAt
)
