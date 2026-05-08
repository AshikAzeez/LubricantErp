package com.havos.lubricerp.feature_reports.presentation.settings

import com.havos.lubricerp.core.common.ThemeMode
import com.havos.lubricerp.core.common.UiIntent
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.havos.lubricerp.core.common.UiState

sealed interface SettingsIntent : UiIntent {
    data class ThemeChanged(val mode: ThemeMode) : SettingsIntent
    data class BiometricToggled(val enabled: Boolean) : SettingsIntent
}

@Stable
data class SettingsUiState(
    val selectedThemeMode: ThemeMode = ThemeMode.SYSTEM,
    val profile: SettingsProfileUi? = null,
    val biometricEnabled: Boolean = false
) : UiState

@Immutable
data class SettingsProfileUi(
    val fullName: String,
    val email: String,
    val branchId: Long,
    val rolesText: String
)

sealed interface SettingsAction {
    data class ThemeSelected(val mode: ThemeMode) : SettingsAction
    data class BiometricToggled(val enabled: Boolean) : SettingsAction
}
