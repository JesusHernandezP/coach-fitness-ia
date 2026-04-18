package com.fitnessaicoach.app.ui.screens.logs

import com.fitnessaicoach.app.data.repository.DashboardRepository
import com.fitnessaicoach.app.ui.MainDispatcherRule
import com.fitnessaicoach.app.ui.common.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class LogActivityViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repo: DashboardRepository

    @Before
    fun setup() {
        repo = mock()
    }

    @Test
    fun `submitActivity saves activity and clears form on success`() = runTest {
        whenever(
            repo.logActivity(
                date = "2026-04-18",
                steps = 9000,
                caloriesBurned = 420,
                notes = "Pierna y core",
            ),
        ).thenReturn(Result.success(Unit))

        val viewModel = LogActivityViewModel(repo) { "2026-04-18" }
        viewModel.updateSteps("9000")
        viewModel.updateCalories("420")
        viewModel.updateNotes("Pierna y core")
        viewModel.submit()
        advanceUntilIdle()

        verify(repo).logActivity(
            date = "2026-04-18",
            steps = 9000,
            caloriesBurned = 420,
            notes = "Pierna y core",
        )
        assertEquals("", viewModel.formState.value.steps)
        assertEquals("", viewModel.formState.value.caloriesBurned)
        assertEquals("", viewModel.formState.value.notes)
        assertEquals(UiState.Success(Unit), viewModel.submitState.value)
    }

    @Test
    fun `submitActivity exposes validation error when form is empty`() = runTest {
        val viewModel = LogActivityViewModel(repo) { "2026-04-18" }

        viewModel.submit()
        advanceUntilIdle()

        assertEquals(UiState.Error("Introduce pasos, calorias o una nota antes de guardar."), viewModel.submitState.value)
    }
}
