package com.jackby03.sparky.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "circuits",
    indices = [Index(value = ["updatedAt"])]
)
data class CircuitEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val componentsJson: String,
    val nodesJson: String,
    val wiresJson: String,
    val createdAt: Long,
    val updatedAt: Long
)
