package com.example.projectotherversion.domain.usecase.post

import com.example.projectotherversion.domain.repository.ArtisanRepository
import javax.inject.Inject

class DeletePostUseCase @Inject constructor(
    private val repository: ArtisanRepository
) {
    suspend operator fun invoke(postId: String): Result<Unit> = repository.deletePost(postId)
}