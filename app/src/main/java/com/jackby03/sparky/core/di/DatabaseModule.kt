package com.jackby03.sparky.core.di

import android.content.Context
import androidx.room.Room
import com.jackby03.sparky.core.constants.AppConstants
import com.jackby03.sparky.data.local.dao.CircuitDao
import com.jackby03.sparky.data.local.dao.ProjectDao
import com.jackby03.sparky.data.local.database.SparkDatabase
import com.jackby03.sparky.data.local.database.SparkDatabase.Companion.MIGRATION_1_2
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SparkDatabase =
        Room.databaseBuilder(context, SparkDatabase::class.java, AppConstants.DATABASE_NAME)
            .addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigration(false)
            .build()

    @Provides
    fun provideCircuitDao(db: SparkDatabase): CircuitDao = db.circuitDao()

    @Provides
    fun provideProjectDao(db: SparkDatabase): ProjectDao = db.projectDao()
}
