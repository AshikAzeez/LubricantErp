package com.havos.lubricerp.feature_reports.presentation.notification

import com.havos.lubricerp.core.common.UiIntent
import com.havos.lubricerp.core.common.UiState

val NotificationFilterTypes = listOf("Info", "Warning", "Approval", "Other")

data class NotificationUiState(
    val unreadCount: Int = 0,
    val isMarkingAllRead: Boolean = false,
    val errorMessage: String? = null,
    val selectedTypes: Set<String> = emptySet()
) : UiState

sealed interface NotificationIntent : UiIntent {
    data object LoadUnreadCount : NotificationIntent
    data class MarkAsRead(val id: Long) : NotificationIntent
    data object MarkAllAsRead : NotificationIntent
    data object Refresh : NotificationIntent
    data class FilterToggled(val type: String) : NotificationIntent
}

sealed interface NotificationEffect {
    data class ShowError(val message: String) : NotificationEffect
    data object MarkedAllRead : NotificationEffect
}
