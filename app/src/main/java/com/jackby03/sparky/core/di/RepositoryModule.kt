package com.jackby03.sparky.core.di

import com.jackby03.sparky.data.repository.CircuitRepositoryImpl
import com.jackby03.sparky.data.repository.ProjectRepositoryImpl
import com.jackby03.sparky.domain.repository.CircuitRepository
import com.jackby03.sparky.domain.repository.ProjectRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCircuitRepository(impl: CircuitRepositoryImpl): CircuitRepository

    @Binds
    @Singleton
    abstract fun bindProjectRepository(impl: ProjectRepositoryImpl): ProjectRepository
}
