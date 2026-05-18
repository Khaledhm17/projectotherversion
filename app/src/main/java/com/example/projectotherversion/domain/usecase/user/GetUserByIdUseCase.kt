package com.example.projectotherversion.domain.usecase.user

import com.example.projectotherversion.domain.model.User
import com.example.projectotherversion.domain.repository.ArtisanRepository
import javax.inject.Inject

class GetUserByIdUseCase @Inject constructor(
    private val repository: ArtisanRepository
) {
    suspend operator fun invoke(userId: String): User? = repository.getUserById(userId)
}
