package com.havos.lubricerp.feature_reports.presentation.home

import app.cash.turbine.test
import com.havos.lubricerp.feature_reports.domain.model.AuthSession
import com.havos.lubricerp.feature_reports.domain.usecase.LogoutUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.ObserveSessionUseCase
import com.havos.lubricerp.feature_reports.presentation.MainDispatcherRule
import com.havos.lubricerp.feature_reports.presentation.reports.ReportMenu
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val observeSessionUseCase: ObserveSessionUseCase = mock()
    private val logoutUseCase: LogoutUseCase = mock()

    private fun createViewModel(): HomeViewModel {
        return HomeViewModel(
            observeSessionUseCase = observeSessionUseCase,
            logoutUseCase = logoutUseCase
        )
    }

    @Test
    fun initialState_hasAllReportMenuCards() = runTest {
        // Given
        whenever(observeSessionUseCase()).thenReturn(flowOf(null))

        // When
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        val expectedCardCount = ReportMenu.entries.size
        assertEquals(expectedCardCount, viewModel.state.value.cards.size)
    }

    @Test
    fun initialState_usernameIsEmpty() = runTest {
        // Given
        whenever(observeSessionUseCase()).thenReturn(flowOf(null))

        // When
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        assertEquals("", viewModel.state.value.username)
    }

    @Test
    fun initialState_selectedMenuIsNull() = runTest {
        // Given
        whenever(observeSessionUseCase()).thenReturn(flowOf(null))

        // When
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        assertNull(viewModel.state.value.selectedMenu)
    }

    @Test
    fun observeSession_updatesUsername() = runTest {
        // Given
        val session = AuthSession(username = "john.doe@example.com", token = "token123")
        whenever(observeSessionUseCase()).thenReturn(flowOf(session))

        // When
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        assertEquals("John Doe", viewModel.state.value.username)
    }

    @Test
    fun observeSession_withNullSession_setsEmptyUsername() = runTest {
        // Given
        whenever(observeSessionUseCase()).thenReturn(flowOf(null))

        // When
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        assertEquals("", viewModel.state.value.username)
    }

    @Test
    fun cardClicked_withSingleSubMenu_emitsOpenReportEffect() = runTest {
        // Given
        whenever(observeSessionUseCase()).thenReturn(flowOf(null))
        val viewModel = createViewModel()
        advanceUntilIdle()
        val menuWithSingleSubMenu = ReportMenu.RAW_MATERIAL_STOCK

        // When & Then
        viewModel.effect.test {
            viewModel.onIntent(HomeIntent.CardClicked(menuWithSingleSubMenu))
            advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is HomeEffect.OpenReport)
            assertEquals(
                ReportMenu.RAW_MATERIAL_STOCK.subMenus.first(),
                (effect as HomeEffect.OpenReport).reportItem
            )
        }
    }

    @Test
    fun cardClicked_withMultipleSubMenus_updatesSelectedMenuState() = runTest {
        // Given
        whenever(observeSessionUseCase()).thenReturn(flowOf(null))
        val viewModel = createViewModel()
        advanceUntilIdle()
        val menuWithMultipleSubMenus = ReportMenu.TANK_REPORTS

        // When
        viewModel.onIntent(HomeIntent.CardClicked(menuWithMultipleSubMenus))

        // Then
        assertNotNull(viewModel.state.value.selectedMenu)
        assertEquals(menuWithMultipleSubMenus, viewModel.state.value.selectedMenu)
    }

    @Test
    fun bottomSheetDismissed_clearsSelectedMenu() = runTest {
        // Given
        whenever(observeSessionUseCase()).thenReturn(flowOf(null))
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onIntent(HomeIntent.CardClicked(ReportMenu.TANK_REPORTS))

        // When
        viewModel.onIntent(HomeIntent.BottomSheetDismissed)

        // Then
        assertNull(viewModel.state.value.selectedMenu)
    }

    @Test
    fun logoutClicked_callsLogoutAndEmitsNavigateToLoginEffect() = runTest {
        // Given
        whenever(observeSessionUseCase()).thenReturn(flowOf(null))
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When & Then
        viewModel.effect.test {
            viewModel.onIntent(HomeIntent.LogoutClicked)
            advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is HomeEffect.NavigateToLogin)
        }
        verify(logoutUseCase).invoke()
    }

    @Test
    fun onSubMenuClicked_emitsOpenReportEffect() = runTest {
        // Given
        whenever(observeSessionUseCase()).thenReturn(flowOf(null))
        val viewModel = createViewModel()
        advanceUntilIdle()
        val reportItem = ReportMenu.TANK_REPORTS.subMenus.first()

        // When & Then
        viewModel.effect.test {
            viewModel.onSubMenuClicked(reportItem)
            advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is HomeEffect.OpenReport)
            assertEquals(reportItem, (effect as HomeEffect.OpenReport).reportItem)
        }
    }

    @Test
    fun onSubMenuClicked_clearsSelectedMenu() = runTest {
        // Given
        whenever(observeSessionUseCase()).thenReturn(flowOf(null))
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onIntent(HomeIntent.CardClicked(ReportMenu.TANK_REPORTS))
        val reportItem = ReportMenu.TANK_REPORTS.subMenus.first()

        // When
        viewModel.onSubMenuClicked(reportItem)
        advanceUntilIdle()

        // Then
        assertNull(viewModel.state.value.selectedMenu)
    }

    // Tests for displayNameFromUsername logic through session observation
    @Test
    fun displayNameFromUsername_withSimpleUsername_returnsCapitalizedName() = runTest {
        // Given
        val session = AuthSession(username = "john.doe@example.com", token = "token")
        whenever(observeSessionUseCase()).thenReturn(flowOf(session))

        // When
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        assertEquals("John Doe", viewModel.state.value.username)
    }

    @Test
    fun displayNameFromUsername_withUnderscore_replacesWithSpace() = runTest {
        // Given
        val session = AuthSession(username = "john_doe@example.com", token = "token")
        whenever(observeSessionUseCase()).thenReturn(flowOf(session))

        // When
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        assertEquals("John Doe", viewModel.state.value.username)
    }

    @Test
    fun displayNameFromUsername_withHyphen_replacesWithSpace() = runTest {
        // Given
        val session = AuthSession(username = "john-doe@example.com", token = "token")
        whenever(observeSessionUseCase()).thenReturn(flowOf(session))

        // When
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        assertEquals("John Doe", viewModel.state.value.username)
    }

    @Test
    fun displayNameFromUsername_withBlankUsername_returnsEmptyString() = runTest {
        // Given
        val session = AuthSession(username = "", token = "token")
        whenever(observeSessionUseCase()).thenReturn(flowOf(session))

        // When
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        assertEquals("", viewModel.state.value.username)
    }

    @Test
    fun displayNameFromUsername_withOnlyAtSymbol_returnsUsername() = runTest {
        // Given
        val session = AuthSession(username = "@example.com", token = "token")
        whenever(observeSessionUseCase()).thenReturn(flowOf(session))

        // When
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        assertEquals("@example.com", viewModel.state.value.username)
    }
}
