package com.havos.lubricerp.feature_reports.presentation.products

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.havos.lubricerp.core.ui.components.ErrorPlaceholder
import com.havos.lubricerp.feature_reports.domain.model.CostBreakdownItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.round

// Costs from the API are per standard 210L blending batch; used to derive per-liter figures.
private const val BATCH_SIZE_LITERS = 210.0

// GST applied on top of the landed per-liter cost (cost + package + transport + margin).
private const val GST_RATE = 0.18

private fun Double.roundTo2(): Double = round(this * 100) / 100

// NOTE: Temporarily unused — RM/Unit, Per Ltr and computed price columns are hidden
// in favor of the API totalCost. Kept for when the detailed cost columns return.
// Per-liter raw material cost derived from the batch-level materialCost.
private fun perLiterCost(item: CostBreakdownItem): Double =
    (item.materialCost / BATCH_SIZE_LITERS).roundTo2()

// Final selling price per liter: per-liter RM cost plus package, transport and margin, with GST.
private fun finalPrice(item: CostBreakdownItem): Double =
    (perLiterCost(item) + item.packageCost + item.transportCost + item.margin) * (1 + GST_RATE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    state: ProductsUiState,
    onBackClick: () -> Unit,
    onAction: (ProductsAction) -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Cost Breakdown")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Products") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onAction(ProductsAction.CreateClicked) },containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White) {
                Icon(Icons.Default.Add, contentDescription = "Create Cost Breakdown")
            }
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { onAction(ProductsAction.Refresh) },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }

                when {
                    state.isLoading -> LoadingContent()
                    state.errorMessage != null && state.items.isEmpty() -> {
                        ErrorPlaceholder(
                            message = state.errorMessage,
                            onRetry = { onAction(ProductsAction.Refresh) }
                        )
                    }
                    else -> {
                        when (selectedTabIndex) {
                            0 -> CostBreakdownList(
                                items = state.items,
                                sortColumn = state.sortColumn,
                                sortAscending = state.sortAscending,
                                onSortChanged = { onAction(ProductsAction.SortChanged(it)) },
                                onMenuAction = { item, action -> onAction(ProductsAction.MenuClicked(item, action)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun CostBreakdownList(
    items: List<CostBreakdownItem>,
    sortColumn: CostBreakdownSortColumn,
    sortAscending: Boolean,
    onSortChanged: (CostBreakdownSortColumn) -> Unit,
    onMenuAction: (CostBreakdownItem, CostBreakdownMenuAction) -> Unit
) {
    if (items.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.height(48.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "No cost breakdown sheets found",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        SortableHeaderRow(
            sortColumn = sortColumn,
            sortAscending = sortAscending,
            onSortChanged = onSortChanged
        )

        items.forEachIndexed { index, item ->
            CostBreakdownRow(
                item = item,
                isEven = index % 2 == 0,
                onMenuAction = { action -> onMenuAction(item, action) }
            )
        }
    }
}

@Composable
private fun SortableHeaderRow(
    sortColumn: CostBreakdownSortColumn,
    sortAscending: Boolean,
    onSortChanged: (CostBreakdownSortColumn) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 8.dp, vertical = 10.dp)
    ) {
        SortableLabel("SKU", CostBreakdownSortColumn.SKU, sortColumn, sortAscending, onSortChanged, Modifier.weight(1.6f))
        SortableLabel("Grade", CostBreakdownSortColumn.PRODUCT_GRADE, sortColumn, sortAscending, onSortChanged, Modifier.weight(1f))
        SortableLabel("Family", CostBreakdownSortColumn.PRODUCT_FAMILY, sortColumn, sortAscending, onSortChanged, Modifier.weight(1f))
        SortableLabel("From", CostBreakdownSortColumn.EFFECTIVE_FROM, sortColumn, sortAscending, onSortChanged, Modifier.weight(0.9f))
        SortableLabel("To", CostBreakdownSortColumn.EFFECTIVE_TO, sortColumn, sortAscending, onSortChanged, Modifier.weight(0.9f))
        SortableLabel("Total", CostBreakdownSortColumn.TOTAL_COST, sortColumn, sortAscending, onSortChanged, Modifier.weight(1f))
    }
}

@Composable
private fun HeaderLabel(label: String, modifier: Modifier = Modifier) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        textAlign = TextAlign.End,
        modifier = modifier
    )
}

@Composable
private fun SortableLabel(
    label: String,
    column: CostBreakdownSortColumn,
    currentSort: CostBreakdownSortColumn,
    sortAscending: Boolean,
    onSortChanged: (CostBreakdownSortColumn) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.clickable { onSortChanged(column) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (currentSort == column) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
        if (currentSort == column) {
            Icon(
                imageVector = if (sortAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                contentDescription = if (sortAscending) "Sorted ascending" else "Sorted descending",
                modifier = Modifier.height(14.dp).width(14.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CostBreakdownRow(
    item: CostBreakdownItem,
    isEven: Boolean,
    onMenuAction: (CostBreakdownMenuAction) -> Unit
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    Card(
        onClick = { showBottomSheet = true },
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isEven) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1.6f)) {
                Text(item.sku, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                StatusChip(isActive = isCurrentlyActive(item.effectiveFrom, item.effectiveTo))
            }
            Text(item.productGrade, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
            Text(item.productFamily, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
            Text(formatDateShort(item.effectiveFrom), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(0.9f))
            Text(formatDateShort(item.effectiveTo), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(0.9f))
            Text(
                "₹%,.2f".format(item.totalCost),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Text(
                text = item.sku,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            BottomSheetAction("View Details", Icons.Default.Visibility) {
                showBottomSheet = false
                onMenuAction(CostBreakdownMenuAction.VIEW_DETAILS)
            }
            BottomSheetAction("Edit", Icons.Default.Edit) {
                showBottomSheet = false
                onMenuAction(CostBreakdownMenuAction.EDIT)
            }
            BottomSheetAction("Convert to Proforma Invoice", Icons.Default.SwapHoriz) {
                showBottomSheet = false
                onMenuAction(CostBreakdownMenuAction.CONVERT_TO_PI)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            BottomSheetAction(
                "Delete",
                Icons.Default.Delete,
                tint = MaterialTheme.colorScheme.error
            ) {
                showBottomSheet = false
                onMenuAction(CostBreakdownMenuAction.DELETE)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun BottomSheetAction(
    text: String,
    icon: ImageVector,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = tint,
                modifier = Modifier.height(22.dp).width(22.dp)
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = tint
            )
        }
    }
}

@Composable
private fun StatusChip(isActive: Boolean) {
    val bgColor = if (isActive) Color(0xFF1B5E20) else MaterialTheme.colorScheme.surfaceContainerHighest
    val textColor = if (isActive) Color(0xFFC8E6C9) else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = bgColor
    ) {
        Text(
            text = if (isActive) "Active" else "Inactive",
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

// Active when today falls between effectiveFrom and effectiveTo (open-ended when
// effectiveTo is null). ISO date strings compare correctly lexicographically.
private fun isCurrentlyActive(effectiveFrom: String, effectiveTo: String?): Boolean {
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    val from = effectiveFrom.substringBefore("T")
    val to = effectiveTo?.substringBefore("T")
    return (from.isBlank() || today >= from) && (to.isNullOrBlank() || today <= to)
}

private fun formatDateShort(date: String?): String {
    if (date.isNullOrBlank()) return "-"
    return date.substringBefore("T")
}
