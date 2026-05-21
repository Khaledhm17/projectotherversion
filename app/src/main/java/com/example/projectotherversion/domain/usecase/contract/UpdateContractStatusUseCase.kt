package com.example.projectotherversion.domain.usecase.contract

import com.example.projectotherversion.domain.model.ContractStatus
import com.example.projectotherversion.domain.repository.ArtisanRepository
import javax.inject.Inject

class UpdateContractStatusUseCase @Inject constructor(
    private val repository: ArtisanRepository
) {
    suspend operator fun invoke(contractId: String, status: ContractStatus): Result<Unit> =
        repository.updateContractStatus(contractId, status)
}
