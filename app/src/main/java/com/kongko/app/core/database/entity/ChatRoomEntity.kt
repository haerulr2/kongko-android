package com.kongko.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_rooms")
data class ChatRoomEntity(
    @PrimaryKey val id: String,
    val type: String,
    val title: String,
    val avatarUrl: String?,
    val lastMessage: String?,
    val lastSenderId: String?,
    val lastMessageIsRead: Boolean,
    val lastMessageIsDelivered: Boolean,
    val unreadCount: Int,
    val updatedAt: Long
)
