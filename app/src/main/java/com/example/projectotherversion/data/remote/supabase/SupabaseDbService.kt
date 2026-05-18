@file:OptIn(SupabaseExperimental::class)

package com.example.projectotherversion.data.remote.supabase

import android.util.Log
import com.example.projectotherversion.data.remote.dto.*
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.selectAsFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException

@Singleton
class SupabaseDbService @Inject constructor(
    client: SupabaseClient
) {
    private val postgrest = client.postgrest

    suspend fun createUserProfile(userDto: UserDto) {
        postgrest.from("users").insert(userDto)
    }

    suspend fun getUserProfile(uid: String): UserDto? = try {
        postgrest.from("users").select {
            filter { eq("id", uid) }
        }.decodeSingleOrNull<UserDto>()
    } catch (e: Exception) {
        Log.e("SupabaseDb", "Error getUserProfile: ${e.message}")
        null
    }

    suspend fun updateUserProfile(uid: String, updates: Map<String, JsonElement>) {
        // قمنا بإزالة try-catch هنا لضمان رمي الأخطاء في حال فشل التحديث في السيرفر
        // هذا يمنع الـ ViewModel من الاعتقاد بأن الحظر نجح وهو لم يصل لقاعدة البيانات
        postgrest.from("users").update(JsonObject(updates)) {
            filter { eq("id", uid) }
        }
        Log.d("SupabaseDb", "Update request executed for $uid with keys: ${updates.keys}")
    }

    fun getAllUsersFlow(): Flow<List<UserDto>> = flow {
        val initial = postgrest.from("users").select().decodeList<UserDto>()
        emit(initial)
        try {
            emitAll(postgrest.from("users").selectAsFlow(UserDto::id))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("SupabaseDb", "Users Realtime Error: ${e.message}")
        }
    }.catch { e ->
        if (e is CancellationException) throw e
        Log.e("SupabaseDb", "Users Flow Error: ${e.message}")
        emit(emptyList())
    }

    suspend fun createPost(postDto: PostDto) { postgrest.from("posts").insert(postDto) }
    suspend fun deletePost(postId: String) { postgrest.from("posts").delete { filter { eq("id", postId) } } }
    fun getAllPostsFlow(): Flow<List<PostDto>> = flow {
        val initial = postgrest.from("posts").select().decodeList<PostDto>()
        emit(initial)
        try { emitAll(postgrest.from("posts").selectAsFlow(PostDto::id)) } catch (e: Exception) { Log.e("SupabaseDb", "Posts Realtime Error: ${e.message}") }
    }.catch { emit(emptyList()) }

    suspend fun sendMessage(messageDto: MessageDto) { postgrest.from("messages").insert(messageDto) }
    fun getAllMessagesFlow(): Flow<List<MessageDto>> = flow {
        val initial = postgrest.from("messages").select().decodeList<MessageDto>()
        emit(initial)
        try { emitAll(postgrest.from("messages").selectAsFlow(MessageDto::id)) } catch (e: Exception) { Log.e("SupabaseDb", "Realtime Message Error: ${e.message}") }
    }.catch { emit(emptyList()) }

    suspend fun submitComplaint(complaintDto: ComplaintDto) { postgrest.from("complaints").insert(complaintDto) }
    fun getAllComplaintsFlow(): Flow<List<ComplaintDto>> = flow {
        val initial = postgrest.from("complaints").select().decodeList<ComplaintDto>()
        emit(initial)
        try { emitAll(postgrest.from("complaints").selectAsFlow(ComplaintDto::id)) } catch (e: Exception) { Log.e("SupabaseDb", "Complaints Realtime Error: ${e.message}") }
    }.catch { emit(emptyList()) }

    suspend fun deleteComplaint(complaintId: String) { postgrest.from("complaints").delete { filter { eq("id", complaintId) } } }

    suspend fun submitRating(ratingDto: RatingDto) {
        postgrest.from("ratings").insert(ratingDto)
    }
}
