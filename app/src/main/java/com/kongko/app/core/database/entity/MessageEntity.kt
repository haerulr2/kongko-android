package com.kongko.app.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    indices = [
        Index(value = ["chatId"]),
        Index(value = ["createdAt"])
    ]
)
data class MessageEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val senderId: String,
    val content: String,
    val type: String,
    val mediaUrl: String?,
    val isForwarded: Boolean,
    val isRead: Boolean,
    val isDelivered: Boolean,
    val createdAt: Long,
    val isMine: Boolean
)
