package com.example.projectotherversion.domain.usecase.user

import com.example.projectotherversion.domain.model.User
import com.example.projectotherversion.domain.repository.ArtisanRepository
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val repository: ArtisanRepository
) {
    suspend operator fun invoke(): User? = repository.getCurrentUser()
}