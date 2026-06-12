package com.havos.lubricerp.feature_reports.presentation.home

import com.havos.lubricerp.core.common.UiIntent
import androidx.compose.runtime.Stable
import com.havos.lubricerp.core.common.UiState
import com.havos.lubricerp.core.ui.components.DashboardCardUi
import com.havos.lubricerp.feature_reports.presentation.reports.ReportItem
import com.havos.lubricerp.feature_reports.presentation.reports.ReportMenu

sealed interface HomeIntent : UiIntent {
    data class CardClicked(val menu: ReportMenu) : HomeIntent
    data object BottomSheetDismissed : HomeIntent
    data object LogoutClicked : HomeIntent
}

@Stable
data class HomeUiState(
    val greetingName: String = "",
    val isProfileLoading: Boolean = false,
    val cards: List<DashboardCardUi> = emptyList(),
    val selectedMenu: ReportMenu? = null
) : UiState

sealed interface HomeEffect {
    data class OpenReport(val reportItem: ReportItem) : HomeEffect
    data object NavigateToLogin : HomeEffect
}

sealed interface HomeNavigation {
    data class OpenReport(val reportKey: String) : HomeNavigation
    data object OpenCustomerData : HomeNavigation
    data class OpenReportModule(val reportItemKey: String) : HomeNavigation
    data object OpenSettings : HomeNavigation
    data object OpenNotifications : HomeNavigation
    data object NavigateLogin : HomeNavigation
}

sealed interface HomeAction {
    data class CardClicked(val menuKey: String) : HomeAction
    data class SubMenuClicked(val reportItem: ReportItem) : HomeAction
    data object DismissBottomSheet : HomeAction
    data object SettingsClicked : HomeAction
    data object LogoutClicked : HomeAction
}
