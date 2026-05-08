package com.havos.lubricerp.feature_reports.presentation.customer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.havos.lubricerp.core.ui.components.ErrorPlaceholder
import com.havos.lubricerp.core.ui.components.OfflinePlaceholder
import com.havos.lubricerp.feature_reports.domain.model.Customer
import com.havos.lubricerp.feature_reports.domain.model.CustomerLedgerEntry
import com.havos.lubricerp.feature_reports.domain.model.CustomerMobileSummary
import com.havos.lubricerp.feature_reports.presentation.R
import com.havos.lubricerp.feature_reports.presentation.reports.DatePickerButton
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CustomerDataScreen(
    state: CustomerDataUiState,
    onBackClick: () -> Unit,
    onAction: (CustomerDataAction) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(true)
    val outline = MaterialTheme.colorScheme.outlineVariant
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val showGoToTop = remember { derivedStateOf { listState.firstVisibleItemIndex > 3 } }

    val filtered = remember(state.customers, state.searchQuery) {
        if (state.searchQuery.isBlank()) state.customers
        else state.customers.filter {
            it.name.contains(state.searchQuery, ignoreCase = true) ||
                it.code.contains(state.searchQuery, ignoreCase = true) ||
                it.phone.contains(state.searchQuery)
        }
    }

    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = { onAction(CustomerDataAction.Refresh) },
        modifier = Modifier.fillMaxSize()
    ) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Customers",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = showGoToTop.value,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                FloatingActionButton(
                    onClick = {
                        scope.launch {
                            if (listState.firstVisibleItemIndex > 8) {
                                listState.scrollToItem(5)
                            }
                            listState.animateScrollToItem(0)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowUp,
                        contentDescription = "Go to top",
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            CustomerSearchBar(
                value = state.searchQuery,
                onValueChange = { onAction(CustomerDataAction.SearchChanged(it)) },
                outline = outline,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )

            when {
                state.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                state.isOffline -> {
                    OfflinePlaceholder(
                        onRetry = { onAction(CustomerDataAction.Retry) }
                    )
                }
                state.errorMessage != null -> {
                    ErrorPlaceholder(
                        message = state.errorMessage,
                        onRetry = { onAction(CustomerDataAction.Retry) }
                    )
                }
                filtered.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (state.customers.isEmpty()) "No customers found."
                            else "No results for \"${state.searchQuery}\".",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    CustomerSummaryRow(total = state.customers.size, filtered = filtered.size)
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 70.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        items(filtered, key = { it.id }) { customer ->
                            val outstanding = state.cachedOutstanding[customer.id]
                            CustomerListRow(
                                customer = customer,
                                outstanding = outstanding,
                                onSelect = { onAction(CustomerDataAction.CustomerSelected(customer)) },
                                onCall = { onAction(CustomerDataAction.CallCustomer(customer.phone)) },
                                onWhatsApp = { onAction(CustomerDataAction.WhatsAppCustomer(customer.phone)) }
                            )
                        }
                    }
                }
            }
        }
    }
    } // end PullToRefreshBox

    state.selectedCustomer?.let { customer ->
        ModalBottomSheet(
            onDismissRequest = { onAction(CustomerDataAction.CustomerDismissed) },
            sheetState = sheetState
        ) {
            CustomerDetailSheet(
                customer = customer,
                state = state,
                onAction = onAction
            )
        }
    }
}

