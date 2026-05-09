package com.havos.lubricerp.feature_reports.presentation.reportmodule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.havos.lubricerp.core.ui.components.ErrorPlaceholder
import com.havos.lubricerp.core.ui.components.OfflinePlaceholder
import com.havos.lubricerp.feature_reports.domain.model.ExpenseSummaryItem
import com.havos.lubricerp.feature_reports.domain.model.ProductSalesItem
import com.havos.lubricerp.feature_reports.domain.model.ReportSalesSummaryItem
import com.havos.lubricerp.feature_reports.presentation.reports.DatePickerButton
import com.havos.lubricerp.feature_reports.presentation.reports.ReportItem
import kotlinx.coroutines.launch

private enum class SalesSortField { NONE, CUSTOMER, TOTAL, BALANCE }

private val tabItems = listOf(
    ReportItem.REPORT_SALES_SUMMARY,
    ReportItem.REPORT_PRODUCT_SALES,
    ReportItem.REPORT_NET_PROFIT,
    ReportItem.REPORT_EXPENSE_SUMMARY
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReportModuleScreen(
    reportItemKey: String,
    state: ReportModuleUiState,
    onAction: (ReportModuleAction) -> Unit,
    onBackClick: () -> Unit
) {
    val initialTab = tabItems.indexOfFirst { it.key == reportItemKey }.coerceAtLeast(0)
    var selectedTab by remember { mutableIntStateOf(initialTab) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = tabItems.getOrNull(selectedTab)?.title ?: "Report Module") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 0.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                tabItems.forEachIndexed { index, item ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }

            if (state.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            when {
                state.isOffline -> OfflinePlaceholder(
                    onRetry = { onAction(ReportModuleAction.ApplyFilter) }
                )
                state.error != null -> ErrorPlaceholder(
                    message = state.error,
                    onRetry = { onAction(ReportModuleAction.ApplyFilter) }
                )
                else -> when (selectedTab) {
                    0 -> SalesSummaryTab(state = state, onAction = onAction)
                    1 -> ProductSalesTab(state = state, onAction = onAction)
                    2 -> NetProfitTab(state = state, onAction = onAction)
                    3 -> ExpenseSummaryTab(state = state, onAction = onAction)
                }
            }
        }
    }
}

@Composable
private fun DateFilterBar(
    fromDate: String,
    toDate: String,
    onAction: (ReportModuleAction) -> Unit
) {
    val utcFmt = remember {
        java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
    }
    val fromMillis = remember(fromDate) { runCatching { utcFmt.parse(fromDate)?.time }.getOrNull() }
    val toMillis   = remember(toDate)   { runCatching { utcFmt.parse(toDate)?.time }.getOrNull() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DatePickerButton(
            label = "From",
            value = fromDate,
            onDateSelected = { onAction(ReportModuleAction.FromDateChanged(it)) },
            modifier = Modifier.weight(1f),
            maxDateMillis = toMillis
        )
        DatePickerButton(
            label = "To",
            value = toDate,
            onDateSelected = { onAction(ReportModuleAction.ToDateChanged(it)) },
            modifier = Modifier.weight(1f),
            minDateMillis = fromMillis
        )
    }
}


