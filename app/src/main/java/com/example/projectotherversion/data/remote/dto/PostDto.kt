package com.example.projectotherversion.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class PostDto(
    @SerialName("id")
    val id: String? = null,
    @SerialName("author_id")
    val authorId: String = "",
    @SerialName("author_name")
    val authorName: String = "",
    @SerialName("type")
    val type: String = "", // "SERVICE" or "REQUEST"
    @SerialName("profession")
    val profession: String = "",
    @SerialName("description")
    val description: String? = null,
    @SerialName("city")
    val city: String = "",
    @SerialName("image_url")
    val imageUrl: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
)
