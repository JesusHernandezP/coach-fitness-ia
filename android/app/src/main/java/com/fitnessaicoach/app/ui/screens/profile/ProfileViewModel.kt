package com.fitnessaicoach.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitnessaicoach.app.data.network.MetabolicProfile
import com.fitnessaicoach.app.data.network.NutritionTarget
import com.fitnessaicoach.app.data.repository.ProfileRepository
import com.fitnessaicoach.app.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileFormState(
    val age: Int = 0,
    val sex: String = "MALE",
    val heightCm: Double = 0.0,
    val currentWeightKg: Double = 0.0,
    val activityLevel: String = "SEDENTARY",
    val goal: String = "LOSE",
    val dietType: String = "STANDARD",
    val weeklyExerciseDays: Int = 0,
    val exerciseMinutes: Int = 0,
    val dailySteps: Int = 0,
) {
    fun toMetabolicProfile() = MetabolicProfile(
        age = age,
        sex = sex,
        heightCm = heightCm,
        currentWeightKg = currentWeightKg,
        activityLevel = activityLevel,
        goal = goal,
        dietType = dietType,
        weeklyExerciseDays = weeklyExerciseDays,
        exerciseMinutes = exerciseMinutes,
        dailySteps = dailySteps,
    )

    companion object {
        fun fromProfile(profile: MetabolicProfile) = ProfileFormState(
            age = profile.age,
            sex = profile.sex,
            heightCm = profile.heightCm,
            currentWeightKg = profile.currentWeightKg,
            activityLevel = profile.activityLevel,
            goal = profile.goal,
            dietType = profile.dietType,
            weeklyExerciseDays = profile.weeklyExerciseDays ?: 0,
            exerciseMinutes = profile.exerciseMinutes ?: 0,
            dailySteps = profile.dailySteps ?: 0,
        )
    }
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repo: ProfileRepository,
    private val authRepo: com.fitnessaicoach.app.data.repository.AuthRepository,
) : ViewModel() {

    private val _formState = MutableStateFlow(ProfileFormState())
    val formState: StateFlow<ProfileFormState> = _formState.asStateFlow()

    private val _isOnboarding = MutableStateFlow(true)
    val isOnboarding: StateFlow<Boolean> = _isOnboarding.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _targetsState = MutableStateFlow<UiState<NutritionTarget>>(UiState.Idle)
    val targetsState: StateFlow<UiState<NutritionTarget>> = _targetsState.asStateFlow()

    private val _saveState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val saveState: StateFlow<UiState<Unit>> = _saveState.asStateFlow()

    init { loadProfile() }

    fun reloadProfile() { loadProfile() }

    private fun loadProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            repo.getProfile()
                .onSuccess { profile ->
                    _formState.value = ProfileFormState.fromProfile(profile)
                    _isOnboarding.value = false
                    loadTargets()
                }
                .onFailure {
                    _formState.value = ProfileFormState()
                    _isOnboarding.value = true
                    _targetsState.value = UiState.Idle
                }
            _isLoading.value = false
        }
    }

    private suspend fun loadTargets() {
        _targetsState.value = UiState.Loading
        _targetsState.value = repo.getTargets().fold(
            onSuccess = { UiState.Success(it) },
            onFailure = { UiState.Error(it.message ?: "No se pudieron cargar los objetivos") },
        )
    }

    fun updateAge(value: String) {
        _formState.value = _formState.value.copy(age = value.toIntOrNull() ?: 0)
    }

    fun updateSex(value: String) {
        _formState.value = _formState.value.copy(sex = value)
    }

    fun updateHeightCm(value: String) {
        _formState.value = _formState.value.copy(heightCm = value.toDoubleOrNull() ?: 0.0)
    }

    fun updateCurrentWeightKg(value: String) {
        _formState.value = _formState.value.copy(currentWeightKg = value.toDoubleOrNull() ?: 0.0)
    }

    fun updateActivityLevel(value: String) {
        _formState.value = _formState.value.copy(activityLevel = value)
    }

    fun updateGoal(value: String) {
        _formState.value = _formState.value.copy(goal = value)
    }

    fun updateDietType(value: String) {
        _formState.value = _formState.value.copy(dietType = value)
    }

    fun updateWeeklyExerciseDays(value: String) {
        _formState.value = _formState.value.copy(weeklyExerciseDays = value.toIntOrNull() ?: 0)
    }

    fun updateExerciseMinutes(value: String) {
        _formState.value = _formState.value.copy(exerciseMinutes = value.toIntOrNull() ?: 0)
    }

    fun updateDailySteps(value: String) {
        _formState.value = _formState.value.copy(dailySteps = value.toIntOrNull() ?: 0)
    }

    fun saveProfile() {
        viewModelScope.launch {
            _saveState.value = UiState.Loading
            repo.saveProfile(_formState.value.toMetabolicProfile())
                .onSuccess { profile ->
                    _formState.value = ProfileFormState.fromProfile(profile)
                    _isOnboarding.value = false
                    _saveState.value = UiState.Success(Unit)
                    loadTargets()
                }
                .onFailure {
                    _saveState.value = UiState.Error(it.message ?: "No se pudo guardar el perfil")
                }
        }
    }

    fun clearSaveState() {
        _saveState.value = UiState.Idle
    }

    fun logout() {
        viewModelScope.launch { authRepo.logout() }
    }
}
