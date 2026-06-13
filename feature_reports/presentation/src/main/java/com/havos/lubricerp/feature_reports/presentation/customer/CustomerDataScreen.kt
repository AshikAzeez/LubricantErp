package com.havos.lubricerp.feature_reports.presentation.customer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.SearchOff
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.havos.lubricerp.core.ui.components.ErrorPlaceholder
import com.havos.lubricerp.core.ui.components.OfflinePlaceholder
import com.havos.lubricerp.feature_reports.domain.model.Customer
import com.havos.lubricerp.feature_reports.domain.model.CustomerLedgerEntry
import com.havos.lubricerp.feature_reports.domain.model.CustomerMobileSummary
import com.havos.lubricerp.feature_reports.domain.model.RecordPaymentResponse
import com.havos.lubricerp.feature_reports.presentation.R
import com.havos.lubricerp.feature_reports.presentation.reports.DatePickerButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CustomerDataScreen(
    state: CustomerDataUiState,
    onBackClick: () -> Unit,
    onAction: (CustomerDataAction) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != androidx.compose.material3.SheetValue.Hidden }
    )
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
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (state.searchQuery.isBlank()) Icons.Filled.Person else Icons.Filled.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (state.customers.isEmpty()) "No customers found."
                                else "No results for \"${state.searchQuery}\".",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
                            CustomerListRow(
                                customer = customer,
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
            sheetState = sheetState,
            dragHandle = null
        ) {
            CustomerDetailSheet(
                customer = customer,
                state = state,
                onAction = onAction,
                coroutineScope = scope
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
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val borderColor = if (isFocused) MaterialTheme.colorScheme.primary else outline.copy(alpha = 0.5f)
    val borderWidth = if (isFocused) 1.5.dp else 1.dp

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        modifier = modifier
            .focusRequester(focusRequester)
            .onFocusChanged { isFocused = it.isFocused },
        decorationBox = { inner ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(borderWidth, borderColor, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(
                            text = "Search by name, code or phone…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
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
                            .clip(CircleShape)
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
    onSelect: () -> Unit,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit
) {
    val phoneValid = isValidPhone(customer.phone)
    val addressFirst = remember(customer.address) {
        customer.address.split(",").firstOrNull()?.trim()?.takeIf { it.isNotBlank() }
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onSelect),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(avatarColor(customer.name)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = customer.name.firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
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
            if (customer.phone.isNotBlank()) {
                IconButton(
                    onClick = { if (phoneValid) onCall() },
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), CircleShape)
                        .alpha(if (phoneValid) 1f else 0.35f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Call,
                        contentDescription = "Call",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                WhatsAppIconButton(enabled = phoneValid, onClick = { if (phoneValid) onWhatsApp() })
            }
        }
    }
}

@Composable
private fun WhatsAppIconButton(enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color(0xFF25D366).copy(alpha = 0.1f))
            .alpha(if (enabled) 1f else 0.35f)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_whatsapp),
            contentDescription = "WhatsApp",
            tint = Color.Unspecified,
            modifier = Modifier.size(20.dp)
        )
    }
}

private val avatarColors = listOf(
    0xFFE91E63, 0xFF9C27B0, 0xFF673AB7, 0xFF3F51B5, 0xFF2196F3,
    0xFF009688, 0xFF4CAF50, 0xFFFF9800, 0xFFFF5722, 0xFF795548
)

