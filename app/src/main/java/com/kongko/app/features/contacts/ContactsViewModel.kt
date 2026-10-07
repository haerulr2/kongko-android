package com.kongko.app.features.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kongko.app.core.network.KongkoApiService
import com.kongko.app.core.network.dto.CreateChatRequest
import com.kongko.app.core.network.dto.UserDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ContactsUiState {
    object Loading : ContactsUiState
    data class Success(val registeredUsers: List<UserDto>) : ContactsUiState
    data class Error(val message: String) : ContactsUiState
}

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val contactsRepository: ContactsRepository,
    private val apiService: KongkoApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow<ContactsUiState>(ContactsUiState.Loading)
    val uiState: StateFlow<ContactsUiState> = _uiState.asStateFlow()

    fun loadContacts() {
        viewModelScope.launch {
            _uiState.value = ContactsUiState.Loading
            try {
                val users = contactsRepository.syncContactsWithServer()
                _uiState.value = ContactsUiState.Success(users)
            } catch (e: Exception) {
                _uiState.value = ContactsUiState.Error(e.message ?: "Gagal memuat kontak")
            }
        }
    }

    fun startChat(user: UserDto, onChatCreated: (chatId: String) -> Unit) {
        viewModelScope.launch {
            try {
                val resp = apiService.createChat(
                    CreateChatRequest(
                        type = "private",
                        participantIds = listOf(user.id)
                    )
                )
                if (resp.isSuccessful && resp.body()?.data != null) {
                    onChatCreated(resp.body()!!.data!!.id)
                }
            } catch (e: Exception) {
                // handle error
            }
        }
    }
}
