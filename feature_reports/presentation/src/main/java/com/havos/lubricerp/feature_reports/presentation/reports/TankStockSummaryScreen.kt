package com.havos.lubricerp.feature_reports.presentation.reports

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.havos.lubricerp.core.ui.components.StatCard
import com.havos.lubricerp.feature_reports.domain.model.TankInfo
import com.havos.lubricerp.feature_reports.domain.model.TankStockSummary
import java.text.NumberFormat
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

    ReportsModuleScreen(
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
private fun ReportsModuleScreen(
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
            if (state.selectedReport == ReportItem.TANK_STOCK_SUMMARY) {
                TankStockSummaryShimmerScreen(modifier = contentModifier)
            } else {
                SkeletonReportLoading(modifier = contentModifier)
            }
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
            Column(
                modifier = contentModifier.padding(16.dp)
            ) {
                Text(text = message, color = MaterialTheme.colorScheme.error)
            }
            return@Scaffold
        }

        when (state.selectedReport) {
            ReportItem.TANK_STOCK_SUMMARY -> TankStockSummaryScreen(state, contentModifier)
            ReportItem.DIP_VARIANCE -> DipVarianceContent(state, onAction, contentModifier)
            ReportItem.TANK_STOCK_LEDGER -> TankStockLedgerContent(state, onAction, contentModifier)
            ReportItem.SKU_STOCK_REPORT -> SkuStockReportContent(state, onAction, contentModifier)
            ReportItem.SLOW_MOVING_STOCK -> SlowMovingContent(state, onAction, contentModifier)
            ReportItem.RAW_MATERIAL_STOCK -> RawMaterialStockContent(state, onAction, contentModifier)
            ReportItem.PACKAGING_LOSS_GAIN -> PackagingLossGainContent(state, onAction, contentModifier)
            ReportItem.PACKAGING_SUMMARY -> PackagingSummaryContent(state, onAction, contentModifier)
            ReportItem.SALES_SUMMARY -> SalesSummaryContent(state, onAction, contentModifier)
            ReportItem.PRODUCT_WISE_SALES -> ProductWiseSalesContent(state, onAction, contentModifier)
            ReportItem.CUSTOMER_OUTSTANDING -> CustomerOutstandingContent(state, onAction, contentModifier)
            ReportItem.SALES_RETURN_SUMMARY -> SalesReturnSummaryContent(state, onAction, contentModifier)
            ReportItem.SALESMAN_PERFORMANCE -> SalesmanPerformanceContent(state, onAction, contentModifier)
            ReportItem.STATE_WISE_SALES -> StateWiseSalesContent(state, onAction, contentModifier)
            ReportItem.DISTRICT_WISE_SALES -> DistrictWiseSalesContent(state, onAction, contentModifier)
            ReportItem.PURCHASE_SUMMARY -> PurchaseSummaryContent(state, onAction, contentModifier)
            ReportItem.GRN_SUMMARY -> GrnSummaryContent(state, onAction, contentModifier)
            ReportItem.STATE_WISE_PURCHASE -> StateWisePurchaseContent(state, onAction, contentModifier)
            ReportItem.DISTRICT_WISE_PURCHASE -> DistrictWisePurchaseContent(state, onAction, contentModifier)
            ReportItem.CONSOLIDATED_STOCK -> ConsolidatedStockContent(state, onAction, contentModifier)
        }
    }
}

