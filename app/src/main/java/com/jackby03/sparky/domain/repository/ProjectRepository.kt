package com.jackby03.sparky.domain.repository

import com.jackby03.sparky.domain.model.project.Project
import kotlinx.coroutines.flow.Flow

interface ProjectRepository {
    fun observeAll(): Flow<List<Project>>
    suspend fun getById(id: String): Project?
    suspend fun save(project: Project)
    suspend fun delete(id: String)
}
