package com.example.projectotherversion.domain.usecase.contract

import com.example.projectotherversion.domain.model.Contract
import com.example.projectotherversion.domain.repository.ArtisanRepository
import javax.inject.Inject

class CreateContractUseCase @Inject constructor(
    private val repository: ArtisanRepository
) {
    suspend operator fun invoke(contract: Contract): Result<Unit> =
        repository.createContract(contract)
}
