package com.fitnessaicoach.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitnessaicoach.app.data.health.HealthConnectAvailability
import com.fitnessaicoach.app.data.health.HealthConnectSyncManager
import com.fitnessaicoach.app.data.network.DailyNutritionSummaryDto
import com.fitnessaicoach.app.data.network.FoodLogDto
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
    val dailyNutrition: DailyNutritionSummaryDto,
    val todayFoodLogs: List<FoodLogDto>,
)

data class ActivityFormState(
    val date: String = "",
    val steps: String = "",
    val caloriesBurned: String = "",
    val notes: String = "",
) {
    fun canSubmit(): Boolean = steps.toIntOrNull() != null || caloriesBurned.toIntOrNull() != null || notes.isNotBlank()
}

data class FoodFormState(
    val date: String = "",
    val mealType: String = "breakfast",
    val description: String = "",
    val calories: String = "",
    val proteinG: String = "",
    val carbsG: String = "",
    val fatG: String = "",
) {
    fun canSubmit(): Boolean = description.isNotBlank() && calories.toDoubleOrNull() != null
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repo: DashboardRepository,
    private val healthConnectSyncManager: HealthConnectSyncManager,
) : ViewModel() {
    private var todayProvider: () -> String = { LocalDate.now().toString() }

    constructor(
        repo: DashboardRepository,
        healthConnectSyncManager: HealthConnectSyncManager,
        todayProvider: () -> String,
    ) : this(repo, healthConnectSyncManager) {
        this.todayProvider = todayProvider
        _formState.value = ActivityFormState(date = todayProvider())
        _foodFormState.value = FoodFormState(date = todayProvider())
    }

    private val _dashboardState = MutableStateFlow<UiState<DashboardContent>>(UiState.Loading)
    val dashboardState: StateFlow<UiState<DashboardContent>> = _dashboardState.asStateFlow()

    private val _formState = MutableStateFlow(ActivityFormState(date = todayProvider()))
    val formState: StateFlow<ActivityFormState> = _formState.asStateFlow()

    private val _foodFormState = MutableStateFlow(FoodFormState(date = todayProvider()))
    val foodFormState: StateFlow<FoodFormState> = _foodFormState.asStateFlow()

    private val _submitState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val submitState: StateFlow<UiState<Unit>> = _submitState.asStateFlow()

    private val _foodSubmitState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val foodSubmitState: StateFlow<UiState<Unit>> = _foodSubmitState.asStateFlow()

    private val _healthSyncState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val healthSyncState: StateFlow<UiState<String>> = _healthSyncState.asStateFlow()

    private val _healthAvailability = MutableStateFlow(healthConnectSyncManager.availability())
    val healthAvailability: StateFlow<HealthConnectAvailability> = _healthAvailability.asStateFlow()

    fun loadDashboard() {
        viewModelScope.launch {
            _dashboardState.value = UiState.Loading
            _dashboardState.value = runCatching {
                DashboardContent(
                    weightProgress = repo.weightProgress().getOrThrow(),
                    weeklySummary = repo.weeklySummary().getOrThrow(),
                    todaySnapshot = repo.today().getOrThrow(),
                    dailyNutrition = repo.todayNutrition().getOrThrow(),
                    todayFoodLogs = repo.todayFoodLogs().getOrThrow(),
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

    fun updateFoodMealType(value: String) {
        _foodFormState.value = _foodFormState.value.copy(mealType = value)
    }

    fun updateFoodDescription(value: String) {
        _foodFormState.value = _foodFormState.value.copy(description = value)
    }

    fun updateFoodCalories(value: String) {
        _foodFormState.value = _foodFormState.value.copy(calories = value.filter { it.isDigit() || it == '.' })
    }

    fun updateFoodProtein(value: String) {
        _foodFormState.value = _foodFormState.value.copy(proteinG = value.filter { it.isDigit() || it == '.' })
    }

    fun updateFoodCarbs(value: String) {
        _foodFormState.value = _foodFormState.value.copy(carbsG = value.filter { it.isDigit() || it == '.' })
    }

    fun updateFoodFat(value: String) {
        _foodFormState.value = _foodFormState.value.copy(fatG = value.filter { it.isDigit() || it == '.' })
    }

    fun submitFoodLog() {
        if (!_foodFormState.value.canSubmit()) {
            _foodSubmitState.value = UiState.Error("Describe la comida e introduce las calorias.")
            return
        }

        viewModelScope.launch {
            val form = _foodFormState.value
            _foodSubmitState.value = UiState.Loading
            repo.createFoodLog(
                date = form.date,
                mealType = form.mealType,
                description = form.description.trim(),
                calories = form.calories.toDouble(),
                proteinG = form.proteinG.toDoubleOrNull(),
                carbsG = form.carbsG.toDoubleOrNull(),
                fatG = form.fatG.toDoubleOrNull(),
            ).onSuccess {
                _foodSubmitState.value = UiState.Success(Unit)
                _foodFormState.value = FoodFormState(date = todayProvider())
                loadDashboard()
            }.onFailure {
                _foodSubmitState.value = UiState.Error(it.message ?: "No se pudo guardar la comida")
            }
        }
    }

    fun clearSubmitState() {
        _submitState.value = UiState.Idle
    }

    fun clearFoodSubmitState() {
        _foodSubmitState.value = UiState.Idle
    }

    fun refreshHealthAvailability() {
        _healthAvailability.value = healthConnectSyncManager.availability()
    }

    suspend fun hasHealthPermissions(): Boolean = healthConnectSyncManager.hasPermissions()

    fun syncTodayFromHealthConnect() {
        viewModelScope.launch {
            _healthSyncState.value = UiState.Loading
            healthConnectSyncManager.readTodayActivity()
                .fold(
                    onSuccess = { activity ->
                        repo.syncDailyActivity(activity)
                            .onSuccess {
                                _healthSyncState.value =
                                    UiState.Success("Actividad sincronizada: ${activity.steps} pasos y ${activity.caloriesBurned} kcal.")
                                loadDashboard()
                            }
                            .onFailure {
                                _healthSyncState.value = UiState.Error(it.message ?: "No se pudo enviar la actividad al backend")
                            }
                    },
                    onFailure = {
                        _healthSyncState.value = UiState.Error(it.message ?: "No se pudo leer Health Connect")
                    },
                )
        }
    }

    fun clearHealthSyncState() {
        _healthSyncState.value = UiState.Idle
    }

    fun requireDashboardContent(): DashboardContent =
        (dashboardState.value as? UiState.Success)?.data
            ?: error("Dashboard content is not available")
}
