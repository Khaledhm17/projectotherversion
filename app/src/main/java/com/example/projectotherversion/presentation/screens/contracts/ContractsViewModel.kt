package com.example.projectotherversion.presentation.screens.contracts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectotherversion.domain.model.Contract
import com.example.projectotherversion.domain.usecase.contract.GetAllContractsForUserUseCase
import com.example.projectotherversion.domain.usecase.user.GetCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContractsViewModel @Inject constructor(
    private val getAllContractsForUserUseCase: GetAllContractsForUserUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ContractsState())
    val state = _state.asStateFlow()

    init {
        loadContracts()
    }

    private fun loadContracts() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val currentUser = getCurrentUserUseCase()
            if (currentUser != null) {
                getAllContractsForUserUseCase(currentUser.id)
                    .onEach { contracts ->
                        _state.update { it.copy(contracts = contracts, isLoading = false) }
                    }
                    .catch { e ->
                        _state.update { it.copy(error = e.message, isLoading = false) }
                    }
                    .launchIn(viewModelScope)
            } else {
                _state.update { it.copy(error = "المستخدم غير موجود", isLoading = false) }
            }
        }
    }
}

data class ContractsState(
    val contracts: List<Contract> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
