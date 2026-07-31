package com.havos.lubricerp.feature_reports.presentation.products

import androidx.compose.runtime.Stable
import com.havos.lubricerp.core.common.UiIntent
import com.havos.lubricerp.core.common.UiState
import com.havos.lubricerp.feature_reports.domain.model.CostBreakdownDetail
import com.havos.lubricerp.feature_reports.domain.model.CostBreakdownItem

sealed interface CostBreakdownDetailIntent : UiIntent {
    data class Load(val id: Long) : CostBreakdownDetailIntent
    data object Refresh : CostBreakdownDetailIntent
    data object EditClicked : CostBreakdownDetailIntent
    data object DeleteClicked : CostBreakdownDetailIntent
    data object ConvertToPiClicked : CostBreakdownDetailIntent
}

@Stable
data class CostBreakdownDetailUiState(
    val isLoading: Boolean = true,
    val detail: CostBreakdownDetail? = null,
    val errorMessage: String? = null,
    val showDeleteConfirmation: Boolean = false,
    val isDeleting: Boolean = false,
    val item: CostBreakdownItem? = null
) : UiState

sealed interface CostBreakdownDetailEffect {
    data class Toast(val message: String) : CostBreakdownDetailEffect
    data class NavigateToEdit(val id: Long) : CostBreakdownDetailEffect
    data class NavigateToConvertToPi(val id: Long) : CostBreakdownDetailEffect
    data object Deleted : CostBreakdownDetailEffect
    data object NavigateBack : CostBreakdownDetailEffect
}

sealed interface CostBreakdownDetailAction {
    data object EditClicked : CostBreakdownDetailAction
    data object DeleteClicked : CostBreakdownDetailAction
    data object ConfirmDelete : CostBreakdownDetailAction
    data object DismissDelete : CostBreakdownDetailAction
    data object ConvertToPiClicked : CostBreakdownDetailAction
    data object Refresh : CostBreakdownDetailAction
}
