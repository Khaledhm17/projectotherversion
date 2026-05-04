package com.example.projectotherversion.domain.usecase.auth

import com.example.projectotherversion.domain.model.User
import com.example.projectotherversion.domain.repository.ArtisanRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val repository: ArtisanRepository
) {
    suspend operator fun invoke(email: String, password: String, user: User): Result<User?> {
        return repository.register(email, password, user)
    }
}