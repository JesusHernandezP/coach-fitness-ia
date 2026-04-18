package com.fitnessaicoach.app.ui.screens.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitnessaicoach.app.data.repository.DashboardRepository
import com.fitnessaicoach.app.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LogWeightFormState(
    val weight: String = "",
    val date: String = "",
)

@HiltViewModel
class LogWeightViewModel @Inject constructor(
    private val repo: DashboardRepository,
) : ViewModel() {
    private var todayProvider: () -> String = { LocalDate.now().toString() }

    constructor(
        repo: DashboardRepository,
        todayProvider: () -> String,
    ) : this(repo) {
        this.todayProvider = todayProvider
        _formState.value = LogWeightFormState(date = todayProvider())
    }

    private val _formState = MutableStateFlow(LogWeightFormState(date = todayProvider()))
    val formState: StateFlow<LogWeightFormState> = _formState.asStateFlow()

    private val _submitState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val submitState: StateFlow<UiState<Unit>> = _submitState.asStateFlow()

    fun updateWeight(value: String) {
        _formState.value = _formState.value.copy(weight = value.filter { it.isDigit() || it == '.' }.take(6))
    }

    fun submit() {
        val weight = _formState.value.weight.toDoubleOrNull()
        if (weight == null || weight <= 0.0) {
            _submitState.value = UiState.Error("Introduce un peso valido en kg.")
            return
        }

        viewModelScope.launch {
            _submitState.value = UiState.Loading
            repo.addWeight(weight, _formState.value.date)
                .onSuccess {
                    _formState.value = LogWeightFormState(date = todayProvider())
                    _submitState.value = UiState.Success(Unit)
                }
                .onFailure {
                    _submitState.value = UiState.Error(it.message ?: "No se pudo guardar el peso")
                }
        }
    }

    fun clearSubmitState() {
        _submitState.value = UiState.Idle
    }
}