@Composable
private fun TankStockSummaryScreen(
    state: ReportDetailUiState,
    modifier: Modifier = Modifier
) {
    val summary = state.tankStockSummary ?: mockTankStockSummary()
    val formatter = remember { NumberFormat.getIntegerInstance() }
    val cardShape = MaterialTheme.shapes.large
    val dottedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)

    LazyColumn(
        modifier = modifier
            .drawBehind {
                val stepX = 26.dp.toPx()
                val stepY = 26.dp.toPx()
                var y = 0f
                while (y < size.height) {
                    var x = 0f
                    while (x < size.width) {
                        drawCircle(dottedColor, radius = 1.3.dp.toPx(), center = Offset(x, y))
                        x += stepX
                    }
                    y += stepY
                }
            }
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = cardShape,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "TOTAL CAPACITY",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = formatter.format(summary.totalCapacityLiters),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = " L",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TankMetricBlock(
                            label = "CURRENT STOCK",
                            value = "${formatter.format(summary.currentStockLiters)} L"
                        )
                        TankMetricBlock(
                            label = "AVAILABLE",
                            value = "${formatter.format(summary.availableCapacityLiters)} L"
                        )
                    }
                }
            }
        }

        item {
            SectionTitleWithBadge(
                title = "TANK FILL LEVELS",
                badge = "LIVE FEED"
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = cardShape,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    summary.tanks.forEach { tank ->
                        TankLevelCapsule(tank = tank)
                    }
                }
            }
        }

        item {
            SectionTitleWithBadge(
                title = "DETAILED INVENTORY",
                badge = null
            )
        }

        items(summary.tanks, key = { it.code }) { tank ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surfaceContainerHighest
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = tank.code,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.Default.Storage,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 10.dp)
                        ) {
                            Text(
                                text = "${tank.location} / Zone A",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = tank.productGrade.ifBlank { "UNASSIGNED GRADE" }.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${tank.fillPercent}%",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (tank.fillPercent == 0) "EMPTY" else "ACTIVE",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    LinearProgressIndicator(
                        progress = { (tank.fillPercent.coerceIn(0, 100)) / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

private fun mockTankStockSummary(): TankStockSummary {
    return TankStockSummary(
        totalCapacityLiters = 126000,
        currentStockLiters = 0,
        availableCapacityLiters = 126000,
        tanks = listOf(
            TankInfo(
                name = "Tank1",
                code = "TK-01",
                location = "Main Plant",
                productGrade = "Unassigned Grade",
                capacityLiters = 20000,
                currentStockLiters = 0,
                availableLiters = 20000,
                fillPercent = 0
            ),
            TankInfo(
                name = "Tank2",
                code = "TK-02",
                location = "Main Plant",
                productGrade = "Unassigned Grade",
                capacityLiters = 20000,
                currentStockLiters = 0,
                availableLiters = 20000,
                fillPercent = 0
            ),
            TankInfo(
                name = "Tank3",
                code = "TK-03",
                location = "Main Plant",
                productGrade = "Unassigned Grade",
                capacityLiters = 68000,
                currentStockLiters = 0,
                availableLiters = 68000,
                fillPercent = 0
            ),
            TankInfo(
                name = "Tank4",
                code = "TK-04",
                location = "Main Plant",
                productGrade = "Unassigned Grade",
                capacityLiters = 18000,
                currentStockLiters = 0,
                availableLiters = 18000,
                fillPercent = 0
            )
        )
    )
}

@Composable
private fun TankMetricBlock(
    label: String,
    value: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SectionTitleWithBadge(
    title: String,
    badge: String?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        if (!badge.isNullOrBlank()) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceContainerHighest
            ) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun TankLevelCapsule(tank: TankInfo) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .width(44.dp)
                .height(142.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = MaterialTheme.shapes.medium
                )
                .padding(horizontal = 5.dp, vertical = 6.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight((tank.fillPercent.coerceIn(0, 100)) / 100f)
                    .clip(MaterialTheme.shapes.small)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.95f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                            )
                        )
                    )
            )
            repeat(3) { marker ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .align(
                            when (marker) {
                                0 -> Alignment.Center
                                1 -> Alignment.TopCenter
                                else -> Alignment.BottomCenter
                            }
                        )
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = tank.code,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center
        )
        Text(
            text = "${tank.fillPercent}%",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
private fun DipVarianceContent(
    state: ReportDetailUiState,
    onAction: (ReportDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    GenericReportDetailContent(
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
private fun TankStockLedgerContent(
    state: ReportDetailUiState,
    onAction: (ReportDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    GenericReportDetailContent(
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

@Composable
private fun SkuStockReportContent(
    state: ReportDetailUiState,
    onAction: (ReportDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    GenericReportDetailContent(
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
private fun SalesSummaryContent(
    state: ReportDetailUiState,
    onAction: (ReportDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    GenericReportDetailContent(
        modifier = modifier,
        headline = "Period-based sales summary",
        state = state,
        onAction = onAction,
        primaryMetric = "Gross Sales",
        primaryValue = "0.00",
        secondaryMetric = "Invoices",
        secondaryValue = "0",
        rows = listOf(
            listOf("North Zone", "0.00", "0"),
            listOf("South Zone", "0.00", "0")
        ),
        headers = listOf("Zone", "Amount", "Invoices")
    )
}

@Composable
private fun ProductWiseSalesContent(
    state: ReportDetailUiState,
    onAction: (ReportDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    GenericReportDetailContent(
        modifier = modifier,
        headline = "Product category contribution in sales",
        state = state,
        onAction = onAction,
        primaryMetric = "Top Product",
        primaryValue = "N/A",
        secondaryMetric = "Total Qty",
        secondaryValue = "0",
        rows = listOf(
            listOf("Engine Oil", "0", "0.00"),
            listOf("Gear Oil", "0", "0.00")
        ),
        headers = listOf("Product", "Qty", "Amount")
    )
}

@Composable
private fun CustomerOutstandingContent(
    state: ReportDetailUiState,
    onAction: (ReportDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    GenericReportDetailContent(
        modifier = modifier,
        headline = "Customer receivables and aging",
        state = state,
        onAction = onAction,
        primaryMetric = "Outstanding",
        primaryValue = "0.00",
        secondaryMetric = "Customers",
        secondaryValue = "0",
        rows = listOf(
            listOf("Apex Lubes", "0.00", "0 Days"),
            listOf("Metro Traders", "0.00", "0 Days")
        ),
        headers = listOf("Customer", "Amount", "Age")
    )
}

@Composable
private fun SalesReturnSummaryContent(
    state: ReportDetailUiState,
    onAction: (ReportDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    GenericReportDetailContent(
        modifier = modifier,
        headline = "Returns by invoice and product",
        state = state,
        onAction = onAction,
        primaryMetric = "Total Return",
        primaryValue = "0.00",
        secondaryMetric = "Return Cases",
        secondaryValue = "0",
        rows = listOf(
            listOf("INV-1001", "Damaged", "0.00"),
            listOf("INV-1012", "Expiry", "0.00")
        ),
        headers = listOf("Invoice", "Reason", "Amount")
    )
}

@Composable
private fun SalesmanPerformanceContent(
    state: ReportDetailUiState,
    onAction: (ReportDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    GenericReportDetailContent(
        modifier = modifier,
        headline = "Sales target vs achievement by salesman",
        state = state,
        onAction = onAction,
        primaryMetric = "Avg Achievement",
        primaryValue = "0%",
        secondaryMetric = "Active Team",
        secondaryValue = "0",
        rows = listOf(
            listOf("Anil", "0%", "0.00"),
            listOf("Rahul", "0%", "0.00")
        ),
        headers = listOf("Salesman", "Achv%", "Sales")
    )
}

@Composable
private fun StateWiseSalesContent(
    state: ReportDetailUiState,
    onAction: (ReportDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    GenericReportDetailContent(
        modifier = modifier,
        headline = "State-wise sales split",
        state = state,
        onAction = onAction,
        primaryMetric = "States Covered",
        primaryValue = "0",
        secondaryMetric = "Total Sales",
        secondaryValue = "0.00",
        rows = listOf(
            listOf("Tamil Nadu", "0.00", "0%"),
            listOf("Karnataka", "0.00", "0%")
        ),
        headers = listOf("State", "Amount", "Share")
    )
}

@Composable
private fun DistrictWiseSalesContent(
    state: ReportDetailUiState,
    onAction: (ReportDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    GenericReportDetailContent(
        modifier = modifier,
        headline = "District-wise sales split",
        state = state,
        onAction = onAction,
        primaryMetric = "Districts Covered",
        primaryValue = "0",
        secondaryMetric = "Total Sales",
        secondaryValue = "0.00",
        rows = listOf(
            listOf("Chennai", "0.00", "0%"),
            listOf("Coimbatore", "0.00", "0%")
        ),
        headers = listOf("District", "Amount", "Share")
    )
}

@Composable
private fun PurchaseSummaryContent(
    state: ReportDetailUiState,
    onAction: (ReportDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    GenericReportDetailContent(
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
private fun GrnSummaryContent(
    state: ReportDetailUiState,
    onAction: (ReportDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    GenericReportDetailContent(
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
private fun StateWisePurchaseContent(
    state: ReportDetailUiState,
    onAction: (ReportDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    GenericReportDetailContent(
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
private fun DistrictWisePurchaseContent(
    state: ReportDetailUiState,
    onAction: (ReportDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    GenericReportDetailContent(
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

@Composable
private fun ConsolidatedStockContent(
    state: ReportDetailUiState,
    onAction: (ReportDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    GenericReportDetailContent(
        modifier = modifier,
        headline = "Combined stock snapshot across modules",
        state = state,
        onAction = onAction,
        primaryMetric = "Total Stock",
        primaryValue = "0",
        secondaryMetric = "Locations",
        secondaryValue = "0",
        rows = listOf(
            listOf("Tank", "0", "0%"),
            listOf("Warehouse", "0", "0%"),
            listOf("Raw Material", "0", "0%")
        ),
        headers = listOf("Category", "Qty", "Share")
    )
}

@Composable
private fun GenericReportDetailContent(
    modifier: Modifier,
    headline: String,
    state: ReportDetailUiState,
    onAction: (ReportDetailAction) -> Unit,
    primaryMetric: String,
    primaryValue: String,
    secondaryMetric: String,
    secondaryValue: String,
    headers: List<String>,
    rows: List<List<String>>
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text(headline) }
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
                    ) { Text("Apply") }
                }
            }
        }
        item {
            AdaptiveStatRow {
                StatCard(primaryMetric, primaryValue, Modifier.weight(1f))
                StatCard(secondaryMetric, secondaryValue, Modifier.weight(1f))
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = headers.joinToString("  |  "),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    rows.forEach { row ->
                        Text(
                            text = row.joinToString("  |  "),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
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

@Composable
private fun TankStockSummaryShimmerScreen(modifier: Modifier = Modifier) {
    val shimmerBrush = standardShimmerBrush()

    LazyColumn(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StandardShimmerBlock(
                        brush = shimmerBrush,
                        modifier = Modifier
                            .fillMaxWidth(0.35f)
                            .height(12.dp)
                    )
                    StandardShimmerBlock(
                        brush = shimmerBrush,
                        modifier = Modifier
                            .fillMaxWidth(0.62f)
                            .height(44.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StandardShimmerBlock(
                            brush = shimmerBrush,
                            modifier = Modifier
                                .weight(1f)
                                .height(30.dp)
                        )
                        StandardShimmerBlock(
                            brush = shimmerBrush,
                            modifier = Modifier
                                .weight(1f)
                                .height(30.dp)
                        )
                    }
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    repeat(4) {
                        StandardShimmerBlock(
                            brush = shimmerBrush,
                            modifier = Modifier
                                .width(38.dp)
                                .height(120.dp)
                                .clip(MaterialTheme.shapes.medium)
                        )
                    }
                }
            }
        }
        items(4) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StandardShimmerBlock(
                            brush = shimmerBrush,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(MaterialTheme.shapes.medium)
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StandardShimmerBlock(
                                brush = shimmerBrush,
                                modifier = Modifier
                                    .fillMaxWidth(0.82f)
                                    .height(16.dp)
                            )
                            StandardShimmerBlock(
                                brush = shimmerBrush,
                                modifier = Modifier
                                    .fillMaxWidth(0.56f)
                                    .height(12.dp)
                            )
                        }
                        StandardShimmerBlock(
                            brush = shimmerBrush,
                            modifier = Modifier
                                .width(44.dp)
                                .height(30.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun standardShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "tank_standard_shimmer")
    val translate by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "tank_standard_shimmer_anim"
    )
    val base = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    val highlight = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    return Brush.linearGradient(
        colors = listOf(base, highlight, base),
        start = Offset(translate * 600f, 0f),
        end = Offset((translate + 1f) * 600f, 320f)
    )
}

@Composable
private fun StandardShimmerBlock(
    brush: Brush,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(brush)
    )
}
