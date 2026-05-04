package com.example.projectotherversion.presentation.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectotherversion.domain.model.Message
import com.example.projectotherversion.domain.model.User
import com.example.projectotherversion.domain.usecase.message.GetMessagesBetweenUseCase
import com.example.projectotherversion.domain.usecase.message.SendMessageUseCase
import com.example.projectotherversion.domain.usecase.user.GetCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val sendMessageUseCase: SendMessageUseCase,
    private val getMessagesBetweenUseCase: GetMessagesBetweenUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ChatState())
    val state = _state.asStateFlow()

    private var messagesJob: Job? = null

    fun initialize(otherUserId: String, otherUserName: String) {
        // منع إعادة التهيئة إذا كانت البيانات محملة بالفعل لنفس المستخدم
        if (_state.value.otherUser?.id == otherUserId) return

        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            try {
                val currentUser = getCurrentUserUseCase() ?: return@launch
                val otherUser = User(id = otherUserId, name = otherUserName)
                
                _state.update { it.copy(currentUserId = currentUser.id, otherUser = otherUser) }
                
                getMessagesBetweenUseCase(currentUser.id, otherUserId)
                    .catch { e ->
                        e.printStackTrace()
                    }
                    .collect { messages ->
                        _state.update { it.copy(messages = messages) }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun onMessageChanged(text: String) {
        _state.update { it.copy(currentMessage = text) }
    }

    fun sendMessage() {
        val msg = _state.value.currentMessage.trim()
        if (msg.isEmpty()) return

        viewModelScope.launch {
            try {
                val message = Message(
                    senderId = _state.value.currentUserId,
                    receiverId = _state.value.otherUser?.id ?: return@launch,
                    content = msg
                )
                sendMessageUseCase(message)
                _state.update { it.copy(currentMessage = "") }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

data class ChatState(
    val currentUserId: String = "",
    val otherUser: User? = null,
    val messages: List<Message> = emptyList(),
    val currentMessage: String = ""
)
