package com.kongko.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kongko.app.core.database.entity.ChatRoomEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatRoomDao {

    @Query("SELECT * FROM chat_rooms ORDER BY updatedAt DESC")
    fun getAllRooms(): Flow<List<ChatRoomEntity>>

    @Query("SELECT * FROM chat_rooms WHERE id = :id LIMIT 1")
    suspend fun getRoomById(id: String): ChatRoomEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(room: ChatRoomEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAll(rooms: List<ChatRoomEntity>)

    @Query("DELETE FROM chat_rooms WHERE id = :id")
    suspend fun deleteRoom(id: String)

    @Query("DELETE FROM chat_rooms")
    suspend fun clearAll()
}
