package com.example.projectotherversion.domain.usecase.complaint

import com.example.projectotherversion.domain.model.Complaint
import com.example.projectotherversion.domain.repository.ArtisanRepository
import javax.inject.Inject

class SubmitComplaintUseCase @Inject constructor(
    private val repository: ArtisanRepository
) {
    suspend operator fun invoke(complaint: Complaint): Result<Unit> =
        repository.submitComplaint(complaint)
}