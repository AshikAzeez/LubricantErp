package com.havos.lubricerp.feature_reports.presentation.home

import com.havos.lubricerp.core.common.UiIntent
import com.havos.lubricerp.core.common.UiState
import com.havos.lubricerp.core.ui.components.DashboardCardUi
import com.havos.lubricerp.feature_reports.presentation.reports.ReportItem
import com.havos.lubricerp.feature_reports.presentation.reports.ReportMenu

sealed interface ReportsTabIntent : UiIntent {
    data class CardClicked(val menu: ReportMenu) : ReportsTabIntent
    data object BottomSheetDismissed : ReportsTabIntent
}

data class ReportsTabUiState(
    val greetingName: String = "",
    val isProfileLoading: Boolean = false,
    val cards: List<DashboardCardUi> = emptyList(),
    val selectedMenu: ReportMenu? = null
) : UiState

sealed interface ReportsTabEffect {
    data class OpenReport(val reportItem: ReportItem) : ReportsTabEffect
}

sealed interface ReportsTabAction {
    data class CardClicked(val menuKey: String) : ReportsTabAction
    data class SubMenuClicked(val reportItem: ReportItem) : ReportsTabAction
    data object DismissBottomSheet : ReportsTabAction
}
