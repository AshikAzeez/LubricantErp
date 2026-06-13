package com.havos.lubricerp.feature_reports.presentation.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.OilBarrel
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.havos.lubricerp.feature_reports.domain.model.ConsolidatedStockItem
import com.havos.lubricerp.feature_reports.domain.model.FastMovingItem
import com.havos.lubricerp.feature_reports.domain.model.LowStockItem
import com.havos.lubricerp.feature_reports.domain.model.StockOverviewTankItem
import com.havos.lubricerp.feature_reports.domain.model.WarehouseStockItem

private val tabTitles = listOf("Bulk Stock", "Packaged", "Consolidated", "Low Stock", "Fast Moving")
private val tabIcons = listOf(Icons.Default.OilBarrel, Icons.Default.Inventory, Icons.Default.Inventory2, Icons.Default.Warning, Icons.Default.LocalFireDepartment)

@Composable
internal fun ConsolidatedStockScreen(
    state: ReportDetailUiState,
    onAction: (ReportDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val numFmt = rememberNumberFormat()
    val currencyFmt = rememberCurrencyFormat()
    val pageCount = 5
    val tabCounts = remember(state) {
        listOf(
            state.stockOverviewTankItems.size,
            state.warehouseStockItems.size,
            state.consolidatedStockItems.size,
            state.lowStockItems.size,
            state.fastMovingItems.size
        )
    }

    val pagerState = rememberPagerState(pageCount = { pageCount })

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != state.selectedConsolidatedTab) {
            onAction(ReportDetailAction.TabSelected(pagerState.currentPage))
        }
    }

    LaunchedEffect(state.selectedConsolidatedTab) {
        if (pagerState.currentPage != state.selectedConsolidatedTab) {
            pagerState.animateScrollToPage(state.selectedConsolidatedTab)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = state.selectedConsolidatedTab,
            edgePadding = 8.dp,
            divider = {}
        ) {
            tabTitles.forEachIndexed { index, title ->
                val count = tabCounts[index]
                val label = if (count > 0) "$title ($count)" else title
                Tab(
                    selected = state.selectedConsolidatedTab == index,
                    onClick = { onAction(ReportDetailAction.TabSelected(index)) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = tabIcons.getOrElse(index) { Icons.Default.Inventory },
                                contentDescription = "$title tab",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                )
            }
        }

        HorizontalDivider()

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) { page ->
            when (page) {
                0 -> BulkStockTab(tanks = state.stockOverviewTankItems, numFmt = numFmt)
                1 -> PackagedStockTab(items = state.warehouseStockItems, numFmt = numFmt)
                2 -> ConsolidatedViewTab(items = state.consolidatedStockItems, numFmt = numFmt)
                3 -> LowStockTab(
                    items = state.lowStockItems,
                    threshold = state.lowStockThreshold,
                    onAction = onAction
                )
                4 -> FastMovingTab(
                    items = state.fastMovingItems,
                    onAction = onAction,
                    days = state.fastMovingDays,
                    top = state.fastMovingTop,
                    currencyFmt = currencyFmt
                )
            }
        }
    }
}

@Composable
private fun rememberNumberFormat() = remember {
    @Suppress("DEPRECATION")
    val locale = java.util.Locale("en", "IN")
    java.text.NumberFormat.getNumberInstance(locale).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
}

@Composable
private fun rememberCurrencyFormat() = remember {
    @Suppress("DEPRECATION")
    val locale = java.util.Locale("en", "IN")
    java.text.NumberFormat.getCurrencyInstance(locale).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
}

@Composable
private fun BulkStockTab(
    tanks: List<StockOverviewTankItem>,
    numFmt: java.text.NumberFormat
) {
    val totalCapacity = remember(tanks) { tanks.sumOf { it.capacityInMT } }
    val totalStock = remember(tanks) { tanks.sumOf { it.stockInMT } }
    val totalAvailable = remember(tanks) { tanks.sumOf { it.availableCapacity } }

    if (tanks.isEmpty()) {
        EmptySection(message = "No bulk tank stock data available.", icon = Icons.Default.OilBarrel)
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StockSummaryCard(
                    label = "Capacity (MT)",
                    value = numFmt.format(totalCapacity),
                    modifier = Modifier.weight(1f)
                )
                StockSummaryCard(
                    label = "Stock (MT)",
                    value = numFmt.format(totalStock),
                    modifier = Modifier.weight(1f)
                )
                StockSummaryCard(
                    label = "Available (L)",
                    value = numFmt.format(totalAvailable),
                    modifier = Modifier.weight(1f),
                    valueColor = MaterialTheme.colorScheme.primary
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Tank", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("Stock (MT)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                Text("Fill %", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
            }
        }

        items(tanks, key = { it.tankId }) { tank ->
            TankStockRow(tank = tank, numFmt = numFmt)
        }
    }
}