@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SalesSummaryTab(
    state: ReportModuleUiState,
    onAction: (ReportModuleAction) -> Unit
) {
    val fmt = remember {
        java.text.NumberFormat.getNumberInstance(java.util.Locale("en", "IN")).apply {
            minimumFractionDigits = 2; maximumFractionDigits = 2
        }
    }
    var sortField by remember { mutableStateOf(SalesSortField.NONE) }
    var sortAsc by remember { mutableStateOf(true) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val showFab by remember { derivedStateOf { listState.firstVisibleItemIndex > 2 } }

    val sorted = remember(state.salesSummaryItems, sortField, sortAsc) {
        when (sortField) {
            SalesSortField.NONE -> state.salesSummaryItems
            SalesSortField.CUSTOMER -> if (sortAsc) state.salesSummaryItems.sortedBy { it.customerName.lowercase() }
                                       else state.salesSummaryItems.sortedByDescending { it.customerName.lowercase() }
            SalesSortField.TOTAL -> if (sortAsc) state.salesSummaryItems.sortedBy { it.totalAmount }
                                    else state.salesSummaryItems.sortedByDescending { it.totalAmount }
            SalesSortField.BALANCE -> if (sortAsc) state.salesSummaryItems.sortedBy { it.balanceAmount }
                                      else state.salesSummaryItems.sortedByDescending { it.balanceAmount }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DateFilterBar(fromDate = state.fromDate, toDate = state.toDate, onAction = onAction)
                    if (state.dateError != null) {
                        Text(
                            text = state.dateError,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.errorContainer)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            if (state.salesSummaryItems.isEmpty()) {
                item { EmptyState("No sales data for selected period.") }
            } else {
                item {
                    val total    = state.salesSummaryItems.sumOf { it.totalAmount }
                    val balance  = state.salesSummaryItems.sumOf { it.balanceAmount }
                    val invoices = state.salesSummaryItems.sumOf { it.invoiceCount }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        RmStatCard(label = "Invoices", value = invoices.toString(), modifier = Modifier.weight(1f))
                        RmStatCard(label = "Total Sales", value = "₹${fmt.format(total)}", modifier = Modifier.weight(1f))
                        RmStatCard(label = "Outstanding", value = "₹${fmt.format(balance)}", valueColor = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
                    }
                }
                item { Spacer(Modifier.height(8.dp)) }
                item {
                    SalesSortableHeader(
                        sortField = sortField,
                        sortAsc = sortAsc,
                        onSort = { field ->
                            if (sortField == field) sortAsc = !sortAsc
                            else { sortField = field; sortAsc = true }
                        }
                    )
                }
                items(sorted, key = { it.customerId }) { item ->
                    SalesSummaryRow(item = item, fmt = fmt)
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }

        if (showFab) {
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        if (listState.firstVisibleItemIndex > 5) listState.scrollToItem(3)
                        listState.animateScrollToItem(0)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Go to top")
            }
        }
    }
}

@Composable
private fun SalesSortableHeader(
    sortField: SalesSortField,
    sortAsc: Boolean,
    onSort: (SalesSortField) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RmSortableColumnLabel(
            text = "Customer",
            field = SalesSortField.CUSTOMER,
            active = sortField == SalesSortField.CUSTOMER,
            asc = sortAsc,
            onClick = { onSort(SalesSortField.CUSTOMER) },
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Start
        )
        RmSortableColumnLabel(
            text = "Total",
            field = SalesSortField.TOTAL,
            active = sortField == SalesSortField.TOTAL,
            asc = sortAsc,
            onClick = { onSort(SalesSortField.TOTAL) },
            modifier = Modifier.width(76.dp),
            textAlign = TextAlign.End
        )
        RmSortableColumnLabel(
            text = "Balance",
            field = SalesSortField.BALANCE,
            active = sortField == SalesSortField.BALANCE,
            asc = sortAsc,
            onClick = { onSort(SalesSortField.BALANCE) },
            modifier = Modifier.width(76.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun RmSortableColumnLabel(
    text: String,
    field: SalesSortField,
    active: Boolean,
    asc: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start
) {
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = modifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (textAlign == TextAlign.End) Arrangement.End else Arrangement.Start
    ) {
        if (textAlign == TextAlign.End) {
            Icon(
                imageVector = when {
                    !active -> Icons.Filled.SwapVert
                    asc -> Icons.Filled.ArrowUpward
                    else -> Icons.Filled.ArrowDownward
                },
                contentDescription = null,
                tint = if (active) activeColor else inactiveColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold,
            color = if (active) activeColor else MaterialTheme.colorScheme.onSurface,
            textAlign = textAlign
        )
        if (textAlign == TextAlign.Start) {
            Spacer(modifier = Modifier.width(2.dp))
            Icon(
                imageVector = when {
                    !active -> Icons.Filled.SwapVert
                    asc -> Icons.Filled.ArrowUpward
                    else -> Icons.Filled.ArrowDownward
                },
                contentDescription = null,
                tint = if (active) activeColor else inactiveColor,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun SalesSummaryRow(item: ReportSalesSummaryItem, fmt: java.text.NumberFormat) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.customerName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${item.customerCode} · ${item.invoiceCount} inv · Paid ₹${fmt.format(item.paidAmount)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "₹${fmt.format(item.totalAmount)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.End,
                modifier = Modifier.width(76.dp)
            )
            Text(
                text = "₹${fmt.format(item.balanceAmount)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (item.balanceAmount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.End,
                modifier = Modifier.width(76.dp)
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun ProductSalesTab(
    state: ReportModuleUiState,
    onAction: (ReportModuleAction) -> Unit
) {
    val fmt = remember {
        java.text.NumberFormat.getNumberInstance(java.util.Locale("en", "IN")).apply {
            minimumFractionDigits = 2; maximumFractionDigits = 2
        }
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item { DateFilterBar(fromDate = state.fromDate, toDate = state.toDate, onAction = onAction) }
        if (state.dateError != null) {
            item {
                Text(
                    text = state.dateError,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
        item { Spacer(Modifier.height(8.dp)) }

        if (state.productSalesItems.isEmpty()) {
            item { EmptyState("No product sales data for selected period.") }
        } else {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val totalQty = state.productSalesItems.sumOf { it.totalQuantity }
                    val totalAmt = state.productSalesItems.sumOf { it.totalAmount }
                    RmStatCard(label = "Products", value = state.productSalesItems.size.toString(), modifier = Modifier.weight(1f))
                    RmStatCard(label = "Total Qty", value = fmt.format(totalQty), modifier = Modifier.weight(1f))
                    RmStatCard(label = "Total Amount", value = "₹${fmt.format(totalAmt)}", modifier = Modifier.weight(1f))
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("Product / Type", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("Qty", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.width(70.dp))
                    Text("Amount", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.width(80.dp))
                }
            }
            items(state.productSalesItems, key = { "${it.productGrade}_${it.deliveryType}" }) { item ->
                ProductSalesRow(item = item, fmt = fmt)
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun ProductSalesRow(item: ProductSalesItem, fmt: java.text.NumberFormat) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.productGrade.ifBlank { "—" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.deliveryType.isNotBlank()) {
                    Text(
                        text = item.deliveryType,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = fmt.format(item.totalQuantity),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.End,
                modifier = Modifier.width(70.dp)
            )
            Text(
                text = "₹${fmt.format(item.totalAmount)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.End,
                modifier = Modifier.width(80.dp)
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun NetProfitTab(
    state: ReportModuleUiState,
    onAction: (ReportModuleAction) -> Unit
) {
    val fmt = remember {
        java.text.NumberFormat.getNumberInstance(java.util.Locale("en", "IN")).apply {
            minimumFractionDigits = 2; maximumFractionDigits = 2
        }
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { DateFilterBar(fromDate = state.fromDate, toDate = state.toDate, onAction = onAction) }
        if (state.dateError != null) {
            item {
                Text(
                    text = state.dateError,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        val profit = state.netProfit
        if (!state.canViewNetProfit) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "Access Restricted",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "Net Profit report is only available to Admin and Manager roles.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        } else if (profit == null) {
            item { EmptyState("No profit data for selected period.") }
        } else {
            val isProfit = profit.netProfit >= 0
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            imageVector = if (isProfit) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown,
                            contentDescription = null,
                            tint = if (isProfit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = if (isProfit) "Net Profit" else "Net Loss",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "₹${fmt.format(profit.netProfit)}",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isProfit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RmStatCard(
                        label = "Total Revenue",
                        value = "₹${fmt.format(profit.totalRevenue)}",
                        valueColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    RmStatCard(
                        label = "Purchase Cost",
                        value = "₹${fmt.format(profit.totalPurchaseCost)}",
                        valueColor = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            if (profit.fromDate.isNotBlank() || profit.toDate.isNotBlank()) {
                item {
                    Text(
                        text = "Period: ${profit.fromDate.take(10)} — ${profit.toDate.take(10)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun ExpenseSummaryTab(
    state: ReportModuleUiState,
    onAction: (ReportModuleAction) -> Unit
) {
    val fmt = remember {
        java.text.NumberFormat.getNumberInstance(java.util.Locale("en", "IN")).apply {
            minimumFractionDigits = 2; maximumFractionDigits = 2
        }
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item { DateFilterBar(fromDate = state.fromDate, toDate = state.toDate, onAction = onAction) }
        if (state.dateError != null) {
            item {
                Text(
                    text = state.dateError,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
        item { Spacer(Modifier.height(8.dp)) }

        if (state.expenseSummaryItems.isEmpty()) {
            item { EmptyState("No expense data for selected period.") }
        } else {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val totalPaid = state.expenseSummaryItems.sumOf { it.totalPaid }
                    RmStatCard(label = "Vendors", value = state.expenseSummaryItems.size.toString(), modifier = Modifier.weight(1f))
                    RmStatCard(label = "Total Paid", value = "₹${fmt.format(totalPaid)}", valueColor = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("Vendor", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("Payments", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.width(72.dp))
                    Text("Total Paid", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.width(82.dp))
                }
            }
            items(state.expenseSummaryItems, key = { it.vendorName }) { item ->
                ExpenseSummaryRow(item = item, fmt = fmt)
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun ExpenseSummaryRow(item: ExpenseSummaryItem, fmt: java.text.NumberFormat) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.vendorName.ifBlank { "—" },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.paymentCount.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.End,
                modifier = Modifier.width(72.dp)
            )
            Text(
                text = "₹${fmt.format(item.totalPaid)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.End,
                modifier = Modifier.width(82.dp)
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun RmStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
