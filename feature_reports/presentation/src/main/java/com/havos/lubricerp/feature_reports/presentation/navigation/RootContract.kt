package com.havos.lubricerp.feature_reports.presentation.navigation

import com.havos.lubricerp.core.common.UiState

data class RootUiState(
    val isLoading: Boolean = true,
    val isAuthenticated: Boolean = false
) : UiState
