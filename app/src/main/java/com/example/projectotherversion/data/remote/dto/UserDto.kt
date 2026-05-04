package com.example.projectotherversion.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class UserDto(
    @SerialName("id")
    val id: String? = null,
    @SerialName("name")
    val name: String? = null,
    @SerialName("email")
    val email: String? = null,
    @SerialName("city")
    val city: String? = null,
    @SerialName("role")
    val role: String? = null,
    @SerialName("profession")
    val profession: String? = null,
    @SerialName("is_blocked")
    val isBlocked: Boolean = false,
    @SerialName("profile_image")
    val profileImage: String? = null
)
