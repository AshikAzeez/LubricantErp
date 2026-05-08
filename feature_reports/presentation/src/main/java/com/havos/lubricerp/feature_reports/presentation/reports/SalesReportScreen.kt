package com.havos.lubricerp.feature_reports.presentation.reports

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutQuart
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.havos.lubricerp.feature_reports.domain.model.PaymentReceivedItem
import com.havos.lubricerp.feature_reports.domain.model.SalesSummaryItem

@Composable
internal fun SalesSummaryScreen(
    state: ReportDetailUiState,
    onAction: (ReportDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    var sortField by remember { mutableStateOf(SalesSortField.NONE) }
    var sortAsc by remember { mutableStateOf(true) }

    val items = state.salesSummaryItems
    val totalAmount = remember(items) { items.sumOf { it.totalAmount } }
    val totalInvoices = remember(items) { items.sumOf { it.invoiceCount } }
    val totalBalance = remember(items) { items.sumOf { it.balanceAmount } }
    val sorted = remember(items, state.showTopCustomers, sortField, sortAsc) {
        val base = if (state.showTopCustomers)
            items.sortedByDescending { it.totalAmount }.take(10)
        else
            items
        when (sortField) {
            SalesSortField.NONE -> base
            SalesSortField.CUSTOMER -> if (sortAsc) base.sortedBy { it.customerName.lowercase() }
                                       else base.sortedByDescending { it.customerName.lowercase() }
            SalesSortField.TOTAL -> if (sortAsc) base.sortedBy { it.totalAmount }
                                    else base.sortedByDescending { it.totalAmount }
            SalesSortField.BALANCE -> if (sortAsc) base.sortedBy { it.balanceAmount }
                                      else base.sortedByDescending { it.balanceAmount }
        }
    }
    val filtered = remember(sorted, state.searchQuery) {
        if (state.searchQuery.isBlank()) sorted
        else sorted.filter {
            it.customerName.contains(state.searchQuery, ignoreCase = true) ||
                it.customerCode.contains(state.searchQuery, ignoreCase = true)
        }
    }
    val payments = state.paymentReceivedItems
    val totalReceived = remember(payments) { payments.sumOf { it.amount } }
    val currencyFmt = remember {
        java.text.NumberFormat.getNumberInstance(java.util.Locale("en", "IN")).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
    }
    val utcDateFmt = remember { java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).apply { timeZone = java.util.TimeZone.getTimeZone("UTC") } }
    val fromMillis = remember(state.fromDate) { runCatching { utcDateFmt.parse(state.fromDate)?.time }.getOrNull() }
    val toMillis = remember(state.toDate) { runCatching { utcDateFmt.parse(state.toDate)?.time }.getOrNull() }
    val outline = MaterialTheme.colorScheme.outlineVariant
    val chipShape = remember { RoundedCornerShape(6.dp) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val showScrollTop = remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
    val keyboardController = LocalSoftwareKeyboardController.current

    // snapshotFlow is correct here: emits only when isScrollInProgress transitions to true,
    // without restarting the entire effect on every Boolean change.
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .collect { scrolling -> if (scrolling) keyboardController?.hide() }
    }

    Box(modifier = modifier) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = 12.dp,
            vertical = 8.dp
        )
    ) {
        item {
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
                    value = state.fromDate,
                    onDateSelected = { onAction(ReportDetailAction.FromDateChanged(it)) },
                    modifier = Modifier.weight(1f),
                    maxDateMillis = toMillis
                )
                DatePickerButton(
                    label = "To",
                    value = state.toDate,
                    onDateSelected = { onAction(ReportDetailAction.ToDateChanged(it)) },
                    modifier = Modifier.weight(1f),
                    minDateMillis = fromMillis
                )
            }
        }

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

        item { Spacer(modifier = Modifier.height(6.dp)) }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MiniStatCard(
                    label = "Sales",
                    value = "₹${currencyFmt.format(totalAmount)}",
                    modifier = Modifier.weight(1f)
                )
                MiniStatCard(
                    label = "Invoices",
                    value = totalInvoices.toString(),
                    modifier = Modifier.weight(1f)
                )
                MiniStatCard(
                    label = "Outstanding",
                    value = "₹${currencyFmt.format(totalBalance)}",
                    valueColor = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Payment Received Today",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MiniStatCard(label = "Receipts", value = payments.size.toString())
                    MiniStatCard(
                        label = "Received",
                        value = "₹${currencyFmt.format(totalReceived)}",
                        valueColor = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(4.dp)) }

        if (payments.isEmpty()) {
            item {
                Text(
                    text = "No payments received today.",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        } else {
            item { SectionHeader(col1 = "Customer / Receipt", col2 = "Amount") }
            items(payments, key = { it.receiptNumber }) { payment ->
                PaymentReceivedRow(item = payment, currencyFmt = currencyFmt)
            }
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CompactSearchBar(
                    value = state.searchQuery,
                    onValueChange = { onAction(ReportDetailAction.SearchChanged(it)) },
                    placeholder = "Search customer…",
                    modifier = Modifier.weight(1f),
                    outline = outline
                )
                TopCustomersToggle(
                    selected = state.showTopCustomers,
                    onClick = { onAction(ReportDetailAction.ToggleTopCustomers) },
                    chipShape = chipShape
                )
            }
        }

        item { Spacer(modifier = Modifier.height(4.dp)) }

        if (filtered.isEmpty()) {
            item {
                Text(
                    text = if (items.isEmpty()) "No data for selected period."
                    else "No results for \"${state.searchQuery}\".",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
        } else {
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
            items(filtered, key = { it.customerId }) { item ->
                SalesCustomerRow(item = item, currencyFmt = currencyFmt)
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }

    AnimatedVisibility(
        visible = showScrollTop.value,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(end = 16.dp, bottom = 24.dp)
            .navigationBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .shadow(6.dp, CircleShape)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable {
                    scope.launch {
                        val totalOffset = listState.firstVisibleItemIndex *
                            listState.layoutInfo.visibleItemsInfo.firstOrNull()?.size?.toFloat().let { it ?: 0f } +
                            listState.firstVisibleItemScrollOffset.toFloat()
                        listState.animateScrollBy(
                            value = -totalOffset,
                            animationSpec = tween(durationMillis = 600, easing = EaseInOutQuart)
                        )
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowUp,
                contentDescription = "Scroll to top",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
    }
}


@Composable
private fun CompactSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    outline: Color
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        modifier = modifier,
        decorationBox = { inner ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, outline, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    inner()
                }
                if (value.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Clear",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { onValueChange("") }
                    )
                }
            }
        }
    )
}

