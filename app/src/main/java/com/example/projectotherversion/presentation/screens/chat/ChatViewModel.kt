package com.example.projectotherversion.presentation.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectotherversion.domain.model.*
import com.example.projectotherversion.domain.usecase.contract.CreateContractUseCase
import com.example.projectotherversion.domain.usecase.contract.GetContractsBetweenUseCase
import com.example.projectotherversion.domain.usecase.contract.UpdateContractStatusUseCase
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
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val createContractUseCase: CreateContractUseCase,
    private val updateContractStatusUseCase: UpdateContractStatusUseCase,
    private val getContractsBetweenUseCase: GetContractsBetweenUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ChatState())
    val state = _state.asStateFlow()

    private var messagesJob: Job? = null
    private var contractsJob: Job? = null

    fun initialize(otherUserId: String, otherUserName: String) {
        if (_state.value.otherUser?.id == otherUserId) return

        messagesJob?.cancel()
        contractsJob?.cancel()

        viewModelScope.launch {
            try {
                val currentUser = getCurrentUserUseCase() ?: return@launch
                val otherUser = User(id = otherUserId, name = otherUserName)
                
                _state.update { it.copy(
                    currentUserId = currentUser.id, 
                    currentUserRole = currentUser.role,
                    currentUserName = currentUser.name,
                    otherUser = otherUser 
                ) }
                
                // Observe Messages
                messagesJob = getMessagesBetweenUseCase(currentUser.id, otherUserId)
                    .catch { it.printStackTrace() }
                    .onEach { messages ->
                        _state.update { it.copy(messages = messages) }
                    }.launchIn(viewModelScope)

                // Observe Contracts
                contractsJob = getContractsBetweenUseCase(currentUser.id, otherUserId)
                    .catch { it.printStackTrace() }
                    .onEach { contracts ->
                        _state.update { it.copy(contracts = contracts) }
                    }.launchIn(viewModelScope)

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

    // Contract Actions
    fun createContract(details: String, price: Double) {
        viewModelScope.launch {
            try {
                val contract = Contract(
                    artisanId = _state.value.currentUserId,
                    customerId = _state.value.otherUser?.id ?: return@launch,
                    artisanName = _state.value.currentUserName,
                    customerName = _state.value.otherUser?.name ?: "زبون",
                    details = details,
                    price = price,
                    status = ContractStatus.PENDING
                )
                createContractUseCase(contract)
                
                // Send automated message about the deal
                val message = Message(
                    senderId = _state.value.currentUserId,
                    receiverId = _state.value.otherUser?.id ?: return@launch,
                    content = "لقد أرسلت لك عرض عمل جديد: $details بقيمة $price دج. يرجى المراجعة."
                )
                sendMessageUseCase(message)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateContractStatus(contractId: String, status: ContractStatus) {
        viewModelScope.launch {
            try {
                updateContractStatusUseCase(contractId, status)
                
                val statusText = when(status) {
                    ContractStatus.ACCEPTED -> "قبلت عرض العمل."
                    ContractStatus.REJECTED -> "رفضت عرض العمل."
                    ContractStatus.CLOSED -> "أغلقت الصفقة."
                    else -> ""
                }
                
                if (statusText.isNotEmpty()) {
                    val message = Message(
                        senderId = _state.value.currentUserId,
                        receiverId = _state.value.otherUser?.id ?: return@launch,
                        content = statusText
                    )
                    sendMessageUseCase(message)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

data class ChatState(
    val currentUserId: String = "",
    val currentUserRole: String = "",
    val currentUserName: String = "",
    val otherUser: User? = null,
    val messages: List<Message> = emptyList(),
    val contracts: List<Contract> = emptyList(),
    val currentMessage: String = ""
)
