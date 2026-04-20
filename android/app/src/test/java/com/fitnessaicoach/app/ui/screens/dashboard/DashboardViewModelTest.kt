package com.fitnessaicoach.app.ui.screens.dashboard

import com.fitnessaicoach.app.data.network.TodaySnapshot
import com.fitnessaicoach.app.data.network.WeightPoint
import com.fitnessaicoach.app.data.network.WeeklySummary
import com.fitnessaicoach.app.data.repository.DashboardRepository
import com.fitnessaicoach.app.ui.MainDispatcherRule
import com.fitnessaicoach.app.ui.common.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    @Before
    fun setup() {
        repo = mock()
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
            steps = 8123,
            caloriesBurned = 410,
            currentWeightKg = 81.7,
            targetCalories = 2360.0,
        )

        whenever(repo.weightProgress()).thenReturn(Result.success(points))
        whenever(repo.weeklySummary()).thenReturn(Result.success(weeklySummary))
        whenever(repo.today()).thenReturn(Result.success(todaySnapshot))

        val viewModel = DashboardViewModel(repo) { "2026-04-18" }
        viewModel.loadDashboard()
        advanceUntilIdle()

        assertEquals(
            UiState.Success(
                DashboardContent(
                    weightProgress = points,
                    weeklySummary = weeklySummary,
                    todaySnapshot = todaySnapshot,
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
            steps = 0,
            caloriesBurned = 0,
            currentWeightKg = null,
            targetCalories = null,
        )
        val updatedTodaySnapshot = TodaySnapshot(
            steps = 7000,
            caloriesBurned = 350,
            currentWeightKg = null,
            targetCalories = null,
        )
        whenever(repo.weightProgress()).thenReturn(Result.success(emptyList()))
        whenever(repo.weeklySummary())
            .thenReturn(Result.success(initialWeeklySummary))
            .thenReturn(Result.success(updatedWeeklySummary))
        whenever(repo.today())
            .thenReturn(Result.success(initialTodaySnapshot))
            .thenReturn(Result.success(updatedTodaySnapshot))
        whenever(
            repo.logActivity(
                date = "2026-04-18",
                steps = 7000,
                caloriesBurned = 350,
                notes = "Entreno de fuerza",
            ),
        ).thenReturn(Result.success(Unit))

        val viewModel = DashboardViewModel(repo) { "2026-04-18" }
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
        whenever(repo.today()).thenReturn(Result.success(TodaySnapshot(0, 0, null, null)))

        val viewModel = DashboardViewModel(repo) { "2026-04-18" }
        viewModel.loadDashboard()
        advanceUntilIdle()

        assertEquals(UiState.Error("sin red"), viewModel.dashboardState.value)
        assertTrue(viewModel.submitState.value is UiState.Idle)
    }
}
