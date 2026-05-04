package com.example.projectotherversion.di

import com.example.projectotherversion.domain.repository.ArtisanRepository
import com.example.projectotherversion.domain.usecase.auth.*
import com.example.projectotherversion.domain.usecase.post.*
import com.example.projectotherversion.domain.usecase.message.*
import com.example.projectotherversion.domain.usecase.user.*
import com.example.projectotherversion.domain.usecase.complaint.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {

    // Auth
    @Provides
    @ViewModelScoped
    fun provideLoginUseCase(repository: ArtisanRepository) = LoginUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideRegisterUseCase(repository: ArtisanRepository) = RegisterUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideLogoutUseCase(repository: ArtisanRepository) = LogoutUseCase(repository)

    // Posts
    @Provides
    @ViewModelScoped
    fun provideCreatePostUseCase(repository: ArtisanRepository) = CreatePostUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideGetAllPostsUseCase(repository: ArtisanRepository) = GetAllPostsUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideDeletePostUseCase(repository: ArtisanRepository) = DeletePostUseCase(repository)

    // Messages
    @Provides
    @ViewModelScoped
    fun provideSendMessageUseCase(repository: ArtisanRepository) = SendMessageUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideGetMessagesBetweenUseCase(repository: ArtisanRepository) = GetMessagesBetweenUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideGetNotificationsCountUseCase(repository: ArtisanRepository) = GetNotificationsCountUseCase(repository)

    // User
    @Provides
    @ViewModelScoped
    fun provideGetCurrentUserUseCase(repository: ArtisanRepository) = GetCurrentUserUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideGetAllUsersUseCase(repository: ArtisanRepository) = GetAllUsersUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideBlockUserUseCase(repository: ArtisanRepository) = BlockUserUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideGetClientsWhoContactedUseCase(repository: ArtisanRepository) = GetClientsWhoContactedUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideUpdateUserProfileUseCase(repository: ArtisanRepository) = UpdateUserProfileUseCase(repository)

    // Complaints
    @Provides
    @ViewModelScoped
    fun provideSubmitComplaintUseCase(repository: ArtisanRepository) = SubmitComplaintUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideGetAllComplaintsUseCase(repository: ArtisanRepository) = GetAllComplaintsUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideDeleteComplaintUseCase(repository: ArtisanRepository) = DeleteComplaintUseCase(repository)
}