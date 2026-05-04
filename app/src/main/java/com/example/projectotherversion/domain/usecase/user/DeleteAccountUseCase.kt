package com.example.projectotherversion.domain.usecase.user

import com.example.projectotherversion.domain.repository.ArtisanRepository
import javax.inject.Inject

class DeleteAccountUseCase @Inject constructor(
    private val repository: ArtisanRepository
) {
    suspend operator fun invoke(): Result<Unit> = repository.deleteAccount()
}