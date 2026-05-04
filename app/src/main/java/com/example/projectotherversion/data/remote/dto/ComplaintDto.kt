package com.example.projectotherversion.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class ComplaintDto(
    @SerialName("id")
    var id: String = "",
    @SerialName("sender_id")
    var senderId: String = "",
    @SerialName("sender_name")
    var senderName: String = "",
    @SerialName("subject")
    var subject: String = "",
    @SerialName("message")
    var message: String = "",
    @SerialName("admin_reply")
    var adminReply: String? = null,
    @SerialName("created_at")
    var createdAt: String? = null
)
