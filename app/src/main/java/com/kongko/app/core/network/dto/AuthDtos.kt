package com.kongko.app.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VerifyTokenRequest(
    @SerialName("firebase_id_token") val firebaseIdToken: String
)

@Serializable
data class RefreshTokenRequest(
    @SerialName("refresh_token") val refreshToken: String
)

@Serializable
data class AuthResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    val user: UserDto
)

@Serializable
data class UserDto(
    val id: String,
    @SerialName("phone_number") val phoneNumber: String,
    val username: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val bio: String? = null,
    @SerialName("last_seen_at") val lastSeenAt: String? = null,
    @SerialName("is_online") val isOnline: Boolean = false
)

@Serializable
data class UpdateProfileRequest(
    val username: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val bio: String? = null
)
