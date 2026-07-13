package com.havos.lubricerp.feature_reports.presentation.proforma

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.havos.lubricerp.core.ui.components.ErrorPlaceholder
import com.havos.lubricerp.core.ui.components.OfflinePlaceholder
import com.havos.lubricerp.core.ui.components.StatCard
import com.havos.lubricerp.feature_reports.domain.model.ProformaInvoice
import com.havos.lubricerp.feature_reports.presentation.reports.DatePickerButton
import com.havos.lubricerp.feature_reports.presentation.reports.ReportLoadingScreen
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun ProformaInvoiceRoute(
    onBackClick: () -> Unit,
    onInvoiceClick: (Long) -> Unit,
    onCreateClick: () -> Unit,
    refreshTrigger: Boolean = false,
    viewModel: ProformaInvoiceViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger) {
            viewModel.onIntent(ProformaInvoiceIntent.Refresh)
        }
    }
    
    ProformaInvoiceScreen(
        state = state,
        onBackClick = onBackClick,
        onInvoiceClick = onInvoiceClick,
        onCreateClick = onCreateClick,
        onIntent = viewModel::onIntent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProformaInvoiceScreen(
    state: ProformaInvoiceUiState,
    onBackClick: () -> Unit,
    onInvoiceClick: (Long) -> Unit,
    onCreateClick: () -> Unit,
    onIntent: (ProformaInvoiceIntent) -> Unit
) {
    var showSortMenu by remember { mutableStateOf(false) }
    var showFilterSection by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Proforma Invoice"
                )
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Proforma Invoices",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterSection = !showFilterSection }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = if (state.selectedStatus != null || !state.fromDate.isNullOrBlank() || !state.toDate.isNullOrBlank() || state.selectedCustomerName != null) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Sort"
                            )
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            SortType.entries.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.title) },
                                    onClick = {
                                        onIntent(ProformaInvoiceIntent.SortTypeChanged(type))
                                        showSortMenu = false
                                    },
                                    leadingIcon = {
                                        if (state.sortType == type) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected"
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)

        if (state.isLoading) {
            ReportLoadingScreen(modifier = contentModifier)
            return@Scaffold
        }

        if (state.isOffline) {
            OfflinePlaceholder(
                onRetry = { onIntent(ProformaInvoiceIntent.LoadInvoices) },
                modifier = contentModifier
            )
            return@Scaffold
        }

        state.error?.let { message ->
            ErrorPlaceholder(
                message = message,
                onRetry = { onIntent(ProformaInvoiceIntent.LoadInvoices) },
                modifier = contentModifier
            )
            return@Scaffold
        }

        Column(
            modifier = contentModifier,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Search Input Row
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { onIntent(ProformaInvoiceIntent.SearchChanged(it)) },
                placeholder = { Text("Search by customer, invoice no...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onIntent(ProformaInvoiceIntent.SearchChanged("")) }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp)
            )

            // Collapsible Date & Customer Filter Section
            AnimatedVisibility(
                visible = showFilterSection,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        var customerMenuExpanded by remember { mutableStateOf(false) }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { customerMenuExpanded = true }
                        ) {
                            OutlinedTextField(
                                value = state.selectedCustomerName ?: "All Customers",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Filter by Customer") },
                                trailingIcon = {
                                    Icon(
                                        imageVector = if (customerMenuExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                        contentDescription = null
                                    )
                                },
                                enabled = false,
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            DropdownMenu(
                                expanded = customerMenuExpanded,
                                onDismissRequest = { customerMenuExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.85f)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("All Customers") },
                                    onClick = {
                                        onIntent(ProformaInvoiceIntent.CustomerFilterChanged(null))
                                        customerMenuExpanded = false
                                    }
                                )
                                state.customers.forEach { customer ->
                                    DropdownMenuItem(
                                        text = { Text(customer.name) },
                                        onClick = {
                                            onIntent(ProformaInvoiceIntent.CustomerFilterChanged(customer.name))
                                            customerMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            DatePickerButton(
                                label = "From Date",
                                value = state.fromDate.orEmpty(),
                                onDateSelected = { onIntent(ProformaInvoiceIntent.DateFilterChanged(it, state.toDate)) },
                                modifier = Modifier.weight(1f)
                            )
                            DatePickerButton(
                                label = "To Date",
                                value = state.toDate.orEmpty(),
                                onDateSelected = { onIntent(ProformaInvoiceIntent.DateFilterChanged(state.fromDate, it)) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (!state.fromDate.isNullOrBlank() || !state.toDate.isNullOrBlank() || state.selectedCustomerName != null) {
                            TextButton(
                                onClick = { 
                                    onIntent(ProformaInvoiceIntent.DateFilterChanged(null, null))
                                    onIntent(ProformaInvoiceIntent.CustomerFilterChanged(null))
                                },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Clear Filters")
                            }
                        }
                    }
                }
            }

            // Horizontal status chips
            val statuses = listOf("All", "Draft", "Sent", "Converted", "Expired", "Cancelled")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                statuses.forEach { statusName ->
                    val isSelected = if (statusName == "All") state.selectedStatus == null else state.selectedStatus == statusName
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            val newStatus = if (statusName == "All") null else statusName
                            onIntent(ProformaInvoiceIntent.StatusFilterChanged(newStatus))
                        },
                        label = { Text(statusName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            // Quick Stats panel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Total Value",
                    value = formatCurrency(state.totalAmount),
                    modifier = Modifier.weight(1.2f)
                )
                StatCard(
                    title = "Sent",
                    value = state.sentCount.toString(),
                    modifier = Modifier.weight(0.9f)
                )
                StatCard(
                    title = "Converted",
                    value = state.convertedCount.toString(),
                    modifier = Modifier.weight(0.9f)
                )
            }

            // Invoices Listing
            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = { onIntent(ProformaInvoiceIntent.Refresh) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (state.filteredInvoices.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "No Proforma Invoices found",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Try adjusting your search or filters",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.filteredInvoices, key = { it.id }) { invoice ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onInvoiceClick(invoice.id) }
                            ) {
                                ProformaInvoiceRow(invoice = invoice)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProformaInvoiceRow(invoice: ProformaInvoice) {
    val statusColor = when (invoice.status.lowercase(Locale.ROOT)) {
        "converted" -> Color(0xFF4CAF50)
        "sent" -> Color(0xFF2196F3)
        "draft" -> Color(0xFF9E9E9E)
        "expired" -> Color(0xFFFF9800)
        "cancelled" -> Color(0xFFF44336)
        else -> MaterialTheme.colorScheme.outline
    }

    val displayFmt = DateTimeFormatter.ofPattern("dd MMM yyyy")
    val parsedDate = parseInvoiceDate(invoice.date)
    val dateStr = if (parsedDate != null) parsedDate.format(displayFmt) else invoice.date
    
    val parsedValidDate = parseInvoiceDate(invoice.validUntilDate)
    val validDateStr = if (parsedValidDate != null) parsedValidDate.format(displayFmt) else invoice.validUntilDate

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = invoice.proformaNumber,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    if (invoice.isInterState) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.tertiaryContainer)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Interstate",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = invoice.customerName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "DATE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = dateStr,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column {
                        Text(
                            text = "VALID UNTIL",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = validDateStr,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                if (invoice.soNumber != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Linked to: ${invoice.soNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = invoice.status.uppercase(Locale.ROOT),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }

                Text(
                    text = formatCurrency(invoice.totalAmount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "${invoice.lineCount} item(s)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

private fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    return formatter.format(amount)
}

private fun parseInvoiceDate(dateStr: String): java.time.LocalDate? {
    if (dateStr.isBlank()) return null
    return try {
        LocalDateTime.parse(dateStr, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME).toLocalDate()
    } catch (_: Exception) {
        try {
            java.time.LocalDate.parse(dateStr, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (_: Exception) {
            null
        }
    }
}
