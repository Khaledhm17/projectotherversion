package com.example.projectotherversion.domain.usecase.complaint

import com.example.projectotherversion.domain.model.Complaint
import com.example.projectotherversion.domain.repository.ArtisanRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllComplaintsUseCase @Inject constructor(
    private val repository: ArtisanRepository
) {
    operator fun invoke(): Flow<List<Complaint>> = repository.getAllComplaints()
}