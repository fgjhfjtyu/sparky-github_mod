package com.jackby03.sparky.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jackby03.sparky.data.local.entity.CircuitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CircuitDao {

    @Query("SELECT * FROM circuits ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<CircuitEntity>>

    @Query("SELECT * FROM circuits WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): CircuitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CircuitEntity)

    @Query("DELETE FROM circuits WHERE id = :id")
    suspend fun deleteById(id: String)
}
