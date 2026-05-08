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
        rows = listOf(
            listOf("TK-01", "0.00 L", "Within Limit"),
            listOf("TK-02", "0.00 L", "Within Limit"),
            listOf("TK-03", "0.00 L", "Within Limit")
        ),
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
        rows = listOf(
            listOf("28/03/2026", "TK-01", "Issue", "0 L"),
            listOf("28/03/2026", "TK-02", "Receipt", "0 L")
        ),
        headers = listOf("Date", "Tank", "Type", "Qty")
    )
}
