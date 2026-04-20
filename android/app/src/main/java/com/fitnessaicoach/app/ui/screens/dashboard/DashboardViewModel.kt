package com.fitnessaicoach.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitnessaicoach.app.data.network.TodaySnapshot
import com.fitnessaicoach.app.data.network.WeightPoint
import com.fitnessaicoach.app.data.network.WeeklySummary
import com.fitnessaicoach.app.data.repository.DashboardRepository
import com.fitnessaicoach.app.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardContent(
    val weightProgress: List<WeightPoint>,
    val weeklySummary: WeeklySummary,
    val todaySnapshot: TodaySnapshot,
)

data class ActivityFormState(
    val date: String = "",
    val steps: String = "",
    val caloriesBurned: String = "",
    val notes: String = "",
) {
    fun canSubmit(): Boolean = steps.toIntOrNull() != null || caloriesBurned.toIntOrNull() != null || notes.isNotBlank()
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repo: DashboardRepository,
) : ViewModel() {
    private var todayProvider: () -> String = { LocalDate.now().toString() }

    constructor(
        repo: DashboardRepository,
        todayProvider: () -> String,
    ) : this(repo) {
        this.todayProvider = todayProvider
        _formState.value = ActivityFormState(date = todayProvider())
    }

    private val _dashboardState = MutableStateFlow<UiState<DashboardContent>>(UiState.Loading)
    val dashboardState: StateFlow<UiState<DashboardContent>> = _dashboardState.asStateFlow()

    private val _formState = MutableStateFlow(ActivityFormState(date = todayProvider()))
    val formState: StateFlow<ActivityFormState> = _formState.asStateFlow()

    private val _submitState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val submitState: StateFlow<UiState<Unit>> = _submitState.asStateFlow()

    fun loadDashboard() {
        viewModelScope.launch {
            _dashboardState.value = UiState.Loading
            _dashboardState.value = runCatching {
                DashboardContent(
                    weightProgress = repo.weightProgress().getOrThrow(),
                    weeklySummary = repo.weeklySummary().getOrThrow(),
                    todaySnapshot = repo.today().getOrThrow(),
                )
            }.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "No se pudo cargar el panel") },
            )
        }
    }

    fun updateSteps(value: String) {
        _formState.value = _formState.value.copy(steps = value.filter { it.isDigit() })
    }

    fun updateCaloriesBurned(value: String) {
        _formState.value = _formState.value.copy(caloriesBurned = value.filter { it.isDigit() })
    }

    fun updateNotes(value: String) {
        _formState.value = _formState.value.copy(notes = value)
    }

    fun submitTodayActivity() {
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
                _submitState.value = UiState.Success(Unit)
                _formState.value = ActivityFormState(date = todayProvider())
                loadDashboard()
            }.onFailure {
                _submitState.value = UiState.Error(it.message ?: "No se pudo registrar la actividad")
            }
        }
    }

    fun clearSubmitState() {
        _submitState.value = UiState.Idle
    }

    fun requireDashboardContent(): DashboardContent =
        (dashboardState.value as? UiState.Success)?.data
            ?: error("Dashboard content is not available")
}