private fun avatarColor(name: String): Color {
    val index = name.hashCode().let { if (it == Int.MIN_VALUE) 0 else kotlin.math.abs(it) } % avatarColors.size
    return Color(avatarColors[index])
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
    onAction: (CustomerDataAction) -> Unit,
    coroutineScope: kotlinx.coroutines.CoroutineScope
) {
    val currencyFmt = remember {
        java.text.NumberFormat.getNumberInstance(java.util.Locale.forLanguageTag("en-IN")).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
    }
    val outline = MaterialTheme.colorScheme.outlineVariant

    var sortField by remember { mutableStateOf(LedgerSortField.NONE) }
    var sortAsc by remember { mutableStateOf(true) }
    val sortedEntries = remember(state.ledgerEntries, sortField, sortAsc) {
        val base = state.ledgerEntries
        when (sortField) {
            LedgerSortField.NONE -> base
            LedgerSortField.DATE -> if (sortAsc) base.sortedBy { it.date }
                                    else base.sortedByDescending { it.date }
            LedgerSortField.DEBIT -> if (sortAsc) base.sortedBy { it.debit }
                                     else base.sortedByDescending { it.debit }
            LedgerSortField.CREDIT -> if (sortAsc) base.sortedBy { it.credit }
                                      else base.sortedByDescending { it.credit }
            LedgerSortField.BALANCE -> if (sortAsc) base.sortedBy { it.runningBalance }
                                       else base.sortedByDescending { it.runningBalance }
        }
    }

    val sheetListState = rememberLazyListState()

    LazyColumn(
        state = sheetListState,
        modifier = Modifier.fillMaxHeight(0.95f),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = { onAction(CustomerDataAction.CustomerDismissed) }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

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
                val infiniteTransition = rememberInfiniteTransition(label = "paymentPulse")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.07f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulseScale"
                )
                Box(
                    modifier = Modifier
                        .graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { onAction(CustomerDataAction.ShowPaymentSheet) }
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+ Payment",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            when {
                state.isMobileSummaryLoading -> {
                    FinancialSummaryShimmer()
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

            val today = remember {
                val fmt = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                fmt.format(java.util.Date())
            }
            val presets = remember {
                val cal = java.util.Calendar.getInstance()
                val fmt = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                listOf(
                    "Today" to (today to today),
                    "7d" to (
                        fmt.format(java.util.Date(cal.timeInMillis - 7 * 24 * 60 * 60 * 1000L)) to today
                    ),
                    "30d" to (
                        fmt.format(java.util.Date(cal.timeInMillis - 30 * 24 * 60 * 60 * 1000L)) to today
                    )
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                presets.forEach { (label, dates) ->
                    val isActive = state.ledgerFromDate == dates.first && state.ledgerToDate == dates.second
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isActive) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceContainer
                            )
                            .border(
                                width = 1.dp,
                                color = if (isActive) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                onAction(CustomerDataAction.LedgerDatePreset(label, dates.first, dates.second))
                            }
                            .padding(horizontal = 12.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            color = if (isActive) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            val fromMillis = remember(state.ledgerFromDate) {
                runCatching {
                    val fmt = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                    fmt.parse(state.ledgerFromDate)?.time
                }.getOrNull()
            }
            val todayMillis = remember {
                java.util.Calendar.getInstance().apply {
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
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.Receipt,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No ledger entries for the selected period.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            val totalDebit = sortedEntries.sumOf { it.debit }
            val totalCredit = sortedEntries.sumOf { it.credit }
            val closing = sortedEntries.lastOrNull()?.runningBalance ?: 0.0

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
                LedgerHeaderRow(
                    sortField = sortField,
                    sortAsc = sortAsc,
                    onSort = { field ->
                        if (sortField == field) sortAsc = !sortAsc
                        else { sortField = field; sortAsc = true }
                    }
                )
            }

            items(
                sortedEntries,
                key = { "${it.date}_${it.refNumber}" }
            ) { entry ->
                LedgerEntryRow(entry = entry, currencyFmt = currencyFmt)
            }
        }

        item { Spacer(modifier = Modifier.height(40.dp)) }
    }

    if (state.showPaymentSheet) {
        PaymentFormDialog(state = state, onAction = onAction)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentFormDialog(
    state: CustomerDataUiState,
    onAction: (CustomerDataAction) -> Unit
) {
    val amountFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        amountFocusRequester.requestFocus()
    }

    LaunchedEffect(state.paymentResult) {
        if (state.paymentResult != null) {
            delay(2000)
            onAction(CustomerDataAction.DismissPaymentSheet)
        }
    }

    Dialog(
        onDismissRequest = { onAction(CustomerDataAction.DismissPaymentSheet) },
        properties = DialogProperties(dismissOnClickOutside = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (state.paymentResult != null) "Payment Successful"
                               else "Record Payment",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { onAction(CustomerDataAction.DismissPaymentSheet) }) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (state.paymentResult != null) {
                    PaymentSuccessContent(state.paymentResult!!)
                } else {
                    PaymentFormContent(state = state, onAction = onAction, focusRequester = amountFocusRequester)
                }
            }
        }
    }
}

@Composable
private fun PaymentSuccessContent(result: RecordPaymentResponse) {
    val currencyFmt = remember {
        java.text.NumberFormat.getNumberInstance(java.util.Locale.forLanguageTag("en-IN")).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }

        Text(
            text = "Payment Recorded",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        HorizontalDivider()

        DetailRow("Receipt", result.receiptNumber)
        DetailRow("Amount Paid", "₹${currencyFmt.format(result.amountPaid)}")
        DetailRow("New Balance", "₹${currencyFmt.format(result.newBalance)}")

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Auto-dismissing…",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun PaymentFormContent(
    state: CustomerDataUiState,
    onAction: (CustomerDataAction) -> Unit,
    focusRequester: FocusRequester
) {
    val paymentModes = listOf("Cash", "Cheque", "UPI", "Bank Transfer", "Card")
    val invoices = remember(state.ledgerEntries) {
        state.ledgerEntries
            .filter { it.debit > 0 && it.invoiceId > 0 }
            .distinctBy { it.invoiceId }
    }
    val outstandingAmount = state.mobileSummary?.outstandingAmount ?: 0.0

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Invoice selector
        val invoiceError = state.paymentFormFieldErrors[CustomerDataUiState.FIELD_INVOICE]
        if (invoices.isNotEmpty()) {
            Text(
                text = "Select Invoice",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            val selectedEntry = invoices.find { it.invoiceId == state.paymentFormInvoiceId }
            if (selectedEntry != null) {
                Text(
                    text = "#${selectedEntry.invoiceId} · ${selectedEntry.refNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                invoices.take(5).forEach { entry ->
                    val selected = state.paymentFormInvoiceId == entry.invoiceId
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (selected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceContainer
                            )
                            .border(
                                width = if (selected) 1.dp else 0.dp,
                                color = if (invoiceError != null) MaterialTheme.colorScheme.error
                                else if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable {
                                onAction(CustomerDataAction.PaymentFormInvoiceChanged(entry.invoiceId))
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "#${entry.invoiceId}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
            if (invoices.size > 5) {
                Text(
                    text = "+${invoices.size - 5} more invoices",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (invoiceError != null) {
            Text(
                text = invoiceError,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // Amount
        val amountError = state.paymentFormFieldErrors[CustomerDataUiState.FIELD_AMOUNT]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Amount (₹)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (outstandingAmount > 0) {
                Text(
                    text = "Outstanding: ₹${
                        java.text.NumberFormat.getNumberInstance(java.util.Locale.forLanguageTag("en-IN")).apply {
                            minimumFractionDigits = 2; maximumFractionDigits = 2
                        }.format(outstandingAmount)
                    }",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        BasicTextField(
            value = state.paymentFormAmount,
            onValueChange = { onAction(CustomerDataAction.PaymentFormAmountChanged(it)) },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = if (amountError != null) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 12.dp, vertical = 10.dp),
            decorationBox = { inner ->
                if (state.paymentFormAmount.isEmpty()) {
                    Text(
                        text = "Enter amount",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                inner()
            }
        )
        if (amountError != null) {
            Text(
                text = amountError,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        // Payment mode
        Text(
            text = "Payment Mode",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        val modeIcon: @Composable (String) -> androidx.compose.ui.graphics.vector.ImageVector = { m ->
            when (m) {
                "Cash" -> Icons.Filled.AttachMoney
                "Cheque" -> Icons.Filled.Receipt
                "UPI" -> Icons.Filled.PhoneAndroid
                "Bank Transfer" -> Icons.Filled.AccountBalance
                "Card" -> Icons.Filled.CreditCard
                else -> Icons.Filled.AttachMoney
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            paymentModes.forEach { mode ->
                val selected = state.paymentFormMode == mode
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceContainer
                        )
                        .clickable { onAction(CustomerDataAction.PaymentFormModeChanged(mode)) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = modeIcon(mode),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (selected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = mode,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Date
        val dateError = state.paymentFormFieldErrors[CustomerDataUiState.FIELD_DATE]
        Text(
            text = "Payment Date",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        val todayMillis = remember {
            java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, 23)
                set(java.util.Calendar.MINUTE, 59)
                set(java.util.Calendar.SECOND, 59)
                set(java.util.Calendar.MILLISECOND, 999)
            }.timeInMillis
        }
        DatePickerButton(
            label = "",
            value = state.paymentFormDate,
            onDateSelected = { onAction(CustomerDataAction.PaymentFormDateChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            maxDateMillis = todayMillis,
            borderColor = if (dateError != null) MaterialTheme.colorScheme.error else null
        )
        Box(modifier = Modifier.height(16.dp)) {
            if (dateError != null) {
                Text(
                    text = dateError,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Reference
        val referenceError = state.paymentFormFieldErrors[CustomerDataUiState.FIELD_REFERENCE]
        Text(
            text = "Reference (optional)",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        BasicTextField(
            value = state.paymentFormReference,
            onValueChange = { onAction(CustomerDataAction.PaymentFormReferenceChanged(it)) },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = if (referenceError != null) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 12.dp, vertical = 10.dp),
            decorationBox = { inner ->
                if (state.paymentFormReference.isEmpty()) {
                    Text(
                        text = "Cheque/UTR number",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                inner()
            }
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "${state.paymentFormReference.length}/30",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Remarks
        val remarksError = state.paymentFormFieldErrors[CustomerDataUiState.FIELD_REMARKS]
        Text(
            text = "Remarks (optional)",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        BasicTextField(
            value = state.paymentFormRemarks,
            onValueChange = { onAction(CustomerDataAction.PaymentFormRemarksChanged(it)) },
            singleLine = false,
            maxLines = 3,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = if (remarksError != null) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 12.dp, vertical = 10.dp),
            decorationBox = { inner ->
                if (state.paymentFormRemarks.isEmpty()) {
                    Text(
                        text = "Any notes about this payment",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                inner()
            }
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "${state.paymentFormRemarks.length}/100",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Error message
        if (state.paymentError != null) {
            Text(
                text = state.paymentError,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                    .clickable { onAction(CustomerDataAction.DismissPaymentSheet) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(enabled = !state.isRecordingPayment) {
                        onAction(CustomerDataAction.SubmitPayment)
                    }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                if (state.isRecordingPayment) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = "Record Payment",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun FinancialSummaryShimmer() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(2) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                )
            }
        }
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
        modifier = modifier.border(
            width = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
            shape = RoundedCornerShape(8.dp)
        ),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
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
            CustomerInfoRow(
                label = "Address",
                value = customer.address,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun CustomerInfoRow(label: String, value: String, maxLines: Int = 1) {
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
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun LedgerSummaryChip(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

internal enum class LedgerSortField { NONE, DATE, DEBIT, CREDIT, BALANCE }

@Composable
private fun LedgerHeaderRow(
    sortField: LedgerSortField,
    sortAsc: Boolean,
    onSort: (LedgerSortField) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LedgerSortableColumn(
            text = "Date / Ref",
            field = LedgerSortField.DATE,
            active = sortField == LedgerSortField.DATE,
            asc = sortAsc,
            onClick = { onSort(LedgerSortField.DATE) },
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Start
        )
        LedgerSortableColumn(
            text = "Debit",
            field = LedgerSortField.DEBIT,
            active = sortField == LedgerSortField.DEBIT,
            asc = sortAsc,
            onClick = { onSort(LedgerSortField.DEBIT) },
            modifier = Modifier.width(76.dp),
            textAlign = TextAlign.End
        )
        LedgerSortableColumn(
            text = "Credit",
            field = LedgerSortField.CREDIT,
            active = sortField == LedgerSortField.CREDIT,
            asc = sortAsc,
            onClick = { onSort(LedgerSortField.CREDIT) },
            modifier = Modifier.width(76.dp),
            textAlign = TextAlign.End
        )
        LedgerSortableColumn(
            text = "Balance",
            field = LedgerSortField.BALANCE,
            active = sortField == LedgerSortField.BALANCE,
            asc = sortAsc,
            onClick = { onSort(LedgerSortField.BALANCE) },
            modifier = Modifier.width(76.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun LedgerSortableColumn(
    text: String,
    field: LedgerSortField,
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
private fun LedgerEntryRow(
    entry: CustomerLedgerEntry,
    currencyFmt: java.text.NumberFormat
) {
    val isCredit = entry.credit > 0
    val bgColor = if (isCredit) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                  else if (entry.debit > 0) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                  else Color.Transparent
    Column(modifier = Modifier.fillMaxWidth().background(bgColor)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.date,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${entry.type} · ${entry.refNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = if (entry.debit > 0) "₹${currencyFmt.format(entry.debit)}" else "–",
                style = MaterialTheme.typography.bodyMedium,
                color = if (entry.debit > 0) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
                modifier = Modifier.width(70.dp)
            )
            Text(
                text = if (entry.credit > 0) "₹${currencyFmt.format(entry.credit)}" else "–",
                style = MaterialTheme.typography.bodyMedium,
                color = if (entry.credit > 0) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
                modifier = Modifier.width(70.dp)
            )
            Text(
                text = "₹${currencyFmt.format(entry.runningBalance)}",
                style = MaterialTheme.typography.bodyMedium,
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
