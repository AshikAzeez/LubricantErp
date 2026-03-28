package com.havos.lubricerp.feature_reports.presentation.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.feature_reports.domain.model.DateRangeFilter
import com.havos.lubricerp.feature_reports.domain.usecase.GetPackagingLossGainUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.GetRawMaterialStockUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.GetTankStockSummaryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReportDetailViewModel(
    private val getTankStockSummaryUseCase: GetTankStockSummaryUseCase,
    private val getRawMaterialStockUseCase: GetRawMaterialStockUseCase,
    private val getPackagingLossGainUseCase: GetPackagingLossGainUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ReportDetailUiState())
    val state: StateFlow<ReportDetailUiState> = _state.asStateFlow()

    fun onIntent(intent: ReportDetailIntent) {
        when (intent) {
            is ReportDetailIntent.Load -> loadReport(intent.reportKey)
            is ReportDetailIntent.FromDateChanged -> _state.update { it.copy(fromDate = intent.date) }
            is ReportDetailIntent.ToDateChanged -> _state.update { it.copy(toDate = intent.date) }
            is ReportDetailIntent.SearchChanged -> _state.update { it.copy(searchQuery = intent.query) }
            is ReportDetailIntent.DaysThresholdChanged -> _state.update { it.copy(daysThreshold = intent.days) }
            is ReportDetailIntent.GroupByChanged -> _state.update { it.copy(groupBy = intent.group) }
            ReportDetailIntent.ApplyFilter -> applyFilter()
            ReportDetailIntent.ResetFilter -> resetFilters()
        }
    }

    private fun loadReport(reportKey: String) {
        val report = reportItemByKey(reportKey)
        _state.update { ReportDetailReducer.reduceForLoading(it, report) }
        when (report) {
            ReportItem.TANK_STOCK_SUMMARY -> fetchTankStockSummary()
            ReportItem.RAW_MATERIAL_STOCK -> fetchRawMaterialStock()
            ReportItem.PACKAGING_LOSS_GAIN -> fetchPackagingLossGain()
            else -> _state.update { it.copy(isLoading = false, errorMessage = null) }
        }
    }

    private fun applyFilter() {
        when (_state.value.selectedReport) {
            ReportItem.PACKAGING_LOSS_GAIN -> fetchPackagingLossGain()
            ReportItem.SLOW_MOVING_STOCK,
            ReportItem.PACKAGING_SUMMARY,
            ReportItem.RAW_MATERIAL_STOCK,
            ReportItem.TANK_STOCK_SUMMARY -> _state.update { it.copy(isLoading = false) }

            else -> Unit
        }
    }

    private fun resetFilters() {
        _state.update {
            it.copy(
                fromDate = "28/03/2026",
                toDate = "28/03/2026",
                daysThreshold = "30 days",
                groupBy = "Daily",
                searchQuery = ""
            )
        }
        applyFilter()
    }

    private fun fetchTankStockSummary() {
        viewModelScope.launch {
            when (val result = getTankStockSummaryUseCase()) {
                is ResultState.Success -> {
                    _state.update { ReportDetailReducer.reduceForTankSuccess(it, result.data) }
                }

                is ResultState.Error -> {
                    _state.update { ReportDetailReducer.reduceForError(it, result.message) }
                }

                ResultState.Loading -> _state.update { it.copy(isLoading = true) }
            }
        }
    }

    private fun fetchRawMaterialStock() {
        viewModelScope.launch {
            when (val result = getRawMaterialStockUseCase()) {
                is ResultState.Success -> {
                    _state.update { ReportDetailReducer.reduceForRawMaterialSuccess(it, result.data) }
                }

                is ResultState.Error -> {
                    _state.update { ReportDetailReducer.reduceForError(it, result.message) }
                }

                ResultState.Loading -> _state.update { it.copy(isLoading = true) }
            }
        }
    }

    private fun fetchPackagingLossGain() {
        viewModelScope.launch {
            val current = _state.value
            when (
                val result = getPackagingLossGainUseCase(
                    DateRangeFilter(
                        fromDate = current.fromDate,
                        toDate = current.toDate
                    )
                )
            ) {
                is ResultState.Success -> {
                    _state.update { ReportDetailReducer.reduceForPackagingSuccess(it, result.data) }
                }

                is ResultState.Error -> {
                    _state.update { ReportDetailReducer.reduceForError(it, result.message) }
                }

                ResultState.Loading -> _state.update { it.copy(isLoading = true) }
            }
        }
    }
}
