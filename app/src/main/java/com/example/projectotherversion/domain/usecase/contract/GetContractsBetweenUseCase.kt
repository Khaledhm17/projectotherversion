package com.example.projectotherversion.domain.usecase.contract

import com.example.projectotherversion.domain.model.Contract
import com.example.projectotherversion.domain.repository.ArtisanRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetContractsBetweenUseCase @Inject constructor(
    private val repository: ArtisanRepository
) {
    operator fun invoke(user1: String, user2: String): Flow<List<Contract>> =
        repository.getContractsBetween(user1, user2)
}
