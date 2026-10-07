package com.kongko.app.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatDto(
    val id: String,
    val type: String,
    val title: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val description: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    val participants: List<ParticipantDto> = emptyList(),
    @SerialName("last_message") val lastMessage: String? = null,
    @SerialName("last_sender_id") val lastSenderId: String? = null,
    @SerialName("last_message_is_read") val lastMessageIsRead: Boolean? = null,
    @SerialName("last_message_is_delivered") val lastMessageIsDelivered: Boolean? = null
)

@Serializable
data class ParticipantDto(
    @SerialName("user_id") val userId: String,
    val role: String = "member",
    val user: UserDto? = null,
    @SerialName("unread_count") val unreadCount: Int = 0
)

@Serializable
data class CreateChatRequest(
    val type: String = "private",
    @SerialName("participant_ids") val participantIds: List<String>,
    val title: String? = null
)

@Serializable
data class MessageDto(
    val id: String,
    @SerialName("chat_id") val chatId: String,
    @SerialName("sender_id") val senderId: String,
    val content: String,
    val type: String = "text",
    @SerialName("media_url") val mediaUrl: String? = null,
    @SerialName("is_forwarded") val isForwarded: Boolean = false,
    @SerialName("is_read") val isRead: Boolean = false,
    @SerialName("is_delivered") val isDelivered: Boolean = false,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class SendMessagePayload(
    @SerialName("chat_id") val chatId: String,
    val content: String,
    val type: String = "text",
    @SerialName("media_url") val mediaUrl: String? = null,
    @SerialName("is_forwarded") val isForwarded: Boolean = false
)
