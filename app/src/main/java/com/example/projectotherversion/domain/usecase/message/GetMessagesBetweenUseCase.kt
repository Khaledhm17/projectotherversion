package com.example.projectotherversion.domain.usecase.message

import com.example.projectotherversion.domain.model.Message
import com.example.projectotherversion.domain.repository.ArtisanRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMessagesBetweenUseCase @Inject constructor(
    private val repository: ArtisanRepository
) {
    operator fun invoke(user1: String, user2: String): Flow<List<Message>> =
        repository.getMessagesBetween(user1, user2)
}