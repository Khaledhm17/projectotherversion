package com.example.projectotherversion.domain.usecase.message

import com.example.projectotherversion.domain.repository.ArtisanRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNotificationsCountUseCase @Inject constructor(
    private val repository: ArtisanRepository
) {
    operator fun invoke(userId: String): Flow<Int> =
        repository.getNotificationsCount(userId)
}