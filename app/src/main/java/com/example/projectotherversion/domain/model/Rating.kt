package com.example.projectotherversion.domain.model

data class Rating(
    val id: String = "",
    val artisanId: String,
    val customerId: String,
    val rating: Int, // 1 to 5
    val createdAt: Long = 0L
)
