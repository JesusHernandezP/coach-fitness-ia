package com.fitnessaicoach.app.ui.screens.dashboard

import com.fitnessaicoach.app.data.health.HealthConnectAvailability
import com.fitnessaicoach.app.data.health.HealthConnectDailyActivity
import com.fitnessaicoach.app.data.health.HealthConnectSyncManager
import com.fitnessaicoach.app.data.network.TodaySnapshot
import com.fitnessaicoach.app.data.network.WeightPoint
import com.fitnessaicoach.app.data.network.WeeklySummary
import com.fitnessaicoach.app.data.network.DailyNutritionSummaryDto
import com.fitnessaicoach.app.data.network.ActivityLogRequest
import com.fitnessaicoach.app.data.network.MetabolicProfile
import com.fitnessaicoach.app.data.repository.DashboardRepository
import com.fitnessaicoach.app.data.repository.ProfileRepository
import com.fitnessaicoach.app.ui.MainDispatcherRule
import com.fitnessaicoach.app.ui.common.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repo: DashboardRepository
    private lateinit var profileRepo: ProfileRepository
    private lateinit var healthConnectSyncManager: HealthConnectSyncManager

    @Before
    fun setup() {
        repo = mock()
        profileRepo = mock()
        healthConnectSyncManager = mock()
        whenever(healthConnectSyncManager.availability()).thenReturn(HealthConnectAvailability.Available)
        runBlocking {
            whenever(profileRepo.getProfile()).thenReturn(
                Result.success(
                    MetabolicProfile(
                        displayName = "Alex",
                        age = 30,
                        sex = "MALE",
                        heightCm = 180.0,
                        currentWeightKg = 80.0,
                        activityLevel = "MODERATELY_ACTIVE",
                        goal = "MAINTAIN",
                        dietType = "STANDARD",
                        weeklyExerciseDays = 4,
                        exerciseMinutes = 45,
                        dailySteps = 9000,
                    ),
                ),
            )
        }
    }

    @Test
    fun `loadDashboard exposes dashboard metrics on success`() = runTest {
        val points = listOf(
            WeightPoint(loggedAt = "2026-04-14T00:00:00Z", weightKg = 82.4),
            WeightPoint(loggedAt = "2026-04-18T00:00:00Z", weightKg = 81.7),
        )
        val weeklySummary = WeeklySummary(
            stepsTotal = 54321,
            caloriesBurnedTotal = 2300,
            daysLogged = 6,
            avgSteps = 9053.5,
            weightDelta = -0.7,
        )
        val todaySnapshot = TodaySnapshot(
            targetCalories = 2360.0,
            consumedCalories = 1280.0,
            remainingCalories = 1080.0,
            targetProteinG = 160.0,
            consumedProteinG = 92.0,
            remainingProteinG = 68.0,
            steps = 8123,
            caloriesBurned = 410,
            currentWeightKg = 81.7,
        )
        val nutrition = DailyNutritionSummaryDto(
            date = "2026-04-18",
            targetCalories = 2360.0,
            consumedCalories = 1280.0,
            remainingCalories = 1080.0,
            targetProteinG = 160.0,
            consumedProteinG = 92.0,
            remainingProteinG = 68.0,
            targetCarbsG = 286.0,
            consumedCarbsG = 140.0,
            remainingCarbsG = 146.0,
            targetFatG = 64.0,
            consumedFatG = 41.0,
            remainingFatG = 23.0,
            activityCaloriesBurned = 430,
            netCalories = 850.0,
        )

        whenever(repo.weightProgress()).thenReturn(Result.success(points))
        whenever(repo.weeklySummary()).thenReturn(Result.success(weeklySummary))
        whenever(repo.today()).thenReturn(Result.success(todaySnapshot))
        whenever(repo.todayNutrition()).thenReturn(Result.success(nutrition))
        whenever(repo.todayFoodLogs()).thenReturn(Result.success(emptyList()))

        val viewModel = DashboardViewModel(repo, profileRepo, healthConnectSyncManager) { "2026-04-18" }
        viewModel.loadDashboard()
        advanceUntilIdle()

        assertEquals(
            UiState.Success(
                DashboardContent(
                    displayName = "Alex",
                    weightProgress = points,
                    weeklySummary = weeklySummary,
                    todaySnapshot = todaySnapshot,
                    dailyNutrition = nutrition,
                    todayFoodLogs = emptyList(),
                ),
            ),
            viewModel.dashboardState.value,
        )
        assertEquals("2026-04-18", viewModel.formState.value.date)
    }

    @Test
    fun `submitTodayActivity logs inline activity and refreshes dashboard`() = runTest {
        val initialWeeklySummary = WeeklySummary(
            stepsTotal = 40000,
            caloriesBurnedTotal = 1800,
            daysLogged = 5,
            avgSteps = 8000.0,
            weightDelta = null,
        )
        val updatedWeeklySummary = WeeklySummary(
            stepsTotal = 47000,
            caloriesBurnedTotal = 2150,
            daysLogged = 6,
            avgSteps = 7833.3,
            weightDelta = -0.5,
        )
        val initialTodaySnapshot = TodaySnapshot(
            targetCalories = null,
            consumedCalories = 0.0,
            remainingCalories = null,
            targetProteinG = null,
            consumedProteinG = 0.0,
            remainingProteinG = null,
            steps = 0,
            caloriesBurned = 0,
            currentWeightKg = null,
        )
        val updatedTodaySnapshot = TodaySnapshot(
            targetCalories = null,
            consumedCalories = 0.0,
            remainingCalories = null,
            targetProteinG = null,
            consumedProteinG = 0.0,
            remainingProteinG = null,
            steps = 7000,
            caloriesBurned = 350,
            currentWeightKg = null,
        )
        whenever(repo.weightProgress()).thenReturn(Result.success(emptyList()))
        whenever(repo.weeklySummary())
            .thenReturn(Result.success(initialWeeklySummary))
            .thenReturn(Result.success(updatedWeeklySummary))
        whenever(repo.today())
            .thenReturn(Result.success(initialTodaySnapshot))
            .thenReturn(Result.success(updatedTodaySnapshot))
        whenever(repo.todayNutrition()).thenReturn(
            Result.success(
                DailyNutritionSummaryDto(
                    date = "2026-04-18",
                    targetCalories = null,
                    consumedCalories = 0.0,
                    remainingCalories = null,
                    targetProteinG = null,
                    consumedProteinG = 0.0,
                    remainingProteinG = null,
                    targetCarbsG = null,
                    consumedCarbsG = 0.0,
                    remainingCarbsG = null,
                    targetFatG = null,
                    consumedFatG = 0.0,
                    remainingFatG = null,
                    activityCaloriesBurned = 0,
                    netCalories = 0.0,
                ),
            ),
        )
        whenever(repo.todayFoodLogs()).thenReturn(Result.success(emptyList()))
        whenever(
            repo.logActivity(
                date = "2026-04-18",
                steps = 7000,
                caloriesBurned = 350,
                notes = "Entreno de fuerza",
            ),
        ).thenReturn(Result.success(Unit))

        val viewModel = DashboardViewModel(repo, profileRepo, healthConnectSyncManager) { "2026-04-18" }
        viewModel.loadDashboard()
        advanceUntilIdle()

        viewModel.updateSteps("7000")
        viewModel.updateCaloriesBurned("350")
        viewModel.updateNotes("Entreno de fuerza")
        viewModel.submitTodayActivity()
        advanceUntilIdle()

        verify(repo).logActivity(
            date = "2026-04-18",
            steps = 7000,
            caloriesBurned = 350,
            notes = "Entreno de fuerza",
        )
        assertEquals(UiState.Success(Unit), viewModel.submitState.value)
        assertEquals(updatedWeeklySummary, viewModel.requireDashboardContent().weeklySummary)
        assertEquals(updatedTodaySnapshot, viewModel.requireDashboardContent().todaySnapshot)
        assertEquals("", viewModel.formState.value.steps)
        assertEquals("", viewModel.formState.value.caloriesBurned)
        assertEquals("", viewModel.formState.value.notes)
    }

    @Test
    fun `loadDashboard exposes error when repository fails`() = runTest {
        whenever(repo.weightProgress()).thenReturn(Result.failure(RuntimeException("sin red")))
        whenever(repo.weeklySummary()).thenReturn(Result.success(WeeklySummary(0, 0, 0, 0.0, null)))
        whenever(repo.today()).thenReturn(
            Result.success(
                TodaySnapshot(
                    targetCalories = null,
                    consumedCalories = 0.0,
                    remainingCalories = null,
                    targetProteinG = null,
                    consumedProteinG = 0.0,
                    remainingProteinG = null,
                    steps = 0,
                    caloriesBurned = 0,
                    currentWeightKg = null,
                ),
            ),
        )
        whenever(repo.todayNutrition()).thenReturn(
            Result.success(
                DailyNutritionSummaryDto(
                    date = "2026-04-18",
                    targetCalories = null,
                    consumedCalories = 0.0,
                    remainingCalories = null,
                    targetProteinG = null,
                    consumedProteinG = 0.0,
                    remainingProteinG = null,
                    targetCarbsG = null,
                    consumedCarbsG = 0.0,
                    remainingCarbsG = null,
                    targetFatG = null,
                    consumedFatG = 0.0,
                    remainingFatG = null,
                    activityCaloriesBurned = 0,
                    netCalories = 0.0,
                ),
            ),
        )
        whenever(repo.todayFoodLogs()).thenReturn(Result.success(emptyList()))

        val viewModel = DashboardViewModel(repo, profileRepo, healthConnectSyncManager) { "2026-04-18" }
        viewModel.loadDashboard()
        advanceUntilIdle()

        assertEquals(UiState.Error("sin red"), viewModel.dashboardState.value)
        assertTrue(viewModel.submitState.value is UiState.Idle)
    }

    @Test
    fun `syncTodayFromHealthConnect uploads activity and refreshes dashboard`() = runTest {
        val activity = HealthConnectDailyActivity("2026-04-18", 8500, 430)
        whenever(healthConnectSyncManager.readTodayActivity()).thenReturn(Result.success(activity))
        whenever(repo.syncDailyActivity(activity)).thenReturn(
            Result.success(
                ActivityLogRequest(
                    date = "2026-04-18",
                    steps = 8500,
                    caloriesBurned = 430,
                    source = "health_connect",
                ),
            ),
        )
        whenever(repo.weightProgress()).thenReturn(Result.success(emptyList()))
        whenever(repo.weeklySummary()).thenReturn(Result.success(WeeklySummary(0, 0, 0, 0.0, null)))
        whenever(repo.today()).thenReturn(
            Result.success(
                TodaySnapshot(
                    targetCalories = null,
                    consumedCalories = 0.0,
                    remainingCalories = null,
                    targetProteinG = null,
                    consumedProteinG = 0.0,
                    remainingProteinG = null,
                    steps = 8500,
                    caloriesBurned = 430,
                    currentWeightKg = null,
                ),
            ),
        )
        whenever(repo.todayNutrition()).thenReturn(
            Result.success(
                DailyNutritionSummaryDto(
                    date = "2026-04-18",
                    targetCalories = null,
                    consumedCalories = 0.0,
                    remainingCalories = null,
                    targetProteinG = null,
                    consumedProteinG = 0.0,
                    remainingProteinG = null,
                    targetCarbsG = null,
                    consumedCarbsG = 0.0,
                    remainingCarbsG = null,
                    targetFatG = null,
                    consumedFatG = 0.0,
                    remainingFatG = null,
                    activityCaloriesBurned = 430,
                    netCalories = 0.0,
                ),
            ),
        )
        whenever(repo.todayFoodLogs()).thenReturn(Result.success(emptyList()))

        val viewModel = DashboardViewModel(repo, profileRepo, healthConnectSyncManager) { "2026-04-18" }
        viewModel.syncTodayFromHealthConnect()
        advanceUntilIdle()

        verify(repo).syncDailyActivity(activity)
        assertEquals(
            UiState.Success("Actividad sincronizada: 8500 pasos y 430 kcal."),
            viewModel.healthSyncState.value,
        )
    }
}
