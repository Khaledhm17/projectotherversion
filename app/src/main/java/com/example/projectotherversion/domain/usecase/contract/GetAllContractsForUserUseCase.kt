package com.example.projectotherversion.domain.usecase.contract

import com.example.projectotherversion.domain.model.Contract
import com.example.projectotherversion.domain.repository.ArtisanRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllContractsForUserUseCase @Inject constructor(
    private val repository: ArtisanRepository
) {
    operator fun invoke(userId: String): Flow<List<Contract>> =
        repository.getAllContractsForUser(userId)
}
