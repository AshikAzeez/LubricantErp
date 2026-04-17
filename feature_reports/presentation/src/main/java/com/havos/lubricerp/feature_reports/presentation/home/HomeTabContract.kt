package com.havos.lubricerp.feature_reports.presentation.home

import com.havos.lubricerp.core.common.UiIntent
import com.havos.lubricerp.core.common.UiState

sealed interface HomeTabIntent : UiIntent

data class HomeTabUiState(
    val greetingName: String = "",
    val isProfileLoading: Boolean = false
) : UiState

sealed interface HomeTabEffect
