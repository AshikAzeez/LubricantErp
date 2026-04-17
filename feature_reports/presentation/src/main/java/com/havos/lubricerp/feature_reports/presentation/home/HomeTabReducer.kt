package com.havos.lubricerp.feature_reports.presentation.home

import com.havos.lubricerp.feature_reports.presentation.reports.ReportMenu

object HomeTabReducer {
    fun reduceForProfileLoading(state: HomeTabUiState, isLoading: Boolean): HomeTabUiState {
        return state.copy(isProfileLoading = isLoading)
    }

    fun reduceForUser(state: HomeTabUiState, name: String): HomeTabUiState {
        return state.copy(greetingName = name, isProfileLoading = false)
    }
}
