package com.example.projectotherversion.di

import com.example.projectotherversion.data.repository.ArtisanRepositoryImpl
import com.example.projectotherversion.domain.repository.ArtisanRepository
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
    abstract fun bindArtisanRepository(
        impl: ArtisanRepositoryImpl
    ): ArtisanRepository
}