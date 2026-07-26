package com.havos.lubricerp.feature_reports.presentation.products

import androidx.compose.runtime.Stable
import com.havos.lubricerp.core.common.UiIntent
import com.havos.lubricerp.core.common.UiState
import com.havos.lubricerp.feature_reports.domain.model.CostBreakdownItem

sealed interface ProductsIntent : UiIntent {
    data object Load : ProductsIntent
    data object Refresh : ProductsIntent
    data class SortChanged(val column: CostBreakdownSortColumn) : ProductsIntent
    data class MenuAction(val item: CostBreakdownItem, val action: CostBreakdownMenuAction) : ProductsIntent
    data object CreateClicked : ProductsIntent
}

enum class CostBreakdownSortColumn(val label: String) {
    SKU("Product SKU"),
    PRODUCT_GRADE("Product Grade"),
    PRODUCT_FAMILY("Product Family"),
    EFFECTIVE_FROM("Effective From"),
    EFFECTIVE_TO("Effective To"),
    TOTAL_COST("Total Cost")
}

enum class CostBreakdownMenuAction {
    VIEW_DETAILS,
    EDIT,
    CONVERT_TO_PI,
    DELETE
}

@Stable
data class ProductsUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val items: List<CostBreakdownItem> = emptyList(),
    val sortColumn: CostBreakdownSortColumn = CostBreakdownSortColumn.EFFECTIVE_FROM,
    val sortAscending: Boolean = false
) : UiState

sealed interface ProductsEffect {
    data class Toast(val message: String) : ProductsEffect
    data class NavigateToCreate(val item: CostBreakdownItem? = null) : ProductsEffect
    data class OpenDetails(val item: CostBreakdownItem) : ProductsEffect
    data class OpenEdit(val item: CostBreakdownItem) : ProductsEffect
    data class ConvertToPi(val item: CostBreakdownItem) : ProductsEffect
    data class NavigateToDetail(val id: Long) : ProductsEffect
}

sealed interface ProductsAction {
    data class SortChanged(val column: CostBreakdownSortColumn) : ProductsAction
    data class MenuClicked(val item: CostBreakdownItem, val action: CostBreakdownMenuAction) : ProductsAction
    data object CreateClicked : ProductsAction
    data object Refresh : ProductsAction
}
