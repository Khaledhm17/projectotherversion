package com.example.projectotherversion.domain.model

data class Post(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val type: String = "", // "SERVICE" or "REQUEST"
    val profession: String = "",
    val description: String = "",
    val city: String = "",
    val imageUrl: String? = null,
    val timestamp: Long = 0L
)