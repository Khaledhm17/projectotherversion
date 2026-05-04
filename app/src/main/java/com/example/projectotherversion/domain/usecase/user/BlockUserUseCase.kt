package com.example.projectotherversion.domain.usecase.user

import com.example.projectotherversion.domain.repository.ArtisanRepository
import javax.inject.Inject

class BlockUserUseCase @Inject constructor(
    private val repository: ArtisanRepository
) {
    suspend operator fun invoke(userId: String, blocked: Boolean): Result<Unit> =
        repository.blockUser(userId, blocked)
}