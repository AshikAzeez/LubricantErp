package com.havos.lubricerp.feature_reports.presentation.products

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.havos.lubricerp.core.ui.components.ErrorPlaceholder
import com.havos.lubricerp.feature_reports.domain.model.CostBreakdownItem

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
            FloatingActionButton(onClick = { onAction(ProductsAction.CreateClicked) }) {
                Icon(Icons.Default.Add, contentDescription = "Create Cost Breakdown")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp)
        ) {
            Text(
                text = "#",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(32.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
            SortableLabel("SKU", CostBreakdownSortColumn.SKU, sortColumn, sortAscending, onSortChanged, Modifier.weight(1.5f))
            SortableLabel("Grade", CostBreakdownSortColumn.PRODUCT_GRADE, sortColumn, sortAscending, onSortChanged, Modifier.weight(1.2f))
            SortableLabel("Family", CostBreakdownSortColumn.PRODUCT_FAMILY, sortColumn, sortAscending, onSortChanged, Modifier.weight(1.2f))
            SortableLabel("From", CostBreakdownSortColumn.EFFECTIVE_FROM, sortColumn, sortAscending, onSortChanged, Modifier.weight(1f))
            SortableLabel("To", CostBreakdownSortColumn.EFFECTIVE_TO, sortColumn, sortAscending, onSortChanged, Modifier.weight(1f))
            SortableLabel("Total", CostBreakdownSortColumn.TOTAL_COST, sortColumn, sortAscending, onSortChanged, Modifier.weight(1f))
            Spacer(Modifier.width(36.dp))
        }
    }
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
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isEven) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${item.id}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.width(32.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(modifier = Modifier.weight(1.5f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.sku, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                    val itemStatus = item.status
                    if (!itemStatus.isNullOrBlank()) {
                        Spacer(Modifier.width(6.dp))
                        StatusBadge(itemStatus)
                    }
                }
            }
            Text(item.productGrade, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1.2f))
            Text(item.productFamily, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1.2f))
            Text(formatDateShort(item.effectiveFrom), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
            Text(formatDateShort(item.effectiveTo), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
            Text(
                "₹%,.2f".format(item.totalCost),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
            IconButton(onClick = { showBottomSheet = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More", modifier = Modifier.height(20.dp))
            }
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
private fun StatusBadge(status: String) {
    val (bgColor, textColor) = when (status.lowercase().trim()) {
        "active" -> Color(0xFF1B5E20) to Color(0xFFC8E6C9)
        "draft" -> Color(0xFFE65100) to Color(0xFFFFE0B2)
        "expired" -> Color(0xFF616161) to Color(0xFFE0E0E0)
        "inactive" -> Color(0xFFB71C1C) to Color(0xFFFFCDD2)
        else -> MaterialTheme.colorScheme.surfaceContainerHighest to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = bgColor
    ) {
        Text(
            text = status.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

private fun formatDateShort(date: String?): String {
    if (date.isNullOrBlank()) return "-"
    return date.substringBefore("T")
}
