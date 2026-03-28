package com.havos.lubricerp.feature_reports.presentation.home

import com.havos.lubricerp.feature_reports.presentation.reports.ReportMenu

object HomeReducer {
    fun reduceForUser(state: HomeUiState, username: String): HomeUiState {
        return state.copy(username = username)
    }

    fun reduceForMenuSelection(state: HomeUiState, menu: ReportMenu?): HomeUiState {
        return state.copy(selectedMenu = menu)
    }
}
