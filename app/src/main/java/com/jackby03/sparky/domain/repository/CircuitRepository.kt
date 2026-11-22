package com.jackby03.sparky.domain.repository

import com.jackby03.sparky.domain.model.circuit.Circuit
import kotlinx.coroutines.flow.Flow

interface CircuitRepository {
    fun observeAll(): Flow<List<Circuit>>
    suspend fun getById(id: String): Circuit?
    suspend fun save(circuit: Circuit)
    suspend fun delete(id: String)
    suspend fun duplicate(id: String): Circuit?
}
