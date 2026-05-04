package com.example.projectotherversion.domain.usecase.auth

import com.example.projectotherversion.domain.repository.ArtisanRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val repository: ArtisanRepository
) {
    suspend operator fun invoke() = repository.logout()
}