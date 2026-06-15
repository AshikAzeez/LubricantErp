package com.havos.lubricerp.feature_reports.presentation.reports

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun DipVarianceScreen(
    state: ReportDetailUiState,
    onAction: (ReportDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    TwoMetricReportScreen(
        modifier = modifier,
        headline = "Daily dip variance by tank and grade",
        state = state,
        onAction = onAction,
        primaryMetric = "Total Variance",
        primaryValue = "0.00 L",
        secondaryMetric = "Max Variance",
        secondaryValue = "0.00 L",
        rows = emptyList(),
        headers = listOf("Tank", "Variance", "Status")
    )
}

@Composable
internal fun TankStockLedgerScreen(
    state: ReportDetailUiState,
    onAction: (ReportDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    TwoMetricReportScreen(
        modifier = modifier,
        headline = "Inflow and outflow ledger for each tank",
        state = state,
        onAction = onAction,
        primaryMetric = "Total Inflow",
        primaryValue = "0 L",
        secondaryMetric = "Total Outflow",
        secondaryValue = "0 L",
        rows = emptyList(),
        headers = listOf("Date", "Tank", "Type", "Qty")
    )
}
