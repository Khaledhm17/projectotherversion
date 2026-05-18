package com.example.projectotherversion.domain.repository

import android.net.Uri
import com.example.projectotherversion.domain.model.*
import kotlinx.coroutines.flow.Flow

interface ArtisanRepository {
    // Auth
    suspend fun login(email: String, password: String): Result<User?>
    suspend fun register(email: String, password: String, user: User): Result<User?>
    suspend fun logout()
    suspend fun getCurrentUser(): User?

    // User
    suspend fun updateUserProfile(user: User): Result<Unit>
    suspend fun deleteAccount(): Result<Unit>
    fun getAllUsers(): Flow<List<User>>
    suspend fun blockUser(userId: String, blocked: Boolean): Result<Unit>
    fun getClientsWhoContacted(artisanId: String): Flow<List<User>>
    suspend fun getUserById(userId: String): User?

    // Posts
    suspend fun createPost(post: Post, imageUri: Uri?): Result<Unit>
    suspend fun deletePost(postId: String): Result<Unit>
    fun getAllPosts(): Flow<List<Post>>

    // Messages
    suspend fun sendMessage(message: Message): Result<Unit>
    fun getMessagesBetween(user1: String, user2: String): Flow<List<Message>>
    fun getNotificationsCount(userId: String): Flow<Int>

    // Complaints
    suspend fun submitComplaint(complaint: Complaint): Result<Unit>
    fun getAllComplaints(): Flow<List<Complaint>>
    suspend fun deleteComplaint(complaintId: String): Result<Unit>

    // Rating
    suspend fun submitRating(rating: Rating): Result<Unit>
}