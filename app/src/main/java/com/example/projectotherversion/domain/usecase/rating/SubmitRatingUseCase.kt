package com.example.projectotherversion.domain.usecase.rating

import com.example.projectotherversion.domain.model.Rating
import com.example.projectotherversion.domain.repository.ArtisanRepository
import javax.inject.Inject

class SubmitRatingUseCase @Inject constructor(
    private val repository: ArtisanRepository
) {
    suspend operator fun invoke(rating: Rating): Result<Unit> = repository.submitRating(rating)
}