@Composable
private fun PackagedStockTab(
    items: List<WarehouseStockItem>,
    numFmt: java.text.NumberFormat
) {
    if (items.isEmpty()) {
        EmptySection(message = "No packaged stock data available.", icon = Icons.Default.Inventory)
        return
    }

    val totalItems = items.sumOf { it.currentStock }
    val lowStockCount = items.count { it.currentStock <= it.reorderLevel }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StockSummaryCard(
                    label = "Total SKUs",
                    value = "${items.size}",
                    modifier = Modifier.weight(1f)
                )
                StockSummaryCard(
                    label = "Total Qty",
                    value = numFmt.format(totalItems),
                    modifier = Modifier.weight(1f)
                )
                StockSummaryCard(
                    label = "Low Stock",
                    value = "$lowStockCount",
                    modifier = Modifier.weight(1f),
                    valueColor = if (lowStockCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("SKU / Product", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
                Text("Qty", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                Text("Status", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
            }
        }

        items(items, key = { "${it.warehouseId}_${it.productSKUId}" }) { item ->
            WarehouseStockRow(item = item, numFmt = numFmt)
        }
    }
}

@Composable
private fun ConsolidatedViewTab(
    items: List<ConsolidatedStockItem>,
    numFmt: java.text.NumberFormat
) {
    if (items.isEmpty()) {
        EmptySection(message = "No consolidated stock data available.", icon = Icons.Default.Inventory2)
        return
    }

    val bulkItems = items.filter { it.itemType == "BulkOil" }
    val packagedItems = items.filter { it.itemType == "PackagedProduct" }
    val totalBulkLiters = bulkItems.sumOf { it.quantity }
    val totalPackagedQty = packagedItems.sumOf { it.quantity.toLong() }.toDouble()

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StockSummaryCard(
                    label = "Bulk Oil (L)",
                    value = numFmt.format(totalBulkLiters),
                    modifier = Modifier.weight(1f),
                    valueColor = MaterialTheme.colorScheme.primary
                )
                StockSummaryCard(
                    label = "Packaged (NOS)",
                    value = numFmt.format(totalPackagedQty),
                    modifier = Modifier.weight(1f),
                    valueColor = MaterialTheme.colorScheme.tertiary
                )
            }
        }

        if (bulkItems.isNotEmpty()) {
            item {
                SectionLabel(text = "Bulk Oil")
            }
            items(bulkItems, key = { it.itemCode }) { item ->
                ConsolidatedStockRow(item = item, numFmt = numFmt)
            }
        }

        if (packagedItems.isNotEmpty()) {
            item {
                SectionLabel(text = "Packaged Products")
            }
            items(packagedItems, key = { it.itemCode }) { item ->
                ConsolidatedStockRow(item = item, numFmt = numFmt)
            }
        }
    }
}

@Composable
private fun LowStockTab(
    items: List<LowStockItem>,
    threshold: Int,
    onAction: (ReportDetailAction) -> Unit
) {
    val isEmpty = items.isEmpty()

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StockSummaryCard(
                    label = "Items Below Threshold",
                    value = "${items.size}",
                    modifier = Modifier.weight(1f),
                    valueColor = if (items.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
                StockSummaryCard(
                    label = "Threshold",
                    value = "$threshold",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (isEmpty) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EmptySection(
                        message = "No low stock items found.",
                        subMessage = "All items are above the threshold.",
                        icon = Icons.Default.Warning
                    )
                }
            }
            return@LazyColumn
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Product", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
                Text("Stock", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                Text("Shortage", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
            }
        }

        items(items, key = { it.productSKUId }) { item ->
            LowStockRow(item = item)
        }
    }
}

@Composable
private fun FastMovingTab(
    items: List<FastMovingItem>,
    onAction: (ReportDetailAction) -> Unit,
    days: Int,
    top: Int,
    currencyFmt: java.text.NumberFormat
) {
    val numFmt = rememberNumberFormat()

    if (items.isEmpty()) {
        EmptySection(message = "No fast-moving product data available.", icon = Icons.Default.LocalFireDepartment)
        return
    }

    val totalQtySold = items.sumOf { it.quantitySold }
    val totalRevenue = items.sumOf { it.totalRevenue }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StockSummaryCard(
                    label = "Top Products",
                    value = "${items.size}",
                    modifier = Modifier.weight(1f),
                    valueColor = MaterialTheme.colorScheme.primary
                )
                StockSummaryCard(
                    label = "Total Qty Sold",
                    value = numFmt.format(totalQtySold),
                    modifier = Modifier.weight(1f)
                )
                StockSummaryCard(
                    label = "Total Revenue",
                    value = currencyFmt.format(totalRevenue),
                    modifier = Modifier.weight(1f),
                    valueColor = MaterialTheme.colorScheme.tertiary
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("#", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp))
                Text("Product", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
                Text("Sold", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                Text("Revenue", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
            }
        }

        items(items, key = { it.rank }) { item ->
            FastMovingRow(item = item, currencyFmt = currencyFmt)
        }
    }
}

