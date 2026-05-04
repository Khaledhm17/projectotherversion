package com.example.projectotherversion.domain.usecase.complaint

import com.example.projectotherversion.domain.repository.ArtisanRepository
import javax.inject.Inject

class DeleteComplaintUseCase @Inject constructor(
    private val repository: ArtisanRepository
) {
    suspend operator fun invoke(complaintId: String): Result<Unit> =
        repository.deleteComplaint(complaintId)
}