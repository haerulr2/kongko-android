package com.kongko.app.features.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kongko.app.core.database.entity.MessageEntity
import com.kongko.app.core.network.KongkoWebSocketClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatRoomViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val wsClient: KongkoWebSocketClient,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val chatId: String = checkNotNull(savedStateHandle["chatId"])
    val title: String = savedStateHandle["title"] ?: "Obrolan"

    val messages: StateFlow<List<MessageEntity>> = chatRepository.getMessages(chatId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    init {
        viewModelScope.launch {
            chatRepository.syncMessages(chatId)
        }
    }

    fun onInputTextChanged(text: String) {
        _inputText.value = text
        wsClient.sendTyping(chatId, text.isNotBlank())
    }

    fun sendMessage() {
        val text = _inputText.value.trim()
        if (text.isEmpty()) return

        _inputText.value = ""
        wsClient.sendTyping(chatId, false)

        viewModelScope.launch {
            chatRepository.sendMessage(chatId, text)
        }
    }

    fun startCall(isVideo: Boolean) {
        // Find other participant from chat or trigger initiate
        // wsClient.initiateCall(...)
    }
}