@Composable
private fun StockSummaryCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp)
            .semantics { contentDescription = "Section: $text" }
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun TankStockRow(
    tank: StockOverviewTankItem,
    numFmt: java.text.NumberFormat
) {
    val fillColor = when {
        tank.fillPercentage > 70 -> MaterialTheme.colorScheme.primary
        tank.fillPercentage > 30 -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.error
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .semantics {
                contentDescription = "Tank ${tank.tankName}, stock ${numFmt.format(tank.stockInMT)} metric tons, fill ${tank.fillPercentage.toInt()} percent"
            }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${tank.tankName} (${tank.tankCode})",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!tank.productGrade.isNullOrBlank()) {
                    Text(
                        text = tank.productGrade.orEmpty(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = tank.locationName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                Text(
                    text = numFmt.format(tank.stockInMT),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                Text(
                    text = "${tank.fillPercentage.toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = fillColor
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (tank.fillPercentage / 100.0).toFloat().coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(4.dp),
            color = fillColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun WarehouseStockRow(
    item: WarehouseStockItem,
    numFmt: java.text.NumberFormat
) {
    val isLowStock = item.currentStock <= item.reorderLevel
    val reorderRatio = (item.currentStock.toFloat() / item.reorderLevel.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
    val barColor = if (isLowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .semantics {
                contentDescription = "SKU ${item.productSKUName}, quantity ${item.currentStock}, reorder level ${item.reorderLevel}${if (isLowStock) ", low stock" else ""}"
            }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(2f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isLowStock) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Low stock warning",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = item.productSKUName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "${item.productFamily} · ${item.warehouseName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                Text(
                    text = numFmt.format(item.currentStock),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "ROL: ${numFmt.format(item.reorderLevel)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                val statusColor = if (isLowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                val statusText = if (isLowStock) "Low" else "OK"
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { reorderRatio },
            modifier = Modifier.fillMaxWidth().height(3.dp),
            color = barColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun ConsolidatedStockRow(
    item: ConsolidatedStockItem,
    numFmt: java.text.NumberFormat
) {
    val isBulk = item.itemType == "BulkOil"
    val iconTint = if (isBulk) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
    val icon = if (isBulk) Icons.Default.OilBarrel else Icons.Default.Inventory
    val typeLabel = if (isBulk) "Bulk oil" else "Packaged product"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .semantics { contentDescription = "$typeLabel ${item.itemName}, quantity ${item.quantity} ${item.unit}, location ${item.location}" }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = typeLabel,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.itemName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row {
                        Text(
                            text = item.itemCode,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = item.location,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (isBulk) numFmt.format(item.quantity) else numFmt.format(item.quantity.toLong()).replace(".00", ""),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun LowStockRow(item: LowStockItem) {
    val isOutOfStock = item.currentStock == 0
    val reorderRatio = (item.currentStock.toFloat() / item.reorderLevel.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
    val shortagePercent = if (item.reorderLevel > 0) {
        (item.shortageQuantity.toFloat() / item.reorderLevel * 100).toInt()
    } else 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .semantics {
                contentDescription = "Low stock ${item.productSKUName}, current stock ${item.currentStock}, shortage ${item.shortageQuantity}, reorder level ${item.reorderLevel}${if (isOutOfStock) ", out of stock" else ""}"
            }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(2f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = if (isOutOfStock) "Out of stock" else "Low stock warning",
                        tint = if (isOutOfStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.productSKUName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "${item.productGrade} · ${item.warehouseName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                Text(
                    text = "${item.currentStock}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isOutOfStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
                if (isOutOfStock) {
                    Text(
                        text = "OUT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                Text(
                    text = "${item.shortageQuantity}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "${shortagePercent}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { reorderRatio },
            modifier = Modifier.fillMaxWidth().height(4.dp),
            color = if (isOutOfStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = "ROL: ${item.reorderLevel}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Stock: ${item.currentStock}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun FastMovingRow(
    item: FastMovingItem,
    currencyFmt: java.text.NumberFormat
) {
    val rankColors = listOf(
        Color(0xFFFFD700),
        Color(0xFFC0C0C0),
        Color(0xFFCD7F32)
    )
    val rankColor = rankColors.getOrElse(item.rank - 1) { MaterialTheme.colorScheme.onSurfaceVariant }
    val rankLabel = when (item.rank) { 1 -> "1st"; 2 -> "2nd"; 3 -> "3rd"; else -> "${item.rank}th" }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .semantics { contentDescription = "Rank $rankLabel ${item.productSKUName}, ${item.quantitySold} units sold, revenue ${currencyFmt.format(item.totalRevenue)}" }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier.width(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#${item.rank}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = rankColor
                )
            }
            Column(modifier = Modifier.weight(2f)) {
                Text(
                    text = item.productSKUName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.productGrade,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                Text(
                    text = "${item.quantitySold}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                Text(
                    text = currencyFmt.format(item.totalRevenue),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun EmptySection(
    message: String,
    subMessage: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (subMessage != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}
