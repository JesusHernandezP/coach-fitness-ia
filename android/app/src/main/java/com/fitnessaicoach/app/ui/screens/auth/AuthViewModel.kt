package com.fitnessaicoach.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitnessaicoach.app.data.repository.AuthRepository
import com.fitnessaicoach.app.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
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
                .fold(onSuccess = { UiState.Success(Unit) }, onFailure = { UiState.Error(it.toAuthMessage(isLogin = true)) })
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            _uiState.value = repo.register(email, password)
                .fold(onSuccess = { UiState.Success(Unit) }, onFailure = { UiState.Error(it.toAuthMessage(isLogin = false)) })
        }
    }

    fun resetState() { _uiState.value = UiState.Idle }

    private fun Throwable.toAuthMessage(isLogin: Boolean): String = when {
        this is HttpException && code() == 400 -> if (isLogin) "Correo o contraseña no válidos" else "Datos inválidos. Verifica el formato del correo y que la contraseña tenga al menos 6 caracteres"
        this is HttpException && code() == 401 -> "Correo o contraseña incorrectos"
        this is HttpException && code() == 409 -> "Ya existe una cuenta con ese correo"
        message?.contains("connect", ignoreCase = true) == true || message?.contains("failed", ignoreCase = true) == true -> "Sin conexión al servidor. Verifica tu red"
        else -> "Error inesperado. Inténtalo de nuevo"
    }
}
