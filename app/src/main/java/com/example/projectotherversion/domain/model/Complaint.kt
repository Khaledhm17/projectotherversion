package com.example.projectotherversion.domain.model

data class Complaint(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val subject: String = "",
    val message: String = "",
    val adminReply: String? = null,
    val timestamp: Long = 0L
)