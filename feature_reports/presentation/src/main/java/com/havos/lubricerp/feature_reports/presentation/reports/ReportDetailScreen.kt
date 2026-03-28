package com.havos.lubricerp.feature_reports.presentation.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.havos.lubricerp.core.ui.components.StatCard
import org.koin.androidx.compose.koinViewModel

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
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReportDetailScreen(
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

        if (state.isLoading) {
            SkeletonReportLoading(modifier = contentModifier)
            return@Scaffold
        }

        state.errorMessage?.let { message ->
            Column(
                modifier = contentModifier.padding(16.dp)
            ) {
                Text(text = message, color = MaterialTheme.colorScheme.error)
            }
            return@Scaffold
        }

        when (state.selectedReport) {
            ReportItem.TANK_STOCK_SUMMARY -> TankStockSummaryContent(state, contentModifier)
            ReportItem.RAW_MATERIAL_STOCK -> RawMaterialStockContent(state, onAction, contentModifier)
            ReportItem.PACKAGING_LOSS_GAIN -> PackagingLossGainContent(state, onAction, contentModifier)
            ReportItem.SLOW_MOVING_STOCK -> SlowMovingContent(state, onAction, contentModifier)
            ReportItem.PACKAGING_SUMMARY -> PackagingSummaryContent(state, onAction, contentModifier)
            else -> PlaceholderContent(state.selectedReport.title, contentModifier)
        }
    }
}

@Composable
private fun TankStockSummaryContent(
    state: ReportDetailUiState,
    modifier: Modifier = Modifier
) {
    val summary = state.tankStockSummary
    LazyColumn(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text(text = "Current stock levels across all tanks") }
        item {
            AdaptiveStatRow {
                StatCard("Total Capacity", "${summary?.totalCapacityLiters ?: 0} L", Modifier.weight(1f))
                StatCard("Current Stock", "${summary?.currentStockLiters ?: 0} L", Modifier.weight(1f))
                StatCard("Available Capacity", "${summary?.availableCapacityLiters ?: 0} L", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun RawMaterialStockContent(
    state: ReportDetailUiState,
    onAction: (ReportDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val filtered = remember(state.rawMaterialItems, state.searchQuery) {
        state.rawMaterialItems.filter {
            state.searchQuery.isBlank() || it.name.contains(state.searchQuery, true) || it.code.contains(state.searchQuery, true)
        }
    }

    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text(text = "Current stock levels and cost of raw materials") }
        item {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { onAction(ReportDetailAction.SearchChanged(it)) },
                label = { Text("Search") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    filtered.forEach { item ->
                        Text("${item.code} - ${item.name} (${item.uom})")
                    }
                }
            }
        }
    }
}

@Composable
private fun PackagingLossGainContent(
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
                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdaptiveStatRow(content: @Composable RowScope.() -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            content = content
        )
    }
}

@Composable
private fun SlowMovingContent(
    state: ReportDetailUiState,
    onAction: (ReportDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Items with no movement in the last 30 days")
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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

@Composable
private fun PackagingSummaryContent(
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
                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
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

@Composable
private fun PlaceholderContent(
    reportTitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = reportTitle, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "This report is scaffolded and ready for API integration.")
    }
}

@Composable
private fun SkeletonReportLoading(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(5) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Loading report data...")
                }
            }
        }
    }
}
