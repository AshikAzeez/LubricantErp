package com.havos.lubricerp.feature_reports.presentation.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun SkuStockReportScreen(
    state: ReportDetailUiState,
    onAction: (ReportDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    TwoMetricReportScreen(
        modifier = modifier,
        headline = "SKU-wise stock availability in warehouse",
        state = state,
        onAction = onAction,
        primaryMetric = "Total SKU",
        primaryValue = "128",
        secondaryMetric = "Out of Stock",
        secondaryValue = "3",
        rows = listOf(
            listOf("SKU-1001", "Engine Oil 20W40", "1240"),
            listOf("SKU-1108", "Hydraulic Oil", "0"),
            listOf("SKU-2104", "Gear Oil", "455")
        ),
        headers = listOf("SKU", "Product", "Stock")
    )
}

@Composable
internal fun SlowMovingStockScreen(
    state: ReportDetailUiState,
    onAction: (ReportDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Items with no movement in the last 30 days") }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = state.daysThreshold,
                        onValueChange = { onAction(ReportDetailAction.DaysThresholdChanged(it)) },
                        label = { Text("Days Threshold") },
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = { onAction(ReportDetailAction.ApplyFilter) },
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) { Text("Filter") }
                }
            }
        }
    }
}
