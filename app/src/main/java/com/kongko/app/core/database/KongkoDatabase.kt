package com.kongko.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kongko.app.core.database.dao.ChatRoomDao
import com.kongko.app.core.database.dao.MessageDao
import com.kongko.app.core.database.entity.ChatRoomEntity
import com.kongko.app.core.database.entity.MessageEntity

@Database(
    entities = [
        ChatRoomEntity::class,
        MessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class KongkoDatabase : RoomDatabase() {
    abstract fun chatRoomDao(): ChatRoomDao
    abstract fun messageDao(): MessageDao
}
