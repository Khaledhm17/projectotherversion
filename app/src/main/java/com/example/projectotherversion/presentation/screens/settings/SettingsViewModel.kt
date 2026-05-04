package com.example.projectotherversion.presentation.screens.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectotherversion.domain.model.User
import com.example.projectotherversion.domain.usecase.user.DeleteAccountUseCase
import com.example.projectotherversion.domain.usecase.user.GetCurrentUserUseCase
import com.example.projectotherversion.domain.usecase.user.UpdateUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state = _state.asStateFlow()

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase()
            user?.let {
                _state.update { state ->
                    state.copy(
                        name = it.name,
                        city = it.city,
                        profession = it.profession ?: "بناء",
                        profileImageUrl = it.profileImage,
                        user = it
                    )
                }
            }
        }
    }

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.NameChanged -> _state.update { it.copy(name = event.name) }
            is SettingsEvent.CityChanged -> _state.update { it.copy(city = event.city) }
            is SettingsEvent.ProfessionChanged -> _state.update { it.copy(profession = event.profession) }
            is SettingsEvent.ProfileImageChanged -> _state.update { it.copy(profileImageUri = event.uri) }
            SettingsEvent.SaveClicked -> saveProfile()
            SettingsEvent.DeleteAccountClicked -> deleteAccount()
        }
    }

    private fun saveProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val currentUser = _state.value.user ?: return@launch
            val updatedUser = currentUser.copy(
                name = _state.value.name,
                city = _state.value.city,
                profession = if (currentUser.role == "CRAFTSMAN") _state.value.profession else null,
                profileImage = _state.value.profileImageUrl
            )

            val result = updateUserProfileUseCase(updatedUser)
            _state.update { it.copy(isLoading = false) }

            result.onSuccess {
                _state.update { it.copy(isSuccess = true) }
            }.onFailure { error ->
                _state.update { it.copy(error = error.message) }
            }
        }
    }

    private fun deleteAccount() {
        viewModelScope.launch {
            _state.update { it.copy(isDeleting = true) }
            val result = deleteAccountUseCase()
            _state.update { it.copy(isDeleting = false) }
            result.onSuccess {
                _state.update { it.copy(isAccountDeleted = true) }
            }.onFailure { error ->
                _state.update { it.copy(error = error.message) }
            }
        }
    }
}

data class SettingsState(
    val user: User? = null,
    val name: String = "",
    val city: String = "الوادي",
    val profession: String = "بناء",
    val profileImageUrl: String? = null,
    val profileImageUri: Uri? = null,
    val isLoading: Boolean = false,
    val isDeleting: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val isAccountDeleted: Boolean = false
)

sealed class SettingsEvent {
    data class NameChanged(val name: String) : SettingsEvent()
    data class CityChanged(val city: String) : SettingsEvent()
    data class ProfessionChanged(val profession: String) : SettingsEvent()
    data class ProfileImageChanged(val uri: Uri?) : SettingsEvent()
    object SaveClicked : SettingsEvent()
    object DeleteAccountClicked : SettingsEvent()
}