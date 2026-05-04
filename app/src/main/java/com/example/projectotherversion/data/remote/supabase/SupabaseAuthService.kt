package com.example.projectotherversion.data.remote.supabase

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.builtin.Email
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseAuthService @Inject constructor(
    private val auth: Auth
) {
    suspend fun login(email: String, password: String): Result<Unit> = try {
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun register(email: String, password: String): Result<Unit> = try {
        auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun getCurrentUserId(): String? = auth.currentUserOrNull()?.id

    suspend fun logout() = auth.signOut()
}
