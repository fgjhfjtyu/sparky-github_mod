package com.jackby03.sparky.data.repository

import com.jackby03.sparky.data.local.dao.ProjectDao
import com.jackby03.sparky.data.local.entity.ProjectEntity
import com.jackby03.sparky.domain.model.project.Project
import com.jackby03.sparky.domain.repository.ProjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProjectRepositoryImpl @Inject constructor(
    private val dao: ProjectDao
) : ProjectRepository {

    override fun observeAll(): Flow<List<Project>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getById(id: String): Project? =
        dao.getById(id)?.toDomain()

    override suspend fun save(project: Project) =
        dao.upsert(project.toEntity())

    override suspend fun delete(id: String) =
        dao.deleteById(id)

    private fun ProjectEntity.toDomain() = Project(
        id          = id,
        name        = name,
        description = description,
        circuitIds  = circuitIdsJson.split(",").filter { it.isNotBlank() },
        createdAt   = createdAt,
        updatedAt   = updatedAt
    )

    private fun Project.toEntity() = ProjectEntity(
        id            = id,
        name          = name,
        description   = description,
        circuitIdsJson = circuitIds.joinToString(","),
        createdAt     = createdAt,
        updatedAt     = updatedAt
    )
}