@Composable
private fun CustomerSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    outline: Color,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        modifier = modifier,
        decorationBox = { inner ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, outline, RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(
                            text = "Search by name, code or phone…",
                            style = MaterialTheme.typography.bodyMedium,
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
private fun CustomerSummaryRow(total: Int, filtered: Int) {
    Text(
        text = if (filtered == total) "$total customers" else "$filtered of $total customers",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
    )
}

@Composable
private fun CustomerListRow(
    customer: Customer,
    outstanding: Double?,
    onSelect: () -> Unit,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit
) {
    val phoneValid = isValidPhone(customer.phone)
    val addressFirst = remember(customer.address) {
        customer.address.split(",").firstOrNull()?.trim()?.takeIf { it.isNotBlank() }
    }
    val currencyFmt = remember {
        java.text.NumberFormat.getNumberInstance(java.util.Locale("en", "IN")).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onSelect)
                .padding(horizontal = 4.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = customer.name.firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = customer.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${customer.code}${if (customer.phone.isNotBlank()) " · ${customer.phone}" else ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (addressFirst != null) {
                    Text(
                        text = addressFirst,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (outstanding != null && outstanding > 0) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Outstanding",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "₹${currencyFmt.format(outstanding)}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            if (customer.phone.isNotBlank()) {
                IconButton(
                    onClick = { if (phoneValid) onCall() },
                    modifier = Modifier.size(36.dp).alpha(if (phoneValid) 1f else 0.35f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Call,
                        contentDescription = "Call",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                WhatsAppIconButton(enabled = phoneValid, onClick = { if (phoneValid) onWhatsApp() })
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun WhatsAppIconButton(enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .alpha(if (enabled) 1f else 0.35f)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_whatsapp),
            contentDescription = "WhatsApp",
            tint = Color.Unspecified,
            modifier = Modifier.size(22.dp)
        )
    }
}

private fun isValidPhone(phone: String): Boolean {
    val digits = phone.replace(Regex("[^0-9]"), "")
    return digits.length in 7..15
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomerDetailSheet(
    customer: Customer,
    state: CustomerDataUiState,
    onAction: (CustomerDataAction) -> Unit
) {
    val currencyFmt = remember {
        java.text.NumberFormat.getNumberInstance(java.util.Locale("en", "IN")).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
    }
    val outline = MaterialTheme.colorScheme.outlineVariant

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = customer.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = customer.code,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (customer.phone.isNotBlank()) {
                    val phoneValid = isValidPhone(customer.phone)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .alpha(if (phoneValid) 1f else 0.35f)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .clickable { if (phoneValid) onAction(CustomerDataAction.CallCustomer(customer.phone)) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Call,
                                contentDescription = "Call",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .alpha(if (phoneValid) 1f else 0.35f)
                                .background(Color(0xFF25D366).copy(alpha = 0.12f))
                                .clickable { if (phoneValid) onAction(CustomerDataAction.WhatsAppCustomer(customer.phone)) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_whatsapp),
                                contentDescription = "WhatsApp",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            when {
                state.isMobileSummaryLoading -> {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                }
                state.mobileSummary != null -> {
                    CustomerFinancialSummary(
                        summary = state.mobileSummary,
                        currencyFmt = currencyFmt
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            CustomerInfoCards(customer = customer)
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }

        item {
            Text(
                text = "Ledger",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            val fromMillis = remember(state.ledgerFromDate) {
                runCatching {
                    val fmt = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                    fmt.parse(state.ledgerFromDate)?.time
                }.getOrNull()
            }
            val todayMillis = remember {
                java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
                    set(java.util.Calendar.HOUR_OF_DAY, 23)
                    set(java.util.Calendar.MINUTE, 59)
                    set(java.util.Calendar.SECOND, 59)
                    set(java.util.Calendar.MILLISECOND, 999)
                }.timeInMillis
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                DatePickerButton(
                    label = "From",
                    value = state.ledgerFromDate,
                    onDateSelected = { onAction(CustomerDataAction.LedgerFromDateChanged(it)) },
                    modifier = Modifier.weight(1f),
                    maxDateMillis = todayMillis
                )
                DatePickerButton(
                    label = "To",
                    value = state.ledgerToDate,
                    onDateSelected = { onAction(CustomerDataAction.LedgerToDateChanged(it)) },
                    modifier = Modifier.weight(1f),
                    minDateMillis = fromMillis,
                    maxDateMillis = todayMillis
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { onAction(CustomerDataAction.LoadLedger) }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Load",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }

        if (state.isLedgerLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp))
                }
            }
        } else if (state.ledgerEntries.isEmpty()) {
            item {
                Text(
                    text = "No ledger entries for the selected period.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        } else {
            val totalDebit = state.ledgerEntries.sumOf { it.debit }
            val totalCredit = state.ledgerEntries.sumOf { it.credit }
            val closing = state.ledgerEntries.lastOrNull()?.runningBalance ?: 0.0

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LedgerSummaryChip(
                        label = "Debit",
                        value = "₹${currencyFmt.format(totalDebit)}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                    LedgerSummaryChip(
                        label = "Credit",
                        value = "₹${currencyFmt.format(totalCredit)}",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    LedgerSummaryChip(
                        label = "Balance",
                        value = "₹${currencyFmt.format(closing)}",
                        color = if (closing > 0) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LedgerHeaderRow()
            }

            items(
                state.ledgerEntries,
                key = { "${it.date}_${it.refNumber}" }
            ) { entry ->
                LedgerEntryRow(entry = entry, currencyFmt = currencyFmt)
            }
        }

        item { Spacer(modifier = Modifier.height(40.dp)) }
    }
}

@Composable
private fun CustomerFinancialSummary(
    summary: CustomerMobileSummary,
    currencyFmt: java.text.NumberFormat
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Financial Overview",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FinancialKpiCard(
                label = "Outstanding",
                value = "\u20b9${currencyFmt.format(summary.outstandingAmount)}",
                valueColor = if (summary.outstandingAmount > 0) MaterialTheme.colorScheme.error
                             else MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            FinancialKpiCard(
                label = "Overdue",
                value = "\u20b9${currencyFmt.format(summary.overdueAmount)}",
                valueColor = if (summary.overdueAmount > 0) MaterialTheme.colorScheme.error
                             else MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            FinancialKpiCard(
                label = "Lifetime",
                value = "\u20b9${currencyFmt.format(summary.totalLifetimePurchases)}",
                valueColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }
        if (!summary.lastPurchaseDate.isNullOrBlank() || !summary.lastInvoiceNumber.isNullOrBlank()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!summary.lastPurchaseDate.isNullOrBlank()) {
                    FinancialKpiCard(
                        label = "Last Purchase",
                        value = summary.lastPurchaseDate.orEmpty(),
                        valueColor = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (!summary.lastInvoiceNumber.isNullOrBlank()) {
                    FinancialKpiCard(
                        label = "Last Invoice",
                        value = summary.lastInvoiceNumber.orEmpty(),
                        valueColor = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun FinancialKpiCard(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CustomerInfoCards(customer: Customer) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (customer.phone.isNotBlank()) {
            CustomerInfoRow("Phone", customer.phone)
        }
        if (customer.email.isNotBlank()) {
            CustomerInfoRow("Email", customer.email)
        }
        if (customer.state.isNotBlank()) {
            CustomerInfoRow("State", customer.state)
        }
        if (!customer.gstNumber.isNullOrBlank()) {
            CustomerInfoRow("GST", customer.gstNumber.orEmpty())
        }
        if (customer.address.isNotBlank()) {
            CustomerInfoRow("Address", customer.address)
        }
    }
}

@Composable
private fun CustomerInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(72.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun LedgerSummaryChip(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LedgerHeaderRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Date / Ref",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "Debit",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            modifier = Modifier.width(70.dp)
        )
        Text(
            text = "Credit",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            modifier = Modifier.width(70.dp)
        )
        Text(
            text = "Balance",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            modifier = Modifier.width(76.dp)
        )
    }
}

@Composable
private fun LedgerEntryRow(
    entry: CustomerLedgerEntry,
    currencyFmt: java.text.NumberFormat
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${entry.type} · ${entry.refNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = if (entry.debit > 0) "₹${currencyFmt.format(entry.debit)}" else "–",
                style = MaterialTheme.typography.bodySmall,
                color = if (entry.debit > 0) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
                modifier = Modifier.width(70.dp)
            )
            Text(
                text = if (entry.credit > 0) "₹${currencyFmt.format(entry.credit)}" else "–",
                style = MaterialTheme.typography.bodySmall,
                color = if (entry.credit > 0) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
                modifier = Modifier.width(70.dp)
            )
            Text(
                text = "₹${currencyFmt.format(entry.runningBalance)}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = if (entry.runningBalance > 0) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.End,
                modifier = Modifier.width(76.dp)
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}
