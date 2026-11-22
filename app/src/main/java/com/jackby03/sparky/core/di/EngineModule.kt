package com.jackby03.sparky.core.di

import com.jackby03.sparky.engine.solver.DcAnalyzer
import com.jackby03.sparky.engine.validator.CircuitValidator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EngineModule {

    @Provides
    @Singleton
    fun provideDcAnalyzer(): DcAnalyzer = DcAnalyzer()

    @Provides
    @Singleton
    fun provideCircuitValidator(): CircuitValidator = CircuitValidator()
}
