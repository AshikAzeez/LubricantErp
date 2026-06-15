package com.havos.lubricerp.feature_reports.presentation.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CurrencyRupee
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.PersonOff
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.RemoveShoppingCart
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.IconButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.havos.lubricerp.core.ui.components.CollectEffect
import com.havos.lubricerp.core.ui.components.shimmerBrush
import com.havos.lubricerp.feature_reports.domain.model.AccountsSummary
import com.havos.lubricerp.feature_reports.domain.model.PaymentPendingCustomer
import com.havos.lubricerp.feature_reports.domain.model.PaymentReceivedItem
import com.havos.lubricerp.feature_reports.domain.model.RecordPaymentResponse
import com.havos.lubricerp.feature_reports.presentation.reports.DatePickerButton
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PaymentReportRoute(
    onBackClick: () -> Unit,
    viewModel: PaymentReportViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    CollectEffect(effects = viewModel.effect) { effect ->
        when (effect) {
            PaymentReportEffect.PaymentSuccess -> onBackClick()
        }
    }

    PaymentReportScreen(
        state = state,
        onAction = { action ->
            when (action) {
                is PaymentReportAction.FromDateReceivedChanged -> viewModel.onIntent(
                    PaymentReportIntent.FromDateReceivedChanged(action.value)
                )
                is PaymentReportAction.ToDateReceivedChanged -> viewModel.onIntent(
                    PaymentReportIntent.ToDateReceivedChanged(action.value)
                )
                PaymentReportAction.ApplyReceivedFilter -> viewModel.onIntent(
                    PaymentReportIntent.ApplyReceivedFilter
                )
                is PaymentReportAction.FromDateAccountsChanged -> viewModel.onIntent(
                    PaymentReportIntent.FromDateAccountsChanged(action.value)
                )
                is PaymentReportAction.ToDateAccountsChanged -> viewModel.onIntent(
                    PaymentReportIntent.ToDateAccountsChanged(action.value)
                )
                PaymentReportAction.ApplyAccountsFilter -> viewModel.onIntent(
                    PaymentReportIntent.ApplyAccountsFilter
                )
                PaymentReportAction.ToggleOverdueOnly -> viewModel.onIntent(
                    PaymentReportIntent.ToggleOverdueOnly
                )
                PaymentReportAction.OpenCollectPayment -> viewModel.onIntent(
                    PaymentReportIntent.OpenCollectPayment
                )
                PaymentReportAction.DismissCollectPayment -> viewModel.onIntent(
                    PaymentReportIntent.DismissCollectPayment
                )
                is PaymentReportAction.PaymentInvoiceSelected -> viewModel.onIntent(
                    PaymentReportIntent.PaymentInvoiceSelected(action.invoiceId)
                )
                is PaymentReportAction.PaymentAmountChanged -> viewModel.onIntent(
                    PaymentReportIntent.PaymentAmountChanged(action.value)
                )
                is PaymentReportAction.PaymentModeChanged -> viewModel.onIntent(
                    PaymentReportIntent.PaymentModeChanged(action.mode)
                )
                is PaymentReportAction.PaymentDateChanged -> viewModel.onIntent(
                    PaymentReportIntent.PaymentDateChanged(action.date)
                )
                is PaymentReportAction.PaymentReferenceChanged -> viewModel.onIntent(
                    PaymentReportIntent.PaymentReferenceChanged(action.value)
                )
                is PaymentReportAction.PaymentRemarksChanged -> viewModel.onIntent(
                    PaymentReportIntent.PaymentRemarksChanged(action.value)
                )
                PaymentReportAction.SubmitPayment -> viewModel.onIntent(
                    PaymentReportIntent.SubmitPayment
                )
                PaymentReportAction.Retry -> viewModel.onIntent(PaymentReportIntent.Retry)
                PaymentReportAction.Refresh -> viewModel.onIntent(PaymentReportIntent.Refresh)
            }
        },
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PaymentReportScreen(
    state: PaymentReportUiState,
    onAction: (PaymentReportAction) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tabTitles = listOf("Received Today", "Pending Collections", "Overdue Customers", "Accounts Summary")
    val pagerState = rememberPagerState(pageCount = { tabTitles.size })
    val scope = rememberCoroutineScope()
    val currencyFmt = remember {
        NumberFormat.getNumberInstance(Locale("en", "IN")).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment Report") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { onAction(PaymentReportAction.Refresh) },
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                ScrollableTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    contentColor = MaterialTheme.colorScheme.primary,
                    edgePadding = 16.dp,
                    indicator = { tabPositions ->
                        if (pagerState.currentPage < tabPositions.size) {
                            val tabPosition = tabPositions[pagerState.currentPage]
                            Box(
                                modifier = Modifier
                                    .tabIndicatorOffset(tabPosition)
                                    .padding(horizontal = 4.dp, vertical = 6.dp)
                                    .fillMaxSize()
                                    .background(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                            )
                        }
                    },
                    divider = {}
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                            selectedContentColor = MaterialTheme.colorScheme.primary,
                            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            text = {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) { page ->
                    when (page) {
                        0 -> ReceivedTodayTab(state, onAction, currencyFmt)
                        1 -> PendingCollectionsTab(state, onAction, currencyFmt)
                        2 -> OverdueCustomersTab(state, onAction, currencyFmt)
                        3 -> AccountsSummaryTab(state, onAction, currencyFmt)
                    }
                }
            }
        }
    }

    if (state.showCollectPayment) {
        CollectPaymentSheet(state = state, onAction = onAction)
    }
}

@Composable
private fun ReceivedTodayTab(
    state: PaymentReportUiState,
    onAction: (PaymentReportAction) -> Unit,
    currencyFmt: NumberFormat
) {
    val utcFmt = remember {
        java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }
    }
    val fromMillis = remember(state.receivedDateFrom) {
        runCatching { utcFmt.parse(state.receivedDateFrom)?.time }.getOrNull()
    }
    val toMillis = remember(state.receivedDateTo) {
        runCatching { utcFmt.parse(state.receivedDateTo)?.time }.getOrNull()
    }

    if (state.isLoading && state.receivedItems.isEmpty()) {
        PaymentShimmer()
        return
    }

    if (state.errorMessage != null && state.receivedItems.isEmpty()) {
        ErrorState(message = state.errorMessage, onRetry = { onAction(PaymentReportAction.Retry) })
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                    value = state.receivedDateFrom,
                    onDateSelected = { onAction(PaymentReportAction.FromDateReceivedChanged(it)) },
                    modifier = Modifier.weight(1f),
                    maxDateMillis = toMillis
                )
                DatePickerButton(
                    label = "To",
                    value = state.receivedDateTo,
                    onDateSelected = { onAction(PaymentReportAction.ToDateReceivedChanged(it)) },
                    modifier = Modifier.weight(1f),
                    minDateMillis = fromMillis
                )
                Button(
                    onClick = { onAction(PaymentReportAction.ApplyReceivedFilter) },
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) { Text("Search") }
            }
        }

        item {
            Text(
                text = "Payments Received (${state.receivedItems.size})",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (state.receivedItems.isEmpty()) {
            item {
                EmptyState(icon = Icons.Outlined.Payments, message = "No payments received in this period.")
            }
        } else {
            items(state.receivedItems, key = { it.id }) { item ->
                PaymentReceivedRow(item = item, currencyFmt = currencyFmt)
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun PendingCollectionsTab(
    state: PaymentReportUiState,
    onAction: (PaymentReportAction) -> Unit,
    currencyFmt: NumberFormat
) {
    val items = remember(state.pendingItems, state.overdueOnly) {
        if (state.overdueOnly) state.pendingItems.filter { it.isOverdue }
        else state.pendingItems
    }

    if (state.isLoading && state.pendingItems.isEmpty()) {
        PaymentShimmer()
        return
    }

    if (state.errorMessage != null && state.pendingItems.isEmpty()) {
        ErrorState(message = state.errorMessage, onRetry = { onAction(PaymentReportAction.Retry) })
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pending Collections (${items.size})",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FilterChip(
                    selected = state.overdueOnly,
                    onClick = { onAction(PaymentReportAction.ToggleOverdueOnly) },
                    label = { Text("Overdue Only") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                )
            }
        }

        if (items.isEmpty()) {
            item {
                EmptyState(icon = Icons.Outlined.HourglassEmpty, message = "No pending collections.")
            }
        } else {
            items(items, key = { it.customerId }) { customer ->
                PendingCustomerRow(customer = customer, currencyFmt = currencyFmt, onCollect = { onAction(PaymentReportAction.OpenCollectPayment) })
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun OverdueCustomersTab(
    state: PaymentReportUiState,
    onAction: (PaymentReportAction) -> Unit,
    currencyFmt: NumberFormat
) {
    val overdueItems = remember(state.pendingItems) {
        state.pendingItems.filter { it.isOverdue }
    }

    if (state.isLoading && state.pendingItems.isEmpty()) {
        PaymentShimmer()
        return
    }

    if (state.errorMessage != null && state.pendingItems.isEmpty()) {
        ErrorState(message = state.errorMessage, onRetry = { onAction(PaymentReportAction.Retry) })
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "Overdue Customers (${overdueItems.size})",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (overdueItems.isEmpty()) {
            item {
                EmptyState(icon = Icons.Outlined.PersonOff, message = "No overdue customers.")
            }
        } else {
            items(overdueItems, key = { it.customerId }) { customer ->
                PendingCustomerRow(customer = customer, currencyFmt = currencyFmt, onCollect = { onAction(PaymentReportAction.OpenCollectPayment) })
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AccountsSummaryTab(
    state: PaymentReportUiState,
    onAction: (PaymentReportAction) -> Unit,
    currencyFmt: NumberFormat
) {
    val utcFmt = remember {
        java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }
    }
    val fromMillis = remember(state.accountsDateFrom) {
        runCatching { utcFmt.parse(state.accountsDateFrom)?.time }.getOrNull()
    }
    val toMillis = remember(state.accountsDateTo) {
        runCatching { utcFmt.parse(state.accountsDateTo)?.time }.getOrNull()
    }

    if (state.isLoading && state.accountsSummary == null) {
        PaymentShimmer()
        return
    }

    if (state.errorMessage != null && state.accountsSummary == null) {
        ErrorState(message = state.errorMessage, onRetry = { onAction(PaymentReportAction.Retry) })
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
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
                    value = state.accountsDateFrom,
                    onDateSelected = { onAction(PaymentReportAction.FromDateAccountsChanged(it)) },
                    modifier = Modifier.weight(1f),
                    maxDateMillis = toMillis
                )
                DatePickerButton(
                    label = "To",
                    value = state.accountsDateTo,
                    onDateSelected = { onAction(PaymentReportAction.ToDateAccountsChanged(it)) },
                    modifier = Modifier.weight(1f),
                    minDateMillis = fromMillis
                )
                Button(
                    onClick = { onAction(PaymentReportAction.ApplyAccountsFilter) },
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) { Text("Apply") }
            }
        }

        val summary = state.accountsSummary
        if (summary != null) {
            item {
                Text(
                    text = "KPI Summary",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                val items = kpiItems(
                    summary = summary,
                    currencyFmt = currencyFmt,
                    primary = MaterialTheme.colorScheme.primary,
                    secondary = MaterialTheme.colorScheme.secondary,
                    tertiary = MaterialTheme.colorScheme.tertiary,
                    error = MaterialTheme.colorScheme.error,
                    onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // KPI cards in a fixed 2-column grid. FlowRow does not support
                // Modifier.weight on its children (weight only applies inside
                // Row/Column), so each card was sizing itself to its own
                // content and producing inconsistent widths/heights. Chunking
                // into rows of 2 and giving each card a fixed height fixes
                // both dimensions consistently across all cards.
                val kpiCardHeight = 92.dp
                items.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowItems.forEach { kpi ->
                            KpiCard(
                                label = kpi.label,
                                value = kpi.value,
                                icon = kpi.icon,
                                valueColor = kpi.color,
                                gradientColors = kpi.gradientColors ?: emptyList(),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(kpiCardHeight)
                            )
                        }
                        // If the last row has an odd number of items, add a
                        // spacer to keep that card at the same width as the
                        // others (half the row) instead of stretching full width.
                        if (rowItems.size < 2) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        } else {
            item {
                EmptyState(icon = Icons.Outlined.AccountBalance, message = "No accounts summary available.")
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun CollectPaymentSheet(
    state: PaymentReportUiState,
    onAction: (PaymentReportAction) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = { onAction(PaymentReportAction.DismissCollectPayment) },
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Collect Payment",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (state.paymentResult != null) {
                PaymentSuccessCard(result = state.paymentResult)
            } else {
                Text(
                    text = "Select Invoice",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                InvoiceSelector(
                    pendingItems = state.pendingItems,
                    selectedInvoiceId = state.paymentInvoiceId,
                    currencyFmt = remember {
                        NumberFormat.getNumberInstance(Locale("en", "IN")).apply {
                            minimumFractionDigits = 2
                            maximumFractionDigits = 2
                        }
                    },
                    onInvoiceSelected = { onAction(PaymentReportAction.PaymentInvoiceSelected(it)) }
                )

                OutlinedTextField(
                    value = state.paymentAmount,
                    onValueChange = { onAction(PaymentReportAction.PaymentAmountChanged(it)) },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    prefix = { Text("₹ ") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Payment Mode",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                PaymentModeChips(
                    selectedMode = state.paymentMode,
                    onModeSelected = { onAction(PaymentReportAction.PaymentModeChanged(it)) }
                )

                DatePickerButton(
                    label = "Payment Date",
                    value = state.paymentDate,
                    onDateSelected = { onAction(PaymentReportAction.PaymentDateChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    maxDateMillis = System.currentTimeMillis()
                )

                OutlinedTextField(
                    value = state.paymentReference,
                    onValueChange = { onAction(PaymentReportAction.PaymentReferenceChanged(it)) },
                    label = { Text("Reference") },
                    singleLine = true,
                    supportingText = { Text("${state.paymentReference.length}/30") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = state.paymentRemarks,
                    onValueChange = { onAction(PaymentReportAction.PaymentRemarksChanged(it)) },
                    label = { Text("Remarks") },
                    maxLines = 3,
                    supportingText = { Text("${state.paymentRemarks.length}/100") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (state.paymentError != null) {
                    Text(
                        text = state.paymentError,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = { onAction(PaymentReportAction.DismissCollectPayment) },
                        modifier = Modifier.weight(1f)
                    ) { Text("Cancel") }
                    Button(
                        onClick = { onAction(PaymentReportAction.SubmitPayment) },
                        modifier = Modifier.weight(1f),
                        enabled = !state.isRecordingPayment
                    ) {
                        if (state.isRecordingPayment) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Record Payment")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InvoiceSelector(
    pendingItems: List<PaymentPendingCustomer>,
    selectedInvoiceId: Long,
    currencyFmt: NumberFormat,
    onInvoiceSelected: (Long) -> Unit
) {
    val candidates = remember(pendingItems) {
        pendingItems.filter { it.outstandingAmount > 0 }
    }

    if (candidates.isEmpty()) {
        Text(
            text = "No pending invoices available.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        candidates.forEach { customer ->
            FilterChip(
                selected = selectedInvoiceId == customer.customerId,
                onClick = { onInvoiceSelected(customer.customerId) },
                label = {
                    Text(
                        text = "${customer.customerName} (₹${currencyFmt.format(customer.outstandingAmount)})",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PaymentModeChips(
    selectedMode: String,
    onModeSelected: (String) -> Unit
) {
    val modes = listOf("Cash", "Cheque", "NEFT", "RTGS", "UPI", "BankTransfer", "Other")
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        modes.forEach { mode ->
            FilterChip(
                selected = selectedMode == mode,
                onClick = { onModeSelected(mode) },
                label = { Text(mode, style = MaterialTheme.typography.labelMedium) }
            )
        }
    }
}

@Composable
private fun PaymentSuccessCard(result: RecordPaymentResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Payment Recorded Successfully",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "Receipt: ${result.receiptNumber}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "Amount: ${formatCurrency(result.amountPaid)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "New Balance: ${formatCurrency(result.newBalance)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "Status: ${result.newPaymentStatus}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun PaymentReceivedRow(
    item: PaymentReceivedItem,
    currencyFmt: NumberFormat
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Receipt,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${item.receiptNumber} · ${item.invoiceNumber}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Payments,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.paymentMode,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${currencyFmt.format(item.amount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (item.paymentDate.isNotBlank()) {
                    Text(
                        text = item.paymentDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
internal fun PendingCustomerRow(
    customer: PaymentPendingCustomer,
    currencyFmt: NumberFormat,
    onCollect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = customer.customerName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (customer.isOverdue) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f))
                            .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "OVERDUE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Outstanding: ₹${currencyFmt.format(customer.outstandingAmount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${customer.unpaidInvoiceCount} invoice(s)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (customer.oldestDueDate.isNotBlank()) {
                Text(
                    text = "Oldest due: ${customer.oldestDueDate}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = onCollect,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.CurrencyRupee,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Collect Payment", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun KpiCard(
    label: String,
    value: String,
    icon: ImageVector,
    valueColor: Color,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .background(
                Brush.linearGradient(
                    // Soften the gradient into a tint instead of a solid fill,
                    // so it sits subtly behind the theme surface color and
                    // never overpowers text contrast.
                    colors = gradientColors.map { it.copy(alpha = 0.16f) }
                )
            )
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ── Title row: icon badge + label, identifies what this card represents ──
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(colors = gradientColors)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        // White icon reads reliably on top of the saturated
                        // gradient chip, regardless of theme.
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    // Always read against the surfaceContainer base, not the
                    // gradient, so contrast is consistent in light/dark theme.
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp
                )
            }

            // ── Value: the actual number/amount for this KPI ──
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun EmptyState(icon: ImageVector, message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            Button(onClick = onRetry) { Text("Retry") }
        }
    }
}

@Composable
private fun PaymentShimmer() {
    val brush = shimmerBrush()
    LazyColumn(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(brush)
            )
        }
        items(5) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(brush)
            )
        }
    }
}

private fun formatCurrency(amount: Double): String {
    val fmt = NumberFormat.getNumberInstance(Locale("en", "IN"))
    fmt.minimumFractionDigits = 2
    fmt.maximumFractionDigits = 2
    return "\u20B9${fmt.format(amount)}"
}

private data class KpiItem(
    val label: String,
    val value: String,
    val icon: ImageVector,
    val color: Color,
    val gradientColors: List<Color>? = null
)

@Composable
private fun kpiItems(
    summary: AccountsSummary,
    currencyFmt: NumberFormat,
    primary: Color,
    secondary: Color,
    tertiary: Color,
    error: Color,
    onSurfaceVariant: Color
): List<KpiItem> {
    return listOf(
        KpiItem(
            "Total Sales", 
            "\u20B9${currencyFmt.format(summary.totalSales)}", 
            Icons.Outlined.CurrencyRupee, 
            primary,
            gradientColors = listOf(com.havos.lubricerp.core.ui.theme.GradientSalesStart, com.havos.lubricerp.core.ui.theme.GradientSalesEnd)
        ),
        KpiItem(
            "Total Purchases", 
            "\u20B9${currencyFmt.format(summary.totalPurchases)}", 
            Icons.Outlined.AccountBalance, 
            secondary,
            gradientColors = listOf(com.havos.lubricerp.core.ui.theme.GradientReceiptsStart, com.havos.lubricerp.core.ui.theme.GradientReceiptsEnd)
        ),
        KpiItem(
            "Receipts Collected", 
            "\u20B9${currencyFmt.format(summary.totalReceiptsCollected)}", 
            Icons.Outlined.AccountBalanceWallet, 
            tertiary,
            gradientColors = listOf(com.havos.lubricerp.core.ui.theme.GradientTanksStart, com.havos.lubricerp.core.ui.theme.GradientTanksEnd)
        ),
        KpiItem(
            "Payments Made", 
            "\u20B9${currencyFmt.format(summary.totalPaymentsMade)}", 
            Icons.Outlined.RemoveShoppingCart, 
            error,
            gradientColors = listOf(com.havos.lubricerp.core.ui.theme.GradientPayablesStart, com.havos.lubricerp.core.ui.theme.GradientPayablesEnd)
        ),
        KpiItem(
            "Out. Receivables", 
            "\u20B9${currencyFmt.format(summary.totalOutstandingReceivables)}", 
            Icons.Outlined.HourglassEmpty, 
            error,
            gradientColors = listOf(com.havos.lubricerp.core.ui.theme.GradientPayablesStart, com.havos.lubricerp.core.ui.theme.GradientPayablesEnd)
        ),
        KpiItem(
            "Out. Payables", 
            "\u20B9${currencyFmt.format(summary.totalOutstandingPayables)}", 
            Icons.Outlined.Receipt, 
            onSurfaceVariant,
            gradientColors = listOf(com.havos.lubricerp.core.ui.theme.GradientReceivablesStart, com.havos.lubricerp.core.ui.theme.GradientReceivablesEnd)
        ),
        KpiItem(
            "Net Cash Flow", 
            "\u20B9${currencyFmt.format(summary.netCashFlow)}", 
            Icons.Outlined.CurrencyRupee, 
            primary,
            gradientColors = listOf(com.havos.lubricerp.core.ui.theme.GradientSalesStart, com.havos.lubricerp.core.ui.theme.GradientSalesEnd)
        )
    )
}
