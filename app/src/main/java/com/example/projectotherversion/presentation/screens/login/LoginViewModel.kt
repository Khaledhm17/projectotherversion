package com.example.projectotherversion.presentation.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectotherversion.domain.usecase.auth.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> {
                _state.update { it.copy(email = event.email, error = null) }
            }
            is LoginEvent.PasswordChanged -> {
                _state.update { it.copy(password = event.password, error = null) }
            }
            LoginEvent.LoginClicked -> login()
        }
    }

    private fun login() {
        // التأكد من أن الحقول ليست فارغة قبل بدء العملية
        if (_state.value.email.isBlank() || _state.value.password.isBlank()) {
            _state.update { it.copy(error = "يرجى إدخال البريد الإلكتروني وكلمة المرور") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            /*
               ملاحظة تقنية:
               داخل LoginUseCase، يجب أن يتم جلب بيانات المستخدم من جدول (profiles/users)
               والتحقق من قيمة عمود (is_blocked).
               إذا كان true، يجب أن يعيد الـ UseCase خطأ مخصص (Exception).
            */
            val result = loginUseCase(_state.value.email, _state.value.password)

            result.onSuccess { user ->
                // إذا نجح الدخول وكان المستخدم غير محظور
                _state.update {
                    it.copy(
                        isLoading = false,
                        user = user,
                        isSuccess = true,
                        error = null
                    )
                }
            }.onFailure { error ->
                // هنا يتم استقبال الخطأ سواء كان (كلمة مرور خاطئة) أو (حساب محظور)
                // القادم من قاعدة بيانات Supabase
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "حدث خطأ غير متوقع"
                    )
                }
            }
        }
    }
}

/**
 * تم إضافة خاصية الحظر في مودل المستخدم لضمان تتبع الحالة
 */
data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val user: com.example.projectotherversion.domain.model.User? = null,
    val isSuccess: Boolean = false
)

sealed class LoginEvent {
    data class EmailChanged(val email: String) : LoginEvent()
    data class PasswordChanged(val password: String) : LoginEvent()
    object LoginClicked : LoginEvent()
}