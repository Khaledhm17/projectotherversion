package com.example.projectotherversion.domain.model

data class Request(
    val id: String = "",
    val clientId: String = "",
    val clientName: String = "",
    val description: String = "",
    val city: String = "",
    val timestamp: Long = 0L
)