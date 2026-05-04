package com.example.projectotherversion.presentation.screens.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectotherversion.domain.model.User
import com.example.projectotherversion.domain.usecase.auth.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state = _state.asStateFlow()

    fun onEvent(event: RegisterEvent) {
        when (event) {
            is RegisterEvent.NameChanged -> _state.update { it.copy(name = event.name) }
            is RegisterEvent.EmailChanged -> _state.update { it.copy(email = event.email) }
            is RegisterEvent.PasswordChanged -> _state.update { it.copy(password = event.password) }
            is RegisterEvent.CityChanged -> _state.update { it.copy(city = event.city) }
            is RegisterEvent.RoleChanged -> _state.update { it.copy(role = event.role) }
            is RegisterEvent.ProfessionChanged -> _state.update { it.copy(profession = event.profession) }
            RegisterEvent.RegisterClicked -> register()
        }
    }

    private fun register() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val user = User(
                name = _state.value.name,
                email = _state.value.email,
                city = _state.value.city,
                role = _state.value.role,
                profession = if (_state.value.role == "CRAFTSMAN") _state.value.profession else null
            )

            val result = registerUseCase(_state.value.email, _state.value.password, user)
            _state.update { it.copy(isLoading = false) }

            result.onSuccess { registeredUser ->
                _state.update { it.copy(user = registeredUser, isSuccess = true) }
            }.onFailure { error ->
                _state.update { it.copy(error = error.message) }
            }
        }
    }
}

data class RegisterState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val city: String = "الوادي",
    val role: String = "CLIENT",
    val profession: String = "بناء",
    val isLoading: Boolean = false,
    val error: String? = null,
    val user: User? = null,
    val isSuccess: Boolean = false
)

sealed class RegisterEvent {
    data class NameChanged(val name: String) : RegisterEvent()
    data class EmailChanged(val email: String) : RegisterEvent()
    data class PasswordChanged(val password: String) : RegisterEvent()
    data class CityChanged(val city: String) : RegisterEvent()
    data class RoleChanged(val role: String) : RegisterEvent()
    data class ProfessionChanged(val profession: String) : RegisterEvent()
    object RegisterClicked : RegisterEvent()
}