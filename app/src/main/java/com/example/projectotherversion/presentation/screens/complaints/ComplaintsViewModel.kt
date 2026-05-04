package com.example.projectotherversion.presentation.screens.complaints

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectotherversion.domain.model.Complaint
import com.example.projectotherversion.domain.model.User
import com.example.projectotherversion.domain.usecase.complaint.DeleteComplaintUseCase
import com.example.projectotherversion.domain.usecase.complaint.GetAllComplaintsUseCase
import com.example.projectotherversion.domain.usecase.complaint.SubmitComplaintUseCase
import com.example.projectotherversion.domain.usecase.user.GetCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ComplaintsViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val submitComplaintUseCase: SubmitComplaintUseCase,
    private val getAllComplaintsUseCase: GetAllComplaintsUseCase,
    private val deleteComplaintUseCase: DeleteComplaintUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ComplaintsState())
    val state = _state.asStateFlow()

    init {
        loadCurrentUser()
        loadComplaints()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase()
            _state.update { it.copy(currentUser = user) }
        }
    }

    private fun loadComplaints() {
        viewModelScope.launch {
            getAllComplaintsUseCase()
                .catch { e -> 
                    _state.update { it.copy(error = "فشل تحميل الشكاوى: ${e.message}") }
                }
                .collect { complaints ->
                    _state.update { it.copy(complaints = complaints) }
                }
        }
    }

    fun onEvent(event: ComplaintsEvent) {
        when (event) {
            is ComplaintsEvent.MessageChanged -> _state.update { it.copy(message = event.message) }
            is ComplaintsEvent.SubjectChanged -> _state.update { it.copy(subject = event.subject) }
            ComplaintsEvent.SubmitClicked -> submitComplaint()
            is ComplaintsEvent.DeleteComplaint -> deleteComplaint(event.complaintId)
            ComplaintsEvent.ClearForm -> _state.update { it.copy(subject = "", message = "", isSubmitted = false, error = null) }
        }
    }

    private fun submitComplaint() {
        viewModelScope.launch {
            val user = _state.value.currentUser ?: return@launch
            _state.update { it.copy(isLoading = true, error = null) }

            val complaint = Complaint(
                senderId = user.id,
                senderName = user.name,
                subject = _state.value.subject,
                message = _state.value.message,
                timestamp = System.currentTimeMillis()
            )

            val result = submitComplaintUseCase(complaint)
            _state.update { it.copy(isLoading = false) }

            result.onSuccess {
                _state.update { it.copy(isSubmitted = true) }
            }.onFailure { error ->
                _state.update { it.copy(error = error.message) }
            }
        }
    }

    private fun deleteComplaint(complaintId: String) {
        viewModelScope.launch {
            deleteComplaintUseCase(complaintId)
        }
    }
}

data class ComplaintsState(
    val currentUser: User? = null,
    val complaints: List<Complaint> = emptyList(),
    val subject: String = "",
    val message: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSubmitted: Boolean = false
)

sealed class ComplaintsEvent {
    data class SubjectChanged(val subject: String) : ComplaintsEvent()
    data class MessageChanged(val message: String) : ComplaintsEvent()
    object SubmitClicked : ComplaintsEvent()
    data class DeleteComplaint(val complaintId: String) : ComplaintsEvent()
    object ClearForm : ComplaintsEvent()
}
