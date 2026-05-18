package com.example.projectotherversion.domain.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val city: String = "",
    val role: String = "",
    val profession: String? = null,
    val isBlocked: Boolean = false,
    val profileImage: String? = null,
    val totalRating: Double = 0.0,
    val ratingCount: Int = 0
)
