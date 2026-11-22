package com.jackby03.sparky.data.repository

import android.util.Log
import com.jackby03.sparky.data.local.dao.CircuitDao
import com.jackby03.sparky.data.local.entity.CircuitEntity
import com.jackby03.sparky.data.mapper.toDomain
import com.jackby03.sparky.data.mapper.toEntity
import com.jackby03.sparky.domain.model.circuit.Circuit
import com.jackby03.sparky.domain.repository.CircuitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private const val TAG = "CircuitRepository"

class CircuitRepositoryImpl @Inject constructor(
    private val dao: CircuitDao
) : CircuitRepository {

    /**
     * Deserializes a [CircuitEntity] to domain model, returning `null` if the stored
     * JSON is structurally invalid (e.g. written by a future app version or corrupted
     * on disk). Logging preserves visibility without crashing.
     */
    private fun CircuitEntity.toDomainSafe(): Circuit? =
        runCatching { toDomain() }.getOrElse { e ->
            Log.e(TAG, "Circuit '$id' ($name) has corrupt JSON — skipping. ${e.message}")
            null
        }

    override fun observeAll(): Flow<List<Circuit>> =
        dao.observeAll().map { entities -> entities.mapNotNull { it.toDomainSafe() } }

    override suspend fun getById(id: String): Circuit? =
        dao.getById(id)?.toDomainSafe()

    override suspend fun save(circuit: Circuit) =
        dao.upsert(circuit.toEntity())

    override suspend fun delete(id: String) =
        dao.deleteById(id)

    override suspend fun duplicate(id: String): Circuit? {
        val original = dao.getById(id)?.toDomain() ?: return null
        val copy = original.copy(
            id = java.util.UUID.randomUUID().toString(),
            name = "${original.name} (copy)",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        dao.upsert(copy.toEntity())
        return copy
    }
}
