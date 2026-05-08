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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.havos.lubricerp.core.ui.components.StatCard

@Composable
internal fun PackagingLossGainScreen(
    state: ReportDetailUiState,
    onAction: (ReportDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val report = state.packagingLossGainReport
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text(text = "Order-wise breakdown of packaging variance") }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = state.fromDate,
                        onValueChange = { onAction(ReportDetailAction.FromDateChanged(it)) },
                        label = { Text("From Date") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = state.toDate,
                        onValueChange = { onAction(ReportDetailAction.ToDateChanged(it)) },
                        label = { Text("To Date") },
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = { onAction(ReportDetailAction.ApplyFilter) },
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) { Text("Filter") }
                    TextButton(
                        onClick = { onAction(ReportDetailAction.ResetFilter) },
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) { Text("Reset") }
                }
            }
        }
        item {
            AdaptiveStatRow {
                StatCard("Total Planned", "${report?.totalPlannedLiters ?: 0.0} L", Modifier.weight(1f))
                StatCard("Total Actual", "${report?.totalActualLiters ?: 0.0} L", Modifier.weight(1f))
                StatCard("Total Variance", "${report?.totalVarianceLiters ?: 0.0} L", Modifier.weight(1f))
            }
        }
    }
}

@Composable
internal fun PackagingSummaryScreen(
    state: ReportDetailUiState,
    onAction: (ReportDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Packaging activity summary grouped by period") }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = state.groupBy,
                        onValueChange = { onAction(ReportDetailAction.GroupByChanged(it)) },
                        label = { Text("Group By") },
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
