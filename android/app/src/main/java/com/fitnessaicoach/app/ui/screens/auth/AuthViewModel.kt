package com.fitnessaicoach.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitnessaicoach.app.data.repository.AuthRepository
import com.fitnessaicoach.app.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val uiState: StateFlow<UiState<Unit>> = _uiState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            _uiState.value = repo.login(email, password)
                .fold(onSuccess = { UiState.Success(Unit) }, onFailure = { UiState.Error(it.message ?: "Error") })
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            _uiState.value = repo.register(email, password)
                .fold(onSuccess = { UiState.Success(Unit) }, onFailure = { UiState.Error(it.message ?: "Error") })
        }
    }

    fun resetState() { _uiState.value = UiState.Idle }
}
