package com.jackby03.sparky.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val circuitIdsJson: String,
    val createdAt: Long,
    val updatedAt: Long
)
