package com.example.projectotherversion.domain.usecase.message

import com.example.projectotherversion.domain.model.Message
import com.example.projectotherversion.domain.repository.ArtisanRepository
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val repository: ArtisanRepository
) {
    suspend operator fun invoke(message: Message): Result<Unit> = repository.sendMessage(message)
}