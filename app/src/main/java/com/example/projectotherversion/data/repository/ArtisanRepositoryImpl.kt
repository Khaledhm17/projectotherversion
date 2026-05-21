package com.example.projectotherversion.data.repository

import android.net.Uri
import android.util.Log
import com.example.projectotherversion.data.remote.dto.*
import com.example.projectotherversion.data.remote.supabase.*
import com.example.projectotherversion.domain.model.*
import com.example.projectotherversion.domain.repository.ArtisanRepository
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.*
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Singleton
class ArtisanRepositoryImpl @Inject constructor(
    private val authService: SupabaseAuthService,
    private val dbService: SupabaseDbService,
    private val storageService: SupabaseStorageService
) : ArtisanRepository {

    private fun String?.toFullUrl(bucket: String): String? {
        if (this.isNullOrBlank()) return null
        if (this.startsWith("http")) return this
        return "https://uitignjqzoswptvukdoh.supabase.co/storage/v1/object/public/$bucket/$this"
    }

    private fun UserDto.toDomain(): User = User(
        id = id ?: "",
        name = name ?: "مستخدم",
        email = email ?: "",
        city = city ?: "",
        role = role ?: "CLIENT", 
        profession = profession ?: "",
        isBlocked = isBlocked ?: false,
        profileImage = profileImage.toFullUrl("avatars"),
        totalRating = totalRating ?: 0.0,
        ratingCount = ratingCount ?: 0
    )

    private fun PostDto.toDomain(): Post = Post(
        id = id ?: "",
        authorId = authorId ?: "",
        authorName = authorName ?: "ناشر مجهول",
        type = type ?: "SERVICE",
        profession = profession ?: "",
        description = description ?: "",
        city = city ?: "",
        imageUrl = imageUrl.toFullUrl("posts"),
        timestamp = 0L 
    )

    private fun MessageDto.toDomain(): Message {
        val timeInMillis = try {
            if (!createdAt.isNullOrBlank()) {
                OffsetDateTime.parse(createdAt, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                    .toInstant()
                    .toEpochMilli()
            } else {
                id?.toLongOrNull() ?: System.currentTimeMillis()
            }
        } catch (e: Exception) {
            System.currentTimeMillis()
        }

        return Message(
            id = id?.toString() ?: "",
            senderId = senderId,
            receiverId = receiverId,
            content = content,
            timestamp = timeInMillis
        )
    }

    private fun ContractDto.toDomain(): Contract = Contract(
        id = id ?: "",
        artisanId = artisanId,
        customerId = customerId,
        artisanName = artisanName,
        customerName = customerName,
        details = details,
        price = price,
        status = try { ContractStatus.valueOf(status.uppercase()) } catch (e: Exception) { ContractStatus.PENDING },
        createdAt = try {
            OffsetDateTime.parse(createdAt, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant().toEpochMilli()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    )

    override suspend fun login(email: String, password: String): Result<User?> {
        val authResult = authService.login(email, password)
        if (authResult.isFailure) return Result.failure(authResult.exceptionOrNull()!!)
        val userId = authService.getCurrentUserId() ?: return Result.failure(Exception("المستخدم غير موجود"))
        val userDto = dbService.getUserProfile(userId)
        val userDomain = userDto?.toDomain()
        if (userDomain?.isBlocked == true) {
            authService.logout()
            return Result.failure(Exception("عذراً، هذا الحساب محظور من قبل الإدارة."))
        }
        return Result.success(userDomain)
    }

    override suspend fun register(email: String, password: String, user: User): Result<User?> {
        val authResult = authService.register(email, password)
        if (authResult.isFailure) return Result.failure(authResult.exceptionOrNull()!!)
        val userId = authService.getCurrentUserId() ?: return Result.failure(Exception("فشل التسجيل"))
        val userDto = UserDto(id = userId, name = user.name, email = email, city = user.city, role = user.role, profession = user.profession)
        dbService.createUserProfile(userDto)
        return Result.success(userDto.toDomain())
    }

    override suspend fun logout() { authService.logout() }

    override suspend fun getCurrentUser(): User? {
        val userId = authService.getCurrentUserId() ?: return null
        val user = dbService.getUserProfile(userId)?.toDomain()
        if (user?.isBlocked == true) {
            authService.logout()
            return null
        }
        return user
    }

    override fun getAllUsers(): Flow<List<User>> = dbService.getAllUsersFlow().map { list -> list.map { it.toDomain() } }

    override suspend fun blockUser(userId: String, blocked: Boolean): Result<Unit> = try {
        dbService.updateUserProfile(userId, mapOf("is_blocked" to JsonPrimitive(blocked)))
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun getUserById(userId: String): User? {
        return dbService.getUserProfile(userId)?.toDomain()
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    override fun getClientsWhoContacted(artisanId: String): Flow<List<User>> {
        return dbService.getAllMessagesFlow().flatMapLatest { messages ->
            val contactIds = messages
                .filter { it.receiverId == artisanId || it.senderId == artisanId }
                .map { if (it.senderId == artisanId) it.receiverId else it.senderId }
                .distinct()
            dbService.getAllUsersFlow().map { allUsers ->
                allUsers.filter { it.id in contactIds }.map { userDto ->
                    val lastMsg = messages.filter {
                        (it.senderId == userDto.id && it.receiverId == artisanId) ||
                        (it.senderId == artisanId && it.receiverId == userDto.id)
                    }.sortedBy { it.id }.lastOrNull()?.content ?: "محادثة جديدة"
                    userDto.toDomain().copy(profession = lastMsg)
                }
            }
        }.catch { emit(emptyList()) }
    }

    override fun getAllPosts(): Flow<List<Post>> = dbService.getAllPostsFlow().map { list -> list.map { it.toDomain() } }

    override suspend fun createPost(post: Post, imageUri: Uri?): Result<Unit> = try {
        val userId = authService.getCurrentUserId() ?: throw Exception("لم يتم تسجيل الدخول")
        var finalPath: String? = null
        if (imageUri != null) {
            val fileName = "post_${userId}_${System.currentTimeMillis()}.jpg"
            val uploadResult = storageService.uploadImage(imageUri, "posts", fileName)
            finalPath = if (uploadResult.isSuccess) fileName else null
        }
        val postDto = PostDto(authorId = userId, authorName = post.authorName, type = post.type, profession = post.profession, description = post.description, city = post.city, imageUrl = finalPath)
        dbService.createPost(postDto)
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun deletePost(postId: String): Result<Unit> = try {
        dbService.deletePost(postId)
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun sendMessage(message: Message): Result<Unit> = try {
        val messageDto = MessageDto(
            senderId = message.senderId,
            receiverId = message.receiverId,
            content = message.content
        )
        dbService.sendMessage(messageDto)
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override fun getMessagesBetween(user1: String, user2: String): Flow<List<Message>> =
        dbService.getAllMessagesFlow().map { list ->
            list.filter {
                (it.senderId == user1 && it.receiverId == user2) ||
                (it.senderId == user2 && it.receiverId == user1)
            }.map { it.toDomain() }.sortedBy { it.timestamp }
        }.catch { emit(emptyList()) }

    override fun getNotificationsCount(userId: String): Flow<Int> = dbService.getAllMessagesFlow().map { messages ->
        messages.count { it.receiverId == userId }
    }

    override suspend fun submitRating(rating: Rating): Result<Unit> = try {
        val newRatingVal = rating.rating.toDouble()
        val oldRatingDto = dbService.getRating(rating.artisanId, rating.customerId)
        val oldRatingVal = oldRatingDto?.rating ?: 0.0

        dbService.submitRating(RatingDto(
            id = oldRatingDto?.id, 
            artisanId = rating.artisanId,
            customerId = rating.customerId,
            rating = newRatingVal
        ))

        val artisanDto = dbService.getUserProfile(rating.artisanId)
        if (artisanDto != null) {
            val currentTotal = artisanDto.totalRating ?: 0.0
            val currentCount = artisanDto.ratingCount ?: 0
            val newTotal: Double
            val newCount: Int
            if (oldRatingDto != null) {
                newTotal = (currentTotal - oldRatingVal + newRatingVal).coerceAtLeast(0.0)
                newCount = currentCount
            } else {
                newTotal = currentTotal + newRatingVal
                newCount = currentCount + 1
            }
            dbService.updateUserProfile(rating.artisanId, mapOf(
                "total_rating" to JsonPrimitive(newTotal),
                "rating_count" to JsonPrimitive(newCount)
            ))
            Result.success(Unit)
        } else { Result.failure(Exception("Artisan not found")) }
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun submitComplaint(complaint: Complaint): Result<Unit> = try {
        dbService.submitComplaint(ComplaintDto(senderId = complaint.senderId, senderName = complaint.senderName, subject = complaint.subject, message = complaint.message))
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override fun getAllComplaints(): Flow<List<Complaint>> = dbService.getAllComplaintsFlow().map { list ->
        list.map { Complaint(it.id ?: "", it.senderId ?: "", it.senderName ?: "", it.subject ?: "", it.message ?: "", it.adminReply, 0L) }
    }

    override suspend fun deleteComplaint(complaintId: String): Result<Unit> = try {
        dbService.deleteComplaint(complaintId)
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun updateUserProfile(user: User): Result<Unit> = try {
        val updates = mutableMapOf<String, JsonElement>("name" to JsonPrimitive(user.name), "city" to JsonPrimitive(user.city))
        user.profileImage?.let { updates["profile_image"] = JsonPrimitive(it) }
        dbService.updateUserProfile(user.id, updates)
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun deleteAccount(): Result<Unit> = Result.success(Unit)

    // Contracts
    override suspend fun createContract(contract: Contract): Result<Unit> = try {
        val contractDto = ContractDto(
            artisanId = contract.artisanId,
            customerId = contract.customerId,
            artisanName = contract.artisanName,
            customerName = contract.customerName,
            details = contract.details,
            price = contract.price,
            status = contract.status.name
        )
        dbService.createContract(contractDto)
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun updateContractStatus(contractId: String, status: ContractStatus): Result<Unit> = try {
        dbService.updateContractStatus(contractId, status.name)
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override fun getContractsBetween(user1: String, user2: String): Flow<List<Contract>> =
        dbService.getContractsFlow(user1).map { list ->
            list.filter { 
                (it.artisanId == user1 && it.customerId == user2) ||
                (it.artisanId == user2 && it.customerId == user1)
            }.map { it.toDomain() }
        }.catch { emit(emptyList()) }

    override fun getAllContractsForUser(userId: String): Flow<List<Contract>> =
        dbService.getContractsFlow(userId).map { list ->
            list.map { it.toDomain() }
        }.catch { emit(emptyList()) }
}
