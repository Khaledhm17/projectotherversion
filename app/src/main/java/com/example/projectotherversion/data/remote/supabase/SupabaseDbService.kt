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
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException

@Singleton
class SupabaseDbService @Inject constructor(
    client: SupabaseClient
) {
    private val postgrest = client.postgrest

    // Users
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
        postgrest.from("users").update(updates) {
            filter { eq("id", uid) }
        }
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

    // Posts
    suspend fun createPost(postDto: PostDto) {
        postgrest.from("posts").insert(postDto)
    }

    suspend fun deletePost(postId: String) {
        postgrest.from("posts").delete {
            filter { eq("id", postId) }
        }
    }

    fun getAllPostsFlow(): Flow<List<PostDto>> = flow {
        val initial = postgrest.from("posts").select().decodeList<PostDto>()
        emit(initial)
        try {
            emitAll(postgrest.from("posts").selectAsFlow(PostDto::id))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("SupabaseDb", "Posts Realtime Error: ${e.message}")
        }
    }.catch { e ->
        if (e is CancellationException) throw e
        Log.e("SupabaseDb", "Posts Flow Error: ${e.message}")
        emit(emptyList())
    }

    // Messages
    suspend fun sendMessage(messageDto: MessageDto) {
        postgrest.from("messages").insert(messageDto)
    }

    fun getAllMessagesFlow(): Flow<List<MessageDto>> = flow {
        val initial = postgrest.from("messages").select().decodeList<MessageDto>()
        emit(initial)
        Log.d("SupabaseDb", "Loaded ${initial.size} messages from DB")

        try {
            emitAll(postgrest.from("messages").selectAsFlow(MessageDto::id))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("SupabaseDb", "Realtime Message Error: ${e.message}")
        }
    }.catch { e ->
        if (e is CancellationException) throw e
        Log.e("SupabaseDb", "Global Message Flow Error: ${e.message}")
        emit(emptyList())
    }

    // Complaints
    suspend fun submitComplaint(complaintDto: ComplaintDto) {
        postgrest.from("complaints").insert(complaintDto)
    }

    fun getAllComplaintsFlow(): Flow<List<ComplaintDto>> = flow {
        val initial = postgrest.from("complaints").select().decodeList<ComplaintDto>()
        emit(initial)
        try {
            emitAll(postgrest.from("complaints").selectAsFlow(ComplaintDto::id))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("SupabaseDb", "Complaints Realtime Error: ${e.message}")
        }
    }.catch { e ->
        if (e is CancellationException) throw e
        Log.e("SupabaseDb", "Complaints Flow Error: ${e.message}")
        emit(emptyList())
    }

    suspend fun deleteComplaint(complaintId: String) {
        postgrest.from("complaints").delete {
            filter { eq("id", complaintId) }
        }
    }
}
