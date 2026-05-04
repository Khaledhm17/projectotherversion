package com.example.projectotherversion.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class MessageDto(
    @SerialName("id")
    val id: String? = null, // العودة إلى Long ليتوافق مع bigint والترتيب الزمني
    @SerialName("sender_id")
    val senderId: String = "",
    @SerialName("receiver_id")
    val receiverId: String = "",
    @SerialName("content")
    val content: String = "",
    @SerialName("created_at")
    val createdAt: String? = null
)
