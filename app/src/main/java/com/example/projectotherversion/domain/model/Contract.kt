package com.example.projectotherversion.domain.model

data class Contract(
    val id: String = "",
    val artisanId: String,
    val customerId: String,
    val artisanName: String,
    val customerName: String,
    val details: String,
    val price: Double,
    val status: ContractStatus = ContractStatus.PENDING,
    val createdAt: Long = 0L
)

enum class ContractStatus {
    PENDING, ACCEPTED, REJECTED, CLOSED
}