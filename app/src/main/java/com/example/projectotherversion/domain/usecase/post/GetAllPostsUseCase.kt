package com.example.projectotherversion.domain.usecase.post

import com.example.projectotherversion.domain.model.Post
import com.example.projectotherversion.domain.repository.ArtisanRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllPostsUseCase @Inject constructor(
    private val repository: ArtisanRepository
) {
    operator fun invoke(): Flow<List<Post>> = repository.getAllPosts()
}