package com.havos.lubricerp.feature_reports.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.havos.lubricerp.core.ui.components.ErrorPlaceholder
import com.havos.lubricerp.core.ui.components.HomeTabShimmer
import com.havos.lubricerp.core.ui.components.OfflinePlaceholder
import com.havos.lubricerp.feature_reports.domain.model.RecentInvoice
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTabScreen(
    viewModel: HomeTabViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier.fillMaxSize()
    ) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
    ) {
        if (state.isProfileLoading || state.isDashboardLoading) {
            item {
                HomeTabShimmer(modifier = Modifier.fillMaxWidth())
            }
        } else {
            if (state.greetingName.isNotBlank()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        tonalElevation = 1.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Hello, ${state.greetingName}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Welcome to Goal Lubricants ERP",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            if (state.isOffline) {
                item {
                    OfflinePlaceholder(onRetry = { viewModel.refresh() })
                }
            } else if (state.dashboardError != null) {
                item {
                    ErrorPlaceholder(
                        message = state.dashboardError!!,
                        onRetry = { viewModel.refresh() }
                    )
                }
            } else {
                item {
                    Text(
                        text = "Today's Sales",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DashboardKpiCard(
                            modifier = Modifier.weight(1f),
                            label = "Amount",
                            value = formatCurrency(state.todaySalesAmount)
                        )
                        DashboardKpiCard(
                            modifier = Modifier.weight(1f),
                            label = "Orders",
                            value = state.todaySalesCount.toString()
                        )
                    }
                }

                item {
                    Text(
                        text = "Monthly Sales",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DashboardKpiCard(
                            modifier = Modifier.weight(1f),
                            label = "Amount",
                            value = formatCurrency(state.monthlySalesAmount)
                        )
                        DashboardKpiCard(
                            modifier = Modifier.weight(1f),
                            label = "Orders",
                            value = state.monthlySalesCount.toString()
                        )
                    }
                }

                item {
                    Text(
                        text = "Financials",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DashboardKpiCard(
                            modifier = Modifier.weight(1f),
                            label = "Receivables",
                            value = formatCurrency(state.outstandingReceivables),
                            valueColor = MaterialTheme.colorScheme.primary
                        )
                        DashboardKpiCard(
                            modifier = Modifier.weight(1f),
                            label = "Payables",
                            value = formatCurrency(state.pendingPayables),
                            valueColor = MaterialTheme.colorScheme.error
                        )
                    }
                }

                if (state.lowStockAlertCount > 0) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = "⚠ ${state.lowStockAlertCount} low stock alert(s)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }

                if (state.recentInvoices.isNotEmpty()) {
                    item {
                        Text(
                            text = "Recent Invoices",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    items(state.recentInvoices, key = { it.id }) { invoice ->
                        RecentInvoiceRow(invoice = invoice)
                    }
                }
            }
        }
    }
    }
}

@Composable
private fun DashboardKpiCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun RecentInvoiceRow(invoice: RecentInvoice) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = invoice.invoiceNumber,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = invoice.customerName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatCurrency(invoice.amount),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = invoice.paymentStatus,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (invoice.paymentStatus == "Pending")
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

private val indiaCurrencyFmt: NumberFormat =
    NumberFormat.getNumberInstance(Locale("en", "IN")).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }

private fun formatCurrency(amount: Double): String = "₹${indiaCurrencyFmt.format(amount)}"
