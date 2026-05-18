package com.example.projectotherversion.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class RatingDto(
    @SerialName("id")
    val id: String? = null,
    @SerialName("artisan_id")
    val artisanId: String,
    @SerialName("customer_id")
    val customerId: String,
    @SerialName("rating")
    val rating: Double,
    @SerialName("created_at")
    val createdAt: String? = null
)
