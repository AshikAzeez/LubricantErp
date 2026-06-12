package com.havos.lubricerp.feature_reports.presentation.reports

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.havos.lubricerp.core.ui.components.ErrorPlaceholder
import com.havos.lubricerp.core.ui.components.OfflinePlaceholder
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailRoute(
    reportKey: String,
    onBackClick: () -> Unit,
    viewModel: ReportDetailViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(reportKey) {
        viewModel.onIntent(ReportDetailIntent.Load(reportKey))
    }

    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = { viewModel.onIntent(ReportDetailIntent.Refresh) },
        modifier = Modifier.fillMaxSize()
    ) {
        ReportDetailScreen(
            state = state,
            onBackClick = onBackClick,
            onAction = { action ->
                when (action) {
                    is ReportDetailAction.FromDateChanged -> viewModel.onIntent(ReportDetailIntent.FromDateChanged(action.value))
                    is ReportDetailAction.ToDateChanged -> viewModel.onIntent(ReportDetailIntent.ToDateChanged(action.value))
                    is ReportDetailAction.SearchChanged -> viewModel.onIntent(ReportDetailIntent.SearchChanged(action.value))
                    is ReportDetailAction.DaysThresholdChanged -> viewModel.onIntent(ReportDetailIntent.DaysThresholdChanged(action.value))
                    is ReportDetailAction.GroupByChanged -> viewModel.onIntent(ReportDetailIntent.GroupByChanged(action.value))
                    ReportDetailAction.ApplyFilter -> viewModel.onIntent(ReportDetailIntent.ApplyFilter)
                    ReportDetailAction.ResetFilter -> viewModel.onIntent(ReportDetailIntent.ResetFilter)
                    ReportDetailAction.ToggleTopCustomers -> viewModel.onIntent(ReportDetailIntent.ToggleTopCustomers)
                    ReportDetailAction.Retry -> viewModel.onIntent(ReportDetailIntent.Load(reportKey))
                    is ReportDetailAction.TabSelected -> viewModel.onIntent(ReportDetailIntent.TabSelected(action.index))
                    is ReportDetailAction.LowStockThresholdChanged -> viewModel.onIntent(ReportDetailIntent.LowStockThresholdChanged(action.value))
                    is ReportDetailAction.FastMovingDaysChanged -> viewModel.onIntent(ReportDetailIntent.FastMovingDaysChanged(action.value))
                    is ReportDetailAction.FastMovingTopChanged -> viewModel.onIntent(ReportDetailIntent.FastMovingTopChanged(action.value))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReportDetailScreen(
    state: ReportDetailUiState,
    onBackClick: () -> Unit,
    onAction: (ReportDetailAction) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = state.selectedReport.title) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)

        if (state.isLoading && state.selectedReport != ReportItem.SALES_SUMMARY) {
            if (state.selectedReport == ReportItem.TANK_STOCK_SUMMARY) {
                TankStockSummaryShimmerScreen(modifier = contentModifier)
            } else {
                ReportLoadingScreen(modifier = contentModifier)
            }
            return@Scaffold
        }

        if (state.isOffline) {
            OfflinePlaceholder(
                onRetry = { onAction(ReportDetailAction.Retry) },
                modifier = contentModifier
            )
            return@Scaffold
        }

        state.errorMessage?.let { message ->
            if (state.selectedReport == ReportItem.TANK_STOCK_SUMMARY) {
                TankStockSummaryScreen(
                    state = state.copy(tankStockSummary = state.tankStockSummary ?: mockTankStockSummary()),
                    modifier = contentModifier
                )
                return@Scaffold
            }
            ErrorPlaceholder(
                message = message,
                onRetry = { onAction(ReportDetailAction.Retry) },
                modifier = contentModifier
            )
            return@Scaffold
        }

        when (state.selectedReport) {
            ReportItem.TANK_STOCK_SUMMARY -> TankStockSummaryScreen(state, contentModifier)
            ReportItem.DIP_VARIANCE -> DipVarianceScreen(state, onAction, contentModifier)
            ReportItem.TANK_STOCK_LEDGER -> TankStockLedgerScreen(state, onAction, contentModifier)
            ReportItem.SKU_STOCK_REPORT -> SkuStockReportScreen(state, onAction, contentModifier)
            ReportItem.SLOW_MOVING_STOCK -> SlowMovingStockScreen(state, onAction, contentModifier)
            ReportItem.RAW_MATERIAL_STOCK -> RawMaterialStockScreen(state, onAction, contentModifier)
            ReportItem.PACKAGING_LOSS_GAIN -> PackagingLossGainScreen(state, onAction, contentModifier)
            ReportItem.PACKAGING_SUMMARY -> PackagingSummaryScreen(state, onAction, contentModifier)
            ReportItem.SALES_SUMMARY -> SalesSummaryScreen(state, onAction, contentModifier)
            ReportItem.PRODUCT_WISE_SALES -> ProductWiseSalesScreen(state, onAction, contentModifier)
            ReportItem.CUSTOMER_OUTSTANDING -> CustomerOutstandingScreen(state, onAction, contentModifier)
            ReportItem.SALES_RETURN_SUMMARY -> SalesReturnSummaryScreen(state, onAction, contentModifier)
            ReportItem.SALESMAN_PERFORMANCE -> SalesmanPerformanceScreen(state, onAction, contentModifier)
            ReportItem.STATE_WISE_SALES -> StateWiseSalesScreen(state, onAction, contentModifier)
            ReportItem.DISTRICT_WISE_SALES -> DistrictWiseSalesScreen(state, onAction, contentModifier)
            ReportItem.PURCHASE_SUMMARY -> PurchaseSummaryScreen(state, onAction, contentModifier)
            ReportItem.GRN_SUMMARY -> GrnSummaryScreen(state, onAction, contentModifier)
            ReportItem.STATE_WISE_PURCHASE -> StateWisePurchaseScreen(state, onAction, contentModifier)
            ReportItem.DISTRICT_WISE_PURCHASE -> DistrictWisePurchaseScreen(state, onAction, contentModifier)
            ReportItem.CONSOLIDATED_STOCK -> ConsolidatedStockScreen(state, onAction, contentModifier)
            ReportItem.CUSTOMER_DATA -> {}
            ReportItem.REPORT_SALES_SUMMARY -> {}
            ReportItem.REPORT_PRODUCT_SALES -> {}
            ReportItem.REPORT_NET_PROFIT -> {}
            ReportItem.REPORT_EXPENSE_SUMMARY -> {}
        }
    }
}
