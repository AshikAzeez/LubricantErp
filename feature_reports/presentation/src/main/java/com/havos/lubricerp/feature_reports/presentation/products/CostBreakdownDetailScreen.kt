package com.havos.lubricerp.feature_reports.presentation.products

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.havos.lubricerp.core.ui.components.ErrorPlaceholder
import com.havos.lubricerp.feature_reports.domain.model.CostBreakdownDetail
import com.havos.lubricerp.feature_reports.domain.model.RawMaterialLine
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CostBreakdownDetailScreen(
    state: CostBreakdownDetailUiState,
    onBackClick: () -> Unit,
    onAction: (CostBreakdownDetailAction) -> Unit
) {
    var toolbarMenuExpanded by remember { mutableStateOf(false) }

    if (state.showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { onAction(CostBreakdownDetailAction.DismissDelete) },
            title = { Text("Confirm Delete") },
            text = { Text("Are you sure you want to delete this cost breakdown sheet?") },
            confirmButton = {
                TextButton(onClick = { onAction(CostBreakdownDetailAction.ConfirmDelete) }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { onAction(CostBreakdownDetailAction.DismissDelete) }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cost Breakdown Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { toolbarMenuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Actions")
                        }
                        DropdownMenu(
                            expanded = toolbarMenuExpanded,
                            onDismissRequest = { toolbarMenuExpanded = false }
                        ) {
                            // TODO: Re-enable once "Convert to Proforma Invoice" is implemented.
                            // DropdownMenuItem(
                            //     text = { Text("Convert to Proforma Invoice") },
                            //     onClick = { toolbarMenuExpanded = false; onAction(CostBreakdownDetailAction.ConvertToPiClicked) },
                            //     leadingIcon = { Icon(Icons.Default.SwapHoriz, null) }
                            // )
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                onClick = { toolbarMenuExpanded = false; onAction(CostBreakdownDetailAction.EditClicked) },
                                leadingIcon = { Icon(Icons.Default.Edit, null) }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                onClick = { toolbarMenuExpanded = false; onAction(CostBreakdownDetailAction.DeleteClicked) },
                                leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.errorMessage != null && state.detail == null -> ErrorPlaceholder(
                message = state.errorMessage,
                onRetry = { onAction(CostBreakdownDetailAction.Refresh) },
                modifier = Modifier.padding(innerPadding)
            )
            state.detail != null -> CostBreakdownDetailContent(
                detail = state.detail,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun CostBreakdownDetailContent(
    detail: CostBreakdownDetail,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ProductInfoCard(detail)
        EffectivePeriodCard(detail)
        CostSummaryCard(detail)
        RawMaterialBreakdownCard(detail.lines)
    }
}

@Composable
private fun ProductInfoCard(detail: CostBreakdownDetail) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Product Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            DetailRow("Product SKU", detail.sku)
            DetailRow("SKU Code", detail.skuCode)
            DetailRow("Product Grade", detail.productGrade)
            DetailRow("Product Family", detail.productFamily)
            val remarksText = detail.remarks
            if (!remarksText.isNullOrBlank()) {
                DetailRow("Remarks", remarksText)
            }
        }
    }
}

@Composable
private fun EffectivePeriodCard(detail: CostBreakdownDetail) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Effective Period", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            DetailRow("Effective From", formatDateShort(detail.effectiveFrom))
            DetailRow("Effective To", if (detail.effectiveTo.isNullOrBlank()) "Ongoing" else formatDateShort(detail.effectiveTo))
        }
    }
}

@Composable
private fun CostSummaryCard(detail: CostBreakdownDetail) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Cost Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            DetailRow("Material Cost", "₹%,.2f".format(detail.materialCost))
            DetailRow("Package Cost", "₹%,.2f".format(detail.packageCost))
            DetailRow("Transport Cost", "₹%,.2f".format(detail.transportCost))
            DetailRow("Margin", "${detail.margin}%")
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Cost", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(
                    "₹%,.2f".format(detail.totalCost),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun RawMaterialBreakdownCard(lines: List<RawMaterialLine>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Raw Material Breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            // Header
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
                Text("Material", modifier = Modifier.weight(2f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Text("Code", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Text("Qty", modifier = Modifier.weight(0.8f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                Text("Rate", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                Text("Amount", modifier = Modifier.weight(1.2f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
            }
            HorizontalDivider()

            lines.forEach { line ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(line.rawMaterialName, modifier = Modifier.weight(2f), style = MaterialTheme.typography.bodySmall)
                    Text(line.rawMaterialCode, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                    Text("%.4f".format(line.quantity), modifier = Modifier.weight(0.8f), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End)
                    Text("₹%.2f".format(line.rate), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End)
                    Text("₹%,.2f".format(line.amount), modifier = Modifier.weight(1.2f), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

            val totalQty = lines.sumOf { it.quantity }
            val totalAmount = lines.sumOf { it.amount }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Qty: ${"%.4f".format(totalQty)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text("Total: ₹%,.2f".format(totalAmount), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(8.dp))
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

// Formats an ISO date string (yyyy-MM-dd'T'...) as dd-MMM-yyyy, e.g. "28-Apr-2026".
private fun formatDateShort(date: String?): String {
    if (date.isNullOrBlank()) return "-"
    val raw = date.substringBefore("T")
    return runCatching {
        val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(raw)
        SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH).format(parsed!!)
    }.getOrDefault(raw)
}
