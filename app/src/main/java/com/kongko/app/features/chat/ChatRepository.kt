package com.kongko.app.features.chat

import android.util.Log
import com.kongko.app.core.database.dao.ChatRoomDao
import com.kongko.app.core.database.dao.MessageDao
import com.kongko.app.core.database.entity.ChatRoomEntity
import com.kongko.app.core.database.entity.MessageEntity
import com.kongko.app.core.datastore.SessionDataStore
import com.kongko.app.core.network.KongkoApiService
import com.kongko.app.core.network.KongkoWebSocketClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val chatRoomDao: ChatRoomDao,
    private val messageDao: MessageDao,
    private val apiService: KongkoApiService,
    private val wsClient: KongkoWebSocketClient,
    private val sessionDataStore: SessionDataStore
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        // Listen to WebSocket events and persist them into Room Database
        scope.launch {
            wsClient.events.collect { event ->
                when (event.type) {
                    "message.created", "message.new" -> {
                        val payload = event.payload ?: return@collect
                        val msgId = payload["id"]?.jsonPrimitive?.contentOrNull ?: UUID.randomUUID().toString()
                        val chatId = payload["chat_id"]?.jsonPrimitive?.contentOrNull ?: return@collect
                        val senderId = payload["sender_id"]?.jsonPrimitive?.contentOrNull ?: ""
                        val content = payload["content"]?.jsonPrimitive?.contentOrNull ?: ""
                        val type = payload["type"]?.jsonPrimitive?.contentOrNull ?: "text"
                        val currentUserId = sessionDataStore.getUserId()

                        val entity = MessageEntity(
                            id = msgId,
                            chatId = chatId,
                            senderId = senderId,
                            content = content,
                            type = type,
                            mediaUrl = payload["media_url"]?.jsonPrimitive?.contentOrNull,
                            isForwarded = false,
                            isRead = false,
                            isDelivered = true,
                            createdAt = System.currentTimeMillis(),
                            isMine = senderId == currentUserId
                        )
                        messageDao.insertMessage(entity)

                        // Update room's last message
                        val existingRoom = chatRoomDao.getRoomById(chatId)
                        if (existingRoom != null) {
                            chatRoomDao.insertOrUpdate(
                                existingRoom.copy(
                                    lastMessage = content,
                                    lastSenderId = senderId,
                                    updatedAt = System.currentTimeMillis()
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    fun getChatRooms(): Flow<List<ChatRoomEntity>> = chatRoomDao.getAllRooms()

    fun getMessages(chatId: String): Flow<List<MessageEntity>> = messageDao.getMessagesForChat(chatId)

    suspend fun syncChats() {
        try {
            val response = apiService.getChats()
            if (response.isSuccessful && response.body()?.data != null) {
                val chats = response.body()!!.data!!
                val currentUserId = sessionDataStore.getUserId()

                val entities = chats.map { chat ->
                    // Determine display title: if private chat, display other participant's name
                    val otherParticipant = chat.participants.firstOrNull { it.userId != currentUserId }
                    val title = chat.title
                        ?: otherParticipant?.user?.username
                        ?: otherParticipant?.user?.phoneNumber
                        ?: "Obrolan"

                    val avatar = chat.avatarUrl ?: otherParticipant?.user?.avatarUrl

                    ChatRoomEntity(
                        id = chat.id,
                        type = chat.type,
                        title = title,
                        avatarUrl = avatar,
                        lastMessage = chat.lastMessage,
                        lastSenderId = chat.lastSenderId,
                        lastMessageIsRead = chat.lastMessageIsRead ?: false,
                        lastMessageIsDelivered = chat.lastMessageIsDelivered ?: false,
                        unreadCount = chat.participants.firstOrNull { it.userId == currentUserId }?.unreadCount ?: 0,
                        updatedAt = System.currentTimeMillis()
                    )
                }
                chatRoomDao.insertOrUpdateAll(entities)
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "syncChats error: ${e.message}")
        }
    }

    suspend fun syncMessages(chatId: String) {
        try {
            val response = apiService.getMessages(chatId)
            if (response.isSuccessful && response.body()?.data != null) {
                val currentUserId = sessionDataStore.getUserId()
                val messages = response.body()!!.data!!.map { msg ->
                    MessageEntity(
                        id = msg.id,
                        chatId = msg.chatId,
                        senderId = msg.senderId,
                        content = msg.content,
                        type = msg.type,
                        mediaUrl = msg.mediaUrl,
                        isForwarded = msg.isForwarded,
                        isRead = msg.isRead,
                        isDelivered = msg.isDelivered,
                        createdAt = System.currentTimeMillis(),
                        isMine = msg.senderId == currentUserId
                    )
                }
                messageDao.insertMessages(messages)
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "syncMessages error: ${e.message}")
        }
    }

    suspend fun sendMessage(chatId: String, content: String) {
        val currentUserId = sessionDataStore.getUserId() ?: ""
        val localId = UUID.randomUUID().toString()

        // Optimistic insert into local Room DB
        val localMsg = MessageEntity(
            id = localId,
            chatId = chatId,
            senderId = currentUserId,
            content = content,
            type = "text",
            mediaUrl = null,
            isForwarded = false,
            isRead = false,
            isDelivered = false,
            createdAt = System.currentTimeMillis(),
            isMine = true
        )
        messageDao.insertMessage(localMsg)

        // Send via WebSocket
        wsClient.sendMessage(chatId, content)
    }
}
