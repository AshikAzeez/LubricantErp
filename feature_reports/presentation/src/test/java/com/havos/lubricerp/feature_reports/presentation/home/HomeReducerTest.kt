package com.havos.lubricerp.feature_reports.presentation.home

import com.havos.lubricerp.feature_reports.presentation.reports.ReportMenu
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HomeReducerTest {

    @Test
    fun reduceForUser_updatesUsername() {
        // Given
        val initialState = HomeUiState(username = "")
        val newUsername = "John Doe"

        // When
        val result = HomeReducer.reduceForUser(initialState, newUsername)

        // Then
        assertEquals(newUsername, result.username)
    }

    @Test
    fun reduceForUser_preservesOtherState() {
        // Given
        val initialState = HomeUiState(
            username = "Old Name",
            cards = emptyList(),
            selectedMenu = ReportMenu.TANK_REPORTS
        )
        val newUsername = "John Doe"

        // When
        val result = HomeReducer.reduceForUser(initialState, newUsername)

        // Then
        assertEquals(newUsername, result.username)
        assertEquals(initialState.cards, result.cards)
        assertEquals(initialState.selectedMenu, result.selectedMenu)
    }

    @Test
    fun reduceForUser_withEmptyUsername_updatesUsername() {
        // Given
        val initialState = HomeUiState(username = "Old Name")
        val newUsername = ""

        // When
        val result = HomeReducer.reduceForUser(initialState, newUsername)

        // Then
        assertEquals("", result.username)
    }

    @Test
    fun reduceForMenuSelection_setsSelectedMenu() {
        // Given
        val initialState = HomeUiState(selectedMenu = null)
        val menu = ReportMenu.TANK_REPORTS

        // When
        val result = HomeReducer.reduceForMenuSelection(initialState, menu)

        // Then
        assertEquals(menu, result.selectedMenu)
    }

    @Test
    fun reduceForMenuSelection_clearsSelectedMenu() {
        // Given
        val initialState = HomeUiState(selectedMenu = ReportMenu.TANK_REPORTS)

        // When
        val result = HomeReducer.reduceForMenuSelection(initialState, null)

        // Then
        assertNull(result.selectedMenu)
    }

    @Test
    fun reduceForMenuSelection_preservesOtherState() {
        // Given
        val initialState = HomeUiState(
            username = "John Doe",
            cards = emptyList(),
            selectedMenu = null
        )
        val menu = ReportMenu.SALES_REPORTS

        // When
        val result = HomeReducer.reduceForMenuSelection(initialState, menu)

        // Then
        assertEquals(initialState.username, result.username)
        assertEquals(initialState.cards, result.cards)
        assertEquals(menu, result.selectedMenu)
    }

    @Test
    fun reduceForMenuSelection_replacesExistingSelection() {
        // Given
        val initialState = HomeUiState(selectedMenu = ReportMenu.TANK_REPORTS)
        val newMenu = ReportMenu.SALES_REPORTS

        // When
        val result = HomeReducer.reduceForMenuSelection(initialState, newMenu)

        // Then
        assertEquals(newMenu, result.selectedMenu)
    }
}
