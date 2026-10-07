package com.kongko.app.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CallDto(
    val id: String,
    @SerialName("caller_id") val callerId: String,
    @SerialName("receiver_id") val receiverId: String,
    val status: String,
    val type: String,
    @SerialName("channel_name") val channelName: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("caller_name") val callerName: String? = null,
    @SerialName("caller_photo_url") val callerPhotoUrl: String? = null,
    @SerialName("receiver_name") val receiverName: String? = null,
    @SerialName("receiver_photo_url") val receiverPhotoUrl: String? = null,
    @SerialName("duration_seconds") val durationSeconds: Int? = null
)

@Serializable
data class AgoraTokenResponse(
    val token: String
)

@Serializable
data class SyncContactsRequest(
    @SerialName("phone_numbers") val phoneNumbers: List<String>
)

@Serializable
data class RegisterFcmTokenRequest(
    @SerialName("fcm_token") val fcmToken: String
)
