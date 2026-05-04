package com.example.projectotherversion.domain.usecase.user

import com.example.projectotherversion.domain.model.User
import com.example.projectotherversion.domain.repository.ArtisanRepository
import javax.inject.Inject

class UpdateUserProfileUseCase @Inject constructor(
    private val repository: ArtisanRepository
) {
    suspend operator fun invoke(user: User): Result<Unit> {
        return repository.updateUserProfile(user)
    }
}