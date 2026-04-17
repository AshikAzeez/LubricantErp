package com.havos.lubricerp.feature_reports.presentation.home

import com.havos.lubricerp.feature_reports.presentation.reports.ReportMenu

object ReportsTabReducer {
    fun reduceForProfileLoading(state: ReportsTabUiState, isLoading: Boolean): ReportsTabUiState {
        return state.copy(isProfileLoading = isLoading)
    }

    fun reduceForUser(state: ReportsTabUiState, name: String): ReportsTabUiState {
        return state.copy(greetingName = name, isProfileLoading = false)
    }

    fun reduceForMenuSelection(state: ReportsTabUiState, menu: ReportMenu?): ReportsTabUiState {
        return state.copy(selectedMenu = menu)
    }
}
