package com.fitnessaicoach.app.ui.screens.logs

import com.fitnessaicoach.app.data.network.WeightLog
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
class LogWeightViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repo: DashboardRepository

    @Before
    fun setup() {
        repo = mock()
    }

    @Test
    fun `submitWeight saves weight and clears form on success`() = runTest {
        whenever(repo.addWeight(81.4, "2026-04-18")).thenReturn(
            Result.success(WeightLog(1L, 81.4, "2026-04-18")),
        )

        val viewModel = LogWeightViewModel(repo) { "2026-04-18" }
        viewModel.updateWeight("81.4")
        viewModel.submit()
        advanceUntilIdle()

        verify(repo).addWeight(81.4, "2026-04-18")
        assertEquals("", viewModel.formState.value.weight)
        assertEquals(UiState.Success(Unit), viewModel.submitState.value)
    }

    @Test
    fun `submitWeight exposes validation error when weight is missing`() = runTest {
        val viewModel = LogWeightViewModel(repo) { "2026-04-18" }

        viewModel.submit()
        advanceUntilIdle()

        assertEquals(UiState.Error("Introduce un peso valido en kg."), viewModel.submitState.value)
    }
}
