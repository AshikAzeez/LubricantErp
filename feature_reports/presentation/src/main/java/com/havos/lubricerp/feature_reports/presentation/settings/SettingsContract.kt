package com.havos.lubricerp.feature_reports.presentation.settings

import com.havos.lubricerp.core.common.ThemeMode
import com.havos.lubricerp.core.common.UiIntent
import com.havos.lubricerp.core.common.UiState

sealed interface SettingsIntent : UiIntent {
    data class ThemeChanged(val mode: ThemeMode) : SettingsIntent
}

data class SettingsUiState(
    val selectedThemeMode: ThemeMode = ThemeMode.SYSTEM
) : UiState

sealed interface SettingsAction {
    data class ThemeSelected(val mode: ThemeMode) : SettingsAction
}
