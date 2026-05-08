package com.havos.lubricerp.feature_reports.presentation.reports

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun PurchaseSummaryScreen(
    state: ReportDetailUiState,
    onAction: (ReportDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    TwoMetricReportScreen(
        modifier = modifier,
        headline = "Purchase summary by supplier and period",
        state = state,
        onAction = onAction,
        primaryMetric = "Total Purchase",
        primaryValue = "0.00",
        secondaryMetric = "Suppliers",
        secondaryValue = "0",
        rows = listOf(
            listOf("Base Oils Corp", "0.00", "0"),
            listOf("Packchem Ltd", "0.00", "0")
        ),
        headers = listOf("Supplier", "Amount", "GRN")
    )
}

@Composable
internal fun GrnSummaryScreen(
    state: ReportDetailUiState,
    onAction: (ReportDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    TwoMetricReportScreen(
        modifier = modifier,
        headline = "Goods receipt note summary",
        state = state,
        onAction = onAction,
        primaryMetric = "Total GRN",
        primaryValue = "0",
        secondaryMetric = "Pending QC",
        secondaryValue = "0",
        rows = listOf(
            listOf("GRN-1001", "Base Oils Corp", "Closed"),
            listOf("GRN-1002", "Packchem Ltd", "Pending")
        ),
        headers = listOf("GRN No", "Supplier", "Status")
    )
}

@Composable
internal fun StateWisePurchaseScreen(
    state: ReportDetailUiState,
    onAction: (ReportDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    TwoMetricReportScreen(
        modifier = modifier,
        headline = "State-wise purchase distribution",
        state = state,
        onAction = onAction,
        primaryMetric = "States Covered",
        primaryValue = "0",
        secondaryMetric = "Total Value",
        secondaryValue = "0.00",
        rows = listOf(
            listOf("Tamil Nadu", "0.00", "0%"),
            listOf("Kerala", "0.00", "0%")
        ),
        headers = listOf("State", "Amount", "Share")
    )
}

@Composable
internal fun DistrictWisePurchaseScreen(
    state: ReportDetailUiState,
    onAction: (ReportDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    TwoMetricReportScreen(
        modifier = modifier,
        headline = "District-wise purchase distribution",
        state = state,
        onAction = onAction,
        primaryMetric = "Districts Covered",
        primaryValue = "0",
        secondaryMetric = "Total Value",
        secondaryValue = "0.00",
        rows = listOf(
            listOf("Chennai", "0.00", "0%"),
            listOf("Madurai", "0.00", "0%")
        ),
        headers = listOf("District", "Amount", "Share")
    )
}
