package com.example.projectotherversion.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class ContractDto(
    @SerialName("id")
    val id: String? = null,
    @SerialName("artisan_id")
    val artisanId: String,
    @SerialName("customer_id")
    val customerId: String,
    @SerialName("artisan_name")
    val artisanName: String,
    @SerialName("customer_name")
    val customerName: String,
    @SerialName("details")
    val details: String,
    @SerialName("price")
    val price: Double,
    @SerialName("status")
    val status: String = "PENDING", // PENDING, ACCEPTED, REJECTED, CLOSED
    @SerialName("created_at")
    val createdAt: String? = null
)
