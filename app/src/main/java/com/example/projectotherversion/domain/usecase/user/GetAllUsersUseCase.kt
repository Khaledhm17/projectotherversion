package com.example.projectotherversion.domain.usecase.user

import com.example.projectotherversion.domain.model.User
import com.example.projectotherversion.domain.repository.ArtisanRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllUsersUseCase @Inject constructor(
    private val repository: ArtisanRepository
) {
    operator fun invoke(): Flow<List<User>> = repository.getAllUsers()
}