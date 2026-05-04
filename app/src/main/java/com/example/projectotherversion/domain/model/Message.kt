package com.example.projectotherversion.domain.model

data class Message(
    val id: Any = "",
    val senderId: String = "",
    val receiverId: String = "",
    val content: String = "",
    val timestamp: Long = 0L
)