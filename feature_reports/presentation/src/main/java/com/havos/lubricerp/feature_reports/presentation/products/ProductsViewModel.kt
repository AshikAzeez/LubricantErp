package com.havos.lubricerp.feature_reports.presentation.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.feature_reports.domain.usecase.GetCostBreakdownSheetsUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.ObserveSessionUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProductsViewModel(
    private val observeSessionUseCase: ObserveSessionUseCase,
    private val getCostBreakdownSheetsUseCase: GetCostBreakdownSheetsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProductsUiState())
    val state: StateFlow<ProductsUiState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<ProductsEffect>()
    val effect: SharedFlow<ProductsEffect> = _effect.asSharedFlow()

    private var sortAscending = false

    init {
        loadData(isRefresh = false)
    }

    fun onIntent(intent: ProductsIntent) {
        when (intent) {
            is ProductsIntent.Load -> loadData(isRefresh = false)
            is ProductsIntent.Refresh -> loadData(isRefresh = true)
            is ProductsIntent.SortChanged -> applySort(intent.column)
            is ProductsIntent.MenuAction -> handleMenuAction(intent.item, intent.action)
            is ProductsIntent.CreateClicked -> viewModelScope.launch {
                _effect.emit(ProductsEffect.NavigateToCreate())
            }
        }
    }

    private fun loadData(isRefresh: Boolean) {
        viewModelScope.launch {
            if (!isRefresh) _state.update { it.copy(isLoading = true, errorMessage = null) }
            val token = runCatching { observeSessionUseCase().first()?.token.orEmpty() }.getOrElse { "" }
            when (val result = getCostBreakdownSheetsUseCase(token)) {
                is ResultState.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            items = sortItems(result.data, it.sortColumn, sortAscending),
                            errorMessage = null
                        )
                    }
                }
                is ResultState.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = result.message
                        )
                    }
                }
                ResultState.Loading -> {}
            }
        }
    }

    private fun applySort(column: CostBreakdownSortColumn) {
        val current = _state.value
        sortAscending = if (current.sortColumn == column) !sortAscending else false
        _state.update {
            it.copy(
                sortColumn = column,
                sortAscending = sortAscending,
                items = sortItems(it.items, column, sortAscending)
            )
        }
    }

    private fun sortItems(
        items: List<com.havos.lubricerp.feature_reports.domain.model.CostBreakdownItem>,
        column: CostBreakdownSortColumn,
        ascending: Boolean
    ): List<com.havos.lubricerp.feature_reports.domain.model.CostBreakdownItem> {
        val comparator: Comparator<com.havos.lubricerp.feature_reports.domain.model.CostBreakdownItem> = when (column) {
            CostBreakdownSortColumn.SKU -> compareBy { it.sku.lowercase() }
            CostBreakdownSortColumn.PRODUCT_GRADE -> compareBy { it.productGrade.lowercase() }
            CostBreakdownSortColumn.PRODUCT_FAMILY -> compareBy { it.productFamily.lowercase() }
            CostBreakdownSortColumn.EFFECTIVE_FROM -> compareBy { it.effectiveFrom }
            CostBreakdownSortColumn.EFFECTIVE_TO -> compareBy { it.effectiveTo ?: "" }
            CostBreakdownSortColumn.TOTAL_COST -> compareBy { it.totalCost }
        }
        return items.sortedWith(if (ascending) comparator else comparator.reversed())
    }

    private fun handleMenuAction(item: com.havos.lubricerp.feature_reports.domain.model.CostBreakdownItem, action: CostBreakdownMenuAction) {
        viewModelScope.launch {
            when (action) {
                CostBreakdownMenuAction.VIEW_DETAILS -> _effect.emit(ProductsEffect.NavigateToDetail(item.id))
                CostBreakdownMenuAction.EDIT -> _effect.emit(ProductsEffect.OpenEdit(item))
                CostBreakdownMenuAction.CONVERT_TO_PI -> _effect.emit(ProductsEffect.ConvertToPi(item))
                CostBreakdownMenuAction.DELETE -> _effect.emit(ProductsEffect.Toast("Delete functionality will be available soon"))
            }
        }
    }

    fun onAction(action: ProductsAction) {
        when (action) {
            is ProductsAction.SortChanged -> onIntent(ProductsIntent.SortChanged(action.column))
            is ProductsAction.MenuClicked -> onIntent(ProductsIntent.MenuAction(action.item, action.action))
            is ProductsAction.CreateClicked -> onIntent(ProductsIntent.CreateClicked)
            is ProductsAction.Refresh -> onIntent(ProductsIntent.Refresh)
        }
    }
}
