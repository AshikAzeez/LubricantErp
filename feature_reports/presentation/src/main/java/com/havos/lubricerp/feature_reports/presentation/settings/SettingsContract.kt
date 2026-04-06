package com.havos.lubricerp.feature_reports.presentation.settings

import com.havos.lubricerp.core.common.ThemeMode
import com.havos.lubricerp.core.common.UiIntent
import com.havos.lubricerp.core.common.UiState

sealed interface SettingsIntent : UiIntent {
    data class ThemeChanged(val mode: ThemeMode) : SettingsIntent
}

data class SettingsUiState(
    val selectedThemeMode: ThemeMode = ThemeMode.SYSTEM,
    val profile: SettingsProfileUi? = null
) : UiState

data class SettingsProfileUi(
    val fullName: String,
    val email: String,
    val branchId: Long,
    val rolesText: String
)

sealed interface SettingsAction {
    data class ThemeSelected(val mode: ThemeMode) : SettingsAction
}
