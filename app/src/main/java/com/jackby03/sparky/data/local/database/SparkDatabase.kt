package com.jackby03.sparky.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jackby03.sparky.data.local.dao.CircuitDao
import com.jackby03.sparky.data.local.dao.ProjectDao
import com.jackby03.sparky.data.local.entity.CircuitEntity
import com.jackby03.sparky.data.local.entity.ProjectEntity

@Database(
    entities = [CircuitEntity::class, ProjectEntity::class],
    version = 2,
    exportSchema = true
)
abstract class SparkDatabase : RoomDatabase() {
    abstract fun circuitDao(): CircuitDao
    abstract fun projectDao(): ProjectDao

    companion object {
        /** Adds index on circuits.updatedAt for faster ORDER BY DESC queries. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS index_circuits_updatedAt ON circuits (updatedAt)")
            }
        }
    }
}
