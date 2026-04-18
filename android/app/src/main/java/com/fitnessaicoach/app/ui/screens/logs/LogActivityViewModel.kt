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

data class LogActivityFormState(
    val date: String = "",
    val steps: String = "",
    val caloriesBurned: String = "",
    val notes: String = "",
) {
    fun canSubmit(): Boolean = steps.toIntOrNull() != null || caloriesBurned.toIntOrNull() != null || notes.isNotBlank()
}

@HiltViewModel
class LogActivityViewModel @Inject constructor(
    private val repo: DashboardRepository,
) : ViewModel() {
    private var todayProvider: () -> String = { LocalDate.now().toString() }

    constructor(
        repo: DashboardRepository,
        todayProvider: () -> String,
    ) : this(repo) {
        this.todayProvider = todayProvider
        _formState.value = LogActivityFormState(date = todayProvider())
    }

    private val _formState = MutableStateFlow(LogActivityFormState(date = todayProvider()))
    val formState: StateFlow<LogActivityFormState> = _formState.asStateFlow()

    private val _submitState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val submitState: StateFlow<UiState<Unit>> = _submitState.asStateFlow()

    fun updateSteps(value: String) {
        _formState.value = _formState.value.copy(steps = value.filter { it.isDigit() })
    }

    fun updateCalories(value: String) {
        _formState.value = _formState.value.copy(caloriesBurned = value.filter { it.isDigit() })
    }

    fun updateNotes(value: String) {
        _formState.value = _formState.value.copy(notes = value)
    }

    fun submit() {
        if (!_formState.value.canSubmit()) {
            _submitState.value = UiState.Error("Introduce pasos, calorias o una nota antes de guardar.")
            return
        }

        viewModelScope.launch {
            _submitState.value = UiState.Loading
            repo.logActivity(
                date = _formState.value.date,
                steps = _formState.value.steps.toIntOrNull(),
                caloriesBurned = _formState.value.caloriesBurned.toIntOrNull(),
                notes = _formState.value.notes.ifBlank { null },
            ).onSuccess {
                _formState.value = LogActivityFormState(date = todayProvider())
                _submitState.value = UiState.Success(Unit)
            }.onFailure {
                _submitState.value = UiState.Error(it.message ?: "No se pudo guardar la actividad")
            }
        }
    }

    fun clearSubmitState() {
        _submitState.value = UiState.Idle
    }
}
