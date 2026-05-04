package com.example.projectotherversion.domain.usecase.post

import android.net.Uri
import com.example.projectotherversion.domain.model.Post
import com.example.projectotherversion.domain.repository.ArtisanRepository
import javax.inject.Inject

class CreatePostUseCase @Inject constructor(
    private val repository: ArtisanRepository
) {
    suspend operator fun invoke(post: Post, imageUri: Uri?): Result<Unit> {
        return repository.createPost(post, imageUri)
    }
}