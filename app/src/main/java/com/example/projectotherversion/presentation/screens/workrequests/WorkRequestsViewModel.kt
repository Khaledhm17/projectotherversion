package com.example.projectotherversion.presentation.screens.workrequests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectotherversion.domain.model.User
import com.example.projectotherversion.domain.usecase.user.GetClientsWhoContactedUseCase
import com.example.projectotherversion.domain.usecase.user.GetCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkRequestsViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getClientsWhoContactedUseCase: GetClientsWhoContactedUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(WorkRequestsState())
    val state = _state.asStateFlow()

    init {
        loadInbox()
    }

    private fun loadInbox() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val currentUser = getCurrentUserUseCase()
                currentUser?.let { user ->
                    // هذه الدالة تجلب كل من تواصل مع المستخدم (سواء كان حرفي أو زبون)
                    getClientsWhoContactedUseCase(user.id)
                        .catch { e ->
                            _state.update { it.copy(error = "فشل تحميل الرسائل: ${e.message}", isLoading = false) }
                        }
                        .collect { contacts ->
                            _state.update { it.copy(contacts = contacts, isLoading = false) }
                        }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "حدث خطأ: ${e.message}", isLoading = false) }
            }
        }
    }
}

data class WorkRequestsState(
    val contacts: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