@Composable
private fun TopCustomersToggle(
    selected: Boolean,
    onClick: () -> Unit,
    chipShape: RoundedCornerShape
) {
    val bg = if (selected) MaterialTheme.colorScheme.primaryContainer
             else MaterialTheme.colorScheme.surfaceContainer
    val fg = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
             else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = Modifier
            .clip(chipShape)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = fg,
            modifier = Modifier.size(14.dp)
        )
        Text(text = "Top", style = MaterialTheme.typography.labelLarge, color = fg)
    }
}

@Composable
private fun MiniStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 8.dp, vertical = 5.dp)
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

internal enum class SalesSortField { NONE, CUSTOMER, TOTAL, BALANCE }

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
        SortableColumnLabel(
            text = "Customer",
            field = SalesSortField.CUSTOMER,
            active = sortField == SalesSortField.CUSTOMER,
            asc = sortAsc,
            onClick = { onSort(SalesSortField.CUSTOMER) },
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Start
        )
        SortableColumnLabel(
            text = "Total",
            field = SalesSortField.TOTAL,
            active = sortField == SalesSortField.TOTAL,
            asc = sortAsc,
            onClick = { onSort(SalesSortField.TOTAL) },
            modifier = Modifier.width(76.dp),
            textAlign = TextAlign.End
        )
        SortableColumnLabel(
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
private fun SortableColumnLabel(
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
                modifier = Modifier.size(13.dp)
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
                modifier = Modifier.size(13.dp)
            )
        }
    }
}

@Composable
fun SectionHeader(
    col1: String,
    col2: String,
    col3: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .padding(horizontal = 12.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = col1,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        if (col3 != null) {
            Text(
                text = col2,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                modifier = Modifier.width(76.dp)
            )
            Text(
                text = col3,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                modifier = Modifier.width(76.dp)
            )
        } else {
            Text(
                text = col2,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                modifier = Modifier.width(80.dp)
            )
        }
    }
}

@Composable
internal fun ProductWiseSalesScreen(
    state: ReportDetailUiState,
    onAction: (ReportDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    TwoMetricReportScreen(
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
internal fun CustomerOutstandingScreen(
    state: ReportDetailUiState,
    onAction: (ReportDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    TwoMetricReportScreen(
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
internal fun SalesReturnSummaryScreen(
    state: ReportDetailUiState,
    onAction: (ReportDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    TwoMetricReportScreen(
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
internal fun SalesmanPerformanceScreen(
    state: ReportDetailUiState,
    onAction: (ReportDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    TwoMetricReportScreen(
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
internal fun StateWiseSalesScreen(
    state: ReportDetailUiState,
    onAction: (ReportDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    TwoMetricReportScreen(
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
internal fun DistrictWiseSalesScreen(
    state: ReportDetailUiState,
    onAction: (ReportDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    TwoMetricReportScreen(
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
private fun SalesCustomerRow(
    item: SalesSummaryItem,
    currencyFmt: java.text.NumberFormat
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
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
                    text = "${item.customerCode} · ${item.invoiceCount} inv",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "₹${currencyFmt.format(item.totalAmount)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.End,
                modifier = Modifier.width(76.dp)
            )
            Text(
                text = "₹${currencyFmt.format(item.balanceAmount)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (item.balanceAmount > 0) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.End,
                modifier = Modifier.width(76.dp)
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun PaymentReceivedRow(
    item: PaymentReceivedItem,
    currencyFmt: java.text.NumberFormat
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
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
                    text = "${item.receiptNumber} · ${item.invoiceNumber} · ${item.paymentMode}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "₹${currencyFmt.format(item.amount)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.End,
                modifier = Modifier.width(80.dp)
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}
