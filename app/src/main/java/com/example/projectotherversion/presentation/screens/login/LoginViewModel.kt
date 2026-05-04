package com.example.projectotherversion.presentation.screens.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectotherversion.domain.usecase.auth.LoginUseCase
import com.example.projectotherversion.domain.usecase.auth.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val logoutUseCase: LogoutUseCase // أضفنا هذا لإنهاء جلسة المحظور
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> {
                _state.update { it.copy(email = event.email) }
            }
            is LoginEvent.PasswordChanged -> {
                _state.update { it.copy(password = event.password) }
            }
            LoginEvent.LoginClicked -> login()
        }
    }

    private fun login() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = loginUseCase(_state.value.email, _state.value.password)

            result.onSuccess { user ->
                // التحقق من حالة الحظر فوراً
                if (user?.isBlocked == true) {
                    // 1. تسجيل الخروج من السيرفر فوراً لقتل الجلسة
                    logoutUseCase()

                    // 2. تحديث الحالة برسالة الخطأ ومنع الدخول
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "عذراً، هذا الحساب محظور من قبل الإدارة.",
                            isSuccess = false // هذا أهم سطر لمنع الانتقال للشاشة التالية
                        )
                    }
                } else {
                    // إذا لم يكن محظوراً، نسمح بالدخول
                    _state.update {
                        it.copy(isLoading = false, user = user, isSuccess = true)
                    }
                }
            }.onFailure { error ->
                _state.update { it.copy(isLoading = false, error = error.message) }
            }
        }
    }
}

// تعريف الـ State والـ Event خارج الكلاس ولكن في نفس الملف لسهولة الوصول
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