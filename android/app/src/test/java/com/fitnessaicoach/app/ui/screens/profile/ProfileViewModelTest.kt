package com.fitnessaicoach.app.ui.screens.profile

import com.fitnessaicoach.app.data.network.MetabolicProfile
import com.fitnessaicoach.app.data.network.NutritionTarget
import com.fitnessaicoach.app.data.repository.AuthRepository
import com.fitnessaicoach.app.data.repository.ProfileRepository
import com.fitnessaicoach.app.ui.MainDispatcherRule
import com.fitnessaicoach.app.ui.common.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repo: ProfileRepository
    private lateinit var authRepo: AuthRepository

    @Before
    fun setup() {
        repo = mock()
        authRepo = mock()
    }

    @Test
    fun `loadProfile fills form and targets when profile exists`() = runTest {
        val profile = MetabolicProfile(
            age = 30,
            sex = "MALE",
            heightCm = 180.0,
            currentWeightKg = 82.5,
            activityLevel = "MODERATE",
            goal = "LOSE",
            dietType = "STANDARD",
            weeklyExerciseDays = 4,
            exerciseMinutes = 50,
            dailySteps = 9000,
        )
        val targets = NutritionTarget(2200.0, 160.0, 210.0, 70.0)
        whenever(repo.getProfile()).thenReturn(Result.success(profile))
        whenever(repo.getTargets()).thenReturn(Result.success(targets))

        val viewModel = ProfileViewModel(repo, authRepo)
        advanceUntilIdle()

        assertEquals(30, viewModel.formState.value.age)
        assertEquals("MALE", viewModel.formState.value.sex)
        assertEquals("MODERATE", viewModel.formState.value.activityLevel)
        assertFalse(viewModel.isOnboarding.value)
        assertEquals(UiState.Success(targets), viewModel.targetsState.value)
    }

    @Test
    fun `loadProfile uses defaults and keeps onboarding when profile is missing`() = runTest {
        whenever(repo.getProfile()).thenReturn(Result.failure(RuntimeException("404")))

        val viewModel = ProfileViewModel(repo, authRepo)
        advanceUntilIdle()

        assertTrue(viewModel.isOnboarding.value)
        assertEquals(ProfileFormState(), viewModel.formState.value)
        assertEquals(UiState.Idle, viewModel.targetsState.value)
        verify(repo, never()).getTargets()
    }

    @Test
    fun `saveProfile persists form and refreshes targets on success`() = runTest {
        val savedProfile = MetabolicProfile(
            age = 28,
            sex = "FEMALE",
            heightCm = 165.0,
            currentWeightKg = 61.0,
            activityLevel = "LIGHT",
            goal = "MAINTAIN",
            dietType = "VEGETARIAN",
            weeklyExerciseDays = 3,
            exerciseMinutes = 40,
            dailySteps = 7000,
        )
        val targets = NutritionTarget(1950.0, 120.0, 220.0, 55.0)
        whenever(repo.getProfile()).thenReturn(Result.failure(RuntimeException("404")))
        whenever(repo.saveProfile(any())).thenReturn(Result.success(savedProfile))
        whenever(repo.getTargets()).thenReturn(Result.success(targets))

        val viewModel = ProfileViewModel(repo, authRepo)
        advanceUntilIdle()

        viewModel.updateAge("28")
        viewModel.updateSex("FEMALE")
        viewModel.updateHeightCm("165")
        viewModel.updateCurrentWeightKg("61")
        viewModel.updateActivityLevel("LIGHT")
        viewModel.updateGoal("MAINTAIN")
        viewModel.updateDietType("VEGETARIAN")
        viewModel.updateWeeklyExerciseDays("3")
        viewModel.updateExerciseMinutes("40")
        viewModel.updateDailySteps("7000")

        viewModel.saveProfile()
        advanceUntilIdle()

        assertEquals(UiState.Success(Unit), viewModel.saveState.value)
        assertFalse(viewModel.isOnboarding.value)
        assertEquals(UiState.Success(targets), viewModel.targetsState.value)
        verify(repo).saveProfile(savedProfile)
    }

    @Test
    fun `saveProfile exposes error when repository fails`() = runTest {
        whenever(repo.getProfile()).thenReturn(Result.failure(RuntimeException("404")))
        whenever(repo.saveProfile(any())).thenReturn(Result.failure(RuntimeException("boom")))

        val viewModel = ProfileViewModel(repo, authRepo)
        advanceUntilIdle()

        viewModel.updateAge("28")
        viewModel.updateSex("FEMALE")
        viewModel.updateHeightCm("165")
        viewModel.updateCurrentWeightKg("61")
        viewModel.saveProfile()
        advanceUntilIdle()

        assertEquals(UiState.Error("boom"), viewModel.saveState.value)
        assertTrue(viewModel.isOnboarding.value)
        verify(repo, never()).getTargets()
    }
}
