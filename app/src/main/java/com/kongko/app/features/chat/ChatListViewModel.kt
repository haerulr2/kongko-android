package com.kongko.app.features.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kongko.app.core.database.entity.ChatRoomEntity
import com.kongko.app.core.network.KongkoWebSocketClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val wsClient: KongkoWebSocketClient
) : ViewModel() {

    val chatRooms: StateFlow<List<ChatRoomEntity>> = chatRepository.getChatRooms()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Connect WebSocket when chat list is open
        wsClient.connect()
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            chatRepository.syncChats()
        }
    }
}
