package com.havos.lubricerp.feature_reports.presentation.home

import com.havos.lubricerp.feature_reports.presentation.reports.ReportMenu

object HomeReducer {
    fun reduceForProfileLoading(state: HomeUiState, isLoading: Boolean): HomeUiState {
        return state.copy(isProfileLoading = isLoading)
    }

    fun reduceForUser(state: HomeUiState, name: String): HomeUiState {
        return state.copy(greetingName = name, isProfileLoading = false)
    }

    fun reduceForMenuSelection(state: HomeUiState, menu: ReportMenu?): HomeUiState {
        return state.copy(selectedMenu = menu)
    }
}
