package com.havos.lubricerp.feature_reports.presentation.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.havos.lubricerp.core.ui.components.ErrorPlaceholder
import com.havos.lubricerp.core.ui.components.HomeTabShimmer
import com.havos.lubricerp.core.ui.components.OfflinePlaceholder
import com.havos.lubricerp.feature_reports.domain.model.AgingBucket
import com.havos.lubricerp.feature_reports.domain.model.BankAccountBalance
import com.havos.lubricerp.feature_reports.domain.model.CashPosition
import com.havos.lubricerp.feature_reports.domain.model.PurchaseSummary
import com.havos.lubricerp.feature_reports.domain.model.RecentInvoice
import com.havos.lubricerp.feature_reports.domain.model.ReceivablesAging
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.ui.platform.LocalLocale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTabScreen(
    viewModel: HomeTabViewModel,
    onNavigateToReport: (String) -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onIntent: (HomeTabIntent) -> Unit = { viewModel.onIntent(it) }

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
                        GreetingBannerCard(
                            name = state.greetingName,
                            roles = state.userRoles
                        )
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
                        val primaryColor = MaterialTheme.colorScheme.primary
                        val errorColor = MaterialTheme.colorScheme.error
                        val kpiItems = buildList {
                            add(
                                KpiItem(
                                    title = "Sales This Month",
                                    value = formatCurrency(state.monthlySalesAmount),
                                    subtitle = "${state.monthlySalesCount} invoices",
                                    icon = Icons.Filled.Receipt,
                                    iconBgColor = Color(0xFF4CAF50),
                                    gradientColors = listOf(
                                        com.havos.lubricerp.core.ui.theme.GradientReceiptsStart,
                                        com.havos.lubricerp.core.ui.theme.GradientReceiptsEnd
                                    )
                                )
                            )
                            add(
                                KpiItem(
                                    title = "Sales Today",
                                    value = formatCurrency(state.todaySalesAmount),
                                    subtitle = "${state.todaySalesCount} orders",
                                    icon = Icons.Filled.ShoppingCart,
                                    iconBgColor = Color(0xFF2196F3),
                                    gradientColors = listOf(
                                        com.havos.lubricerp.core.ui.theme.GradientSalesStart,
                                        com.havos.lubricerp.core.ui.theme.GradientSalesEnd
                                    )
                                )
                            )
                            if (state.canViewFinancials) {
                                add(
                                    KpiItem(
                                        title = "Outstanding Receivables",
                                        value = formatCurrency(state.outstandingReceivables),
                                        subtitle = "From customers",
                                        icon = Icons.Filled.PeopleAlt,
                                        iconBgColor = Color(0xFFFF9800),
                                        valueColor = primaryColor,
                                        gradientColors = listOf(
                                            com.havos.lubricerp.core.ui.theme.GradientReceivablesStart,
                                            com.havos.lubricerp.core.ui.theme.GradientReceivablesEnd
                                        )
                                    )
                                )
                                add(
                                    KpiItem(
                                        title = "Outstanding Payables",
                                        value = formatCurrency(state.pendingPayables),
                                        subtitle = "To vendors",
                                        icon = Icons.Filled.AttachMoney,
                                        iconBgColor = Color(0xFFF44336),
                                        valueColor = errorColor,
                                        gradientColors = listOf(
                                            com.havos.lubricerp.core.ui.theme.GradientPayablesStart,
                                            com.havos.lubricerp.core.ui.theme.GradientPayablesEnd
                                        )
                                    )
                                )
                            }
                            if (state.lowStockAlertCount > 0) {
                                add(
                                    KpiItem(
                                        title = "Low Stock Alerts",
                                        value = state.lowStockAlertCount.toString(),
                                        subtitle = "Items need reorder",
                                        icon = Icons.Filled.Warning,
                                        iconBgColor = Color(0xFFFF5722),
                                        valueColor = errorColor,
                                        gradientColors = listOf(
                                            com.havos.lubricerp.core.ui.theme.GradientPayablesStart,
                                            com.havos.lubricerp.core.ui.theme.GradientPayablesEnd
                                        )
                                    )
                                )
                            }
                        }
                        KpiToggleSection(items = kpiItems)
                    }

                    if (state.receivablesAging != null) {
                        item {
                            ReceivablesAgingCard(aging = state.receivablesAging!!)
                        }
                    }

                    if (state.cashPosition != null) {
                        item {
                            CashPositionCard(cash = state.cashPosition!!)
                        }
                    }

                    if (state.purchaseSummary != null) {
                        item {
                            PurchaseSummaryCard(summary = state.purchaseSummary!!)
                        }
                    }

                    item {
                        TankUtilizationCard(
                            onClick = { onNavigateToReport("tank_stock_summary") }
                        )
                    }

                    if (state.canViewFinancials) {
                        item {
                            NetProfitDashboardCard(
                                state = state,
                                onIntent = onIntent
                            )
                        }
                    }

                    if (state.topSellingProducts.isNotEmpty()) {
                        item {
                            TopSellingProductsCard(products = state.topSellingProducts)
                        }
                    }

                    if (state.recentInvoices.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Recent Transactions",
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
private fun TopSellingProductsCard(products: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    com.havos.lubricerp.core.ui.theme.GradientSalesStart,
                                    com.havos.lubricerp.core.ui.theme.GradientSalesEnd
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.TrendingUp,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Top Selling Products",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Most popular items by sales count",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                products.forEachIndexed { index, product ->
                    val rank = index + 1
                    val badgeGradient = when (rank) {
                        1 -> Brush.linearGradient(listOf(Color(0xFFFBBF24), Color(0xFFD97706)))
                        2 -> Brush.linearGradient(listOf(Color(0xFF9CA3AF), Color(0xFF4B5563)))
                        3 -> Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFB45309)))
                        else -> Brush.linearGradient(listOf(Color(0xFFE5E7EB), Color(0xFF9CA3AF)))
                    }
                    val badgeTextColor = when (rank) {
                        in 1..3 -> Color.White
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    val popularityProgress = (products.size - index).toFloat() / products.size

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f))
                            .border(
                                0.5.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(badgeGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$rank",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = badgeTextColor
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = product,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (rank == 1) FontWeight.Bold else FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(popularityProgress)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(
                                            if (rank == 1) {
                                                Brush.linearGradient(
                                                    listOf(Color(0xFF3B82F6), Color(0xFF60A5FA))
                                                )
                                            } else {
                                                Brush.linearGradient(
                                                    listOf(
                                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                                    )
                                                )
                                            }
                                        )
                                )
                            }
                        }

                        if (rank == 1) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFFBBF24).copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Best Seller",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD97706)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GreetingBannerCard(name: String, roles: List<String>) {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 12 -> "Good Morning"
        hour < 17 -> "Good Afternoon"
        else      -> "Good Evening"
    }
    val emoji = when {
        hour < 12 -> "☀️"
        hour < 17 -> "🌤"
        else      -> "🌙"
    }
    val dateLabel = SimpleDateFormat("EEEE, d MMMM yyyy", LocalLocale.current.platformLocale)
        .format(Calendar.getInstance().time)
    val initials = name.trim().split(" ")
        .take(2).joinToString("") { it.first().uppercaseChar().toString() }

    val gradientBrush = Brush.linearGradient(
        colors = listOf(Color(0xFF4C1DFF), Color(0xFF7B5CFA), Color(0xFF2D6A4F))
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(gradientBrush)
    ) {
        Box(
            modifier = Modifier
                .size(130.dp)
                .offset(x = 210.dp, y = (-40).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.07f))
        )
        Box(
            modifier = Modifier
                .size(80.dp)
                .offset(x = 270.dp, y = 60.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f))
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.18f))
                    .border(1.5.dp, Color.White.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    fontSize = 18.sp
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "$greeting $emoji",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.80f),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                if (roles.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.18f))
                            .border(0.5.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = roles.joinToString(", "),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 10.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.65f)
                )
            }
        }
    }
}

@Composable
private fun KpiRow(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconBgColor: Color,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = valueColor
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconBgColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconBgColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun TankUtilizationCard(
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Press-down scale: card shrinks slightly on tap for tactile feedback
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "cardScale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "tankCardAnim")

    // Arrow slides right and fades, then snaps back — more deliberate than a linear bounce
    val arrowOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arrowOffset"
    )

    // Subtle shimmer sweep across the gradient background
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerOffset"
    )

    // Icon pulses gently to draw attention
    val iconPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconPulse"
    )

    // Ripple ring around icon — expands and fades
    val rippleRadius by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rippleRadius"
    )
    val rippleAlpha = if (rippleRadius < 0.5f) rippleRadius * 2f else (1f - rippleRadius) * 2f

    val tankColor = com.havos.lubricerp.core.ui.theme.GradientTanksStart
    val tankColorEnd = com.havos.lubricerp.core.ui.theme.GradientTanksEnd

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(cardScale)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        tankColor.copy(alpha = 0.85f),
                        tankColorEnd.copy(alpha = 0.75f),
                        tankColor.copy(alpha = 0.65f)
                    )
                )
            )
            // Shimmer sweep layer on top
            .background(
                Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Color.Transparent,
                        (shimmerOffset * 0.3f + 0.35f).coerceIn(0f, 1f) to Color.White.copy(alpha = 0.10f),
                        (shimmerOffset * 0.3f + 0.50f).coerceIn(0f, 1f) to Color.White.copy(alpha = 0.18f),
                        (shimmerOffset * 0.3f + 0.65f).coerceIn(0f, 1f) to Color.White.copy(alpha = 0.10f),
                        1f to Color.Transparent
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.35f),
                        Color.White.copy(alpha = 0.10f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: text block
            Column(modifier = Modifier.weight(1f)) {
                // "Tap to explore" pill badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(Color.White.copy(alpha = 0.18f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "TAP TO EXPLORE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.90f),
                        fontSize = 9.sp,
                        letterSpacing = 0.8.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tank Utilization",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Full stock breakdown & levels",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.80f)
                )
                Spacer(modifier = Modifier.height(14.dp))

                // "View Report →" pill CTA button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(Color.White.copy(alpha = 0.22f))
                        .border(
                            1.dp,
                            Color.White.copy(alpha = 0.40f),
                            RoundedCornerShape(50.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "View Report",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(14.dp)
                            .offset(x = arrowOffset.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Right: pulsing icon with ripple ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(72.dp)
            ) {
                // Outer ripple ring
                Box(
                    modifier = Modifier
                        .size((40 + rippleRadius * 32).dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = rippleAlpha * 0.12f))
                )
                // Inner solid circle
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.20f))
                        .border(1.5.dp, Color.White.copy(alpha = 0.35f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Inventory,
                        contentDescription = "Tank stock report",
                        tint = Color.White,
                        modifier = Modifier
                            .size(26.dp)
                            .graphicsLayer { scaleX = iconPulse; scaleY = iconPulse }
                    )
                }
            }
        }
    }
}

private data class KpiItem(
    val title: String,
    val value: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconBgColor: Color,
    val valueColor: Color = Color.Unspecified,
    val gradientColors: List<Color>? = null
)

@Composable
private fun KpiToggleSection(
    items: List<KpiItem>
) {
    var isGridView by remember { mutableStateOf(true) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Key Metrics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { isGridView = !isGridView }) {
                Icon(
                    imageVector = if (isGridView) Icons.AutoMirrored.Filled.ViewList
                    else Icons.Filled.GridView,
                    contentDescription = if (isGridView) "Switch to list view"
                    else "Switch to grid view",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        AnimatedContent(
            targetState = isGridView,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith
                        fadeOut(animationSpec = tween(300)) using
                        SizeTransform(clip = false)
            },
            label = "kpiViewToggle"
        ) { grid ->
            if (grid) {
                KpiGridContent(items)
            } else {
                KpiListContent(items)
            }
        }
    }
}

@Composable
private fun KpiGridContent(items: List<KpiItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CompactKpiCard(
                    item = rowItems[0],
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
                if (rowItems.size == 2) {
                    CompactKpiCard(
                        item = rowItems[1],
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                } else {
                    // Invisible spacer to keep the lone card at exactly half width
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun KpiListContent(items: List<KpiItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { item ->
            FullWidthKpiCard(
                item = item,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun CompactKpiCard(
    item: KpiItem,
    modifier: Modifier = Modifier
) {
    val hasGradient = item.gradientColors != null
    val valueColor = if (hasGradient) Color.White
    else if (item.valueColor != Color.Unspecified) item.valueColor
    else MaterialTheme.colorScheme.onSurface
    val labelColor = if (hasGradient) Color.White.copy(alpha = 0.85f)
    else MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (hasGradient) Color.Transparent else MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(16.dp),
        border = if (hasGradient) androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (hasGradient) Modifier.background(
                        Brush.linearGradient(item.gradientColors!!),
                        shape = RoundedCornerShape(16.dp)
                    ) else Modifier
                )
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (hasGradient) Color.White.copy(alpha = 0.2f) else item.iconBgColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = if (hasGradient) Color.White else item.iconBgColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.labelSmall,
                color = labelColor,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                maxLines = 1
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = labelColor,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun FullWidthKpiCard(
    item: KpiItem,
    modifier: Modifier = Modifier
) {
    val hasGradient = item.gradientColors != null
    val valueColor = if (hasGradient) Color.White
    else if (item.valueColor != Color.Unspecified) item.valueColor
    else MaterialTheme.colorScheme.onSurface
    val labelColor = if (hasGradient) Color.White.copy(alpha = 0.85f)
    else MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (hasGradient) Color.Transparent else MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(16.dp),
        border = if (hasGradient) androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (hasGradient) Modifier.background(
                        Brush.linearGradient(item.gradientColors!!),
                        shape = RoundedCornerShape(16.dp)
                    ) else Modifier
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.labelMedium,
                    color = labelColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = valueColor
                )
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (hasGradient) Color.White.copy(alpha = 0.2f) else item.iconBgColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = if (hasGradient) Color.White else item.iconBgColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun RecentInvoiceRow(invoice: RecentInvoice) {
    val statusColor = when (invoice.paymentStatus.lowercase()) {
        "paid" -> Color(0xFF4CAF50)
        "unpaid", "pending" -> MaterialTheme.colorScheme.error
        "partial" -> Color(0xFFFF9800)
        else -> MaterialTheme.colorScheme.primary
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            ,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(statusColor.copy(alpha = 0.12f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = invoice.paymentStatus,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = statusColor
                )
            }
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
            Text(
                text = formatCurrency(invoice.amount),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NetProfitDashboardCard(
    state: HomeTabUiState,
    onIntent: (HomeTabIntent) -> Unit
) {
    var customFrom by remember { mutableStateOf(state.netProfitCustomFrom) }
    var customTo by remember { mutableStateOf(state.netProfitCustomTo) }
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        NetProfitCard(
            state = state,
            onIntent = onIntent,
            customFrom = customFrom,
            customTo = customTo,
            onCustomFromChange = { customFrom = it },
            onCustomToChange = { customTo = it }
        )
        /*Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Net Profit",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                NetProfitPeriod.entries.forEach { period ->
                    FilterChip(
                        selected = state.netProfitPeriod == period,
                        onClick = { onIntent(HomeTabIntent.NetProfitPeriodChanged(period)) },
                        label = {
                            Text(
                                text = period.label,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            AnimatedVisibility(
                visible = state.netProfitPeriod == NetProfitPeriod.CUSTOM,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showFromPicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "From",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = customFrom.ifBlank { "Pick date" },
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = { showToPicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "To",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = customTo.ifBlank { "Pick date" },
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            if (showFromPicker) {
                NetProfitDatePickerDialog(
                    title = "From Date",
                    onDateSelected = { dateStr ->
                        customFrom = dateStr
                        showFromPicker = false
                        if (customTo.isNotBlank()) {
                            onIntent(HomeTabIntent.NetProfitCustomDateChanged(dateStr, customTo))
                            onIntent(HomeTabIntent.NetProfitCustomApply)
                        }
                    },
                    onDismiss = { showFromPicker = false }
                )
            }
            if (showToPicker) {
                NetProfitDatePickerDialog(
                    title = "To Date",
                    onDateSelected = { dateStr ->
                        customTo = dateStr
                        showToPicker = false
                        if (customFrom.isNotBlank()) {
                            onIntent(HomeTabIntent.NetProfitCustomDateChanged(customFrom, dateStr))
                            onIntent(HomeTabIntent.NetProfitCustomApply)
                        }
                    },
                    onDismiss = { showToPicker = false }
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            if (state.isNetProfitLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                }
            } else if (state.netProfit != null) {
                val isProfit = state.netProfit >= 0
                Text(
                    text = formatCurrency(state.netProfit),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isProfit) MaterialTheme.colorScheme.tertiary
                    else MaterialTheme.colorScheme.error
                )
                Text(
                    text = if (isProfit) "Profit" else "Loss",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isProfit) MaterialTheme.colorScheme.tertiary
                    else MaterialTheme.colorScheme.error
                )
            } else {
                Text(
                    text = "—",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }*/
        
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetProfitDatePickerDialog(
    title: String,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val pickerState = rememberDatePickerState()
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = pickerState.selectedDateMillis
                    if (millis != null) {
                        val utcCal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
                            timeInMillis = millis
                        }
                        val mm = (utcCal.get(java.util.Calendar.MONTH) + 1).toString().padStart(2, '0')
                        val dd = utcCal.get(java.util.Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
                        onDateSelected("${utcCal.get(java.util.Calendar.YEAR)}-$mm-$dd")
                    }
                }
            ) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    ) {
        DatePicker(
            state = pickerState,
            title = { Text(text = title, modifier = Modifier.padding(start = 24.dp, top = 16.dp)) }
        )
    }
}

// ── Receivables Aging Card ───────────────────────────────────────────────

@Composable
private fun ReceivablesAgingCard(aging: ReceivablesAging) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    com.havos.lubricerp.core.ui.theme.GradientReceivablesStart,
                                    com.havos.lubricerp.core.ui.theme.GradientReceivablesEnd
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PeopleAlt,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Receivables Aging",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "DSO: ${aging.dsoDays.toInt()} days | ${aging.overduePercentage.toInt()}% overdue",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))

            aging.agingBuckets.forEach { bucket ->
                val barColor = when {
                    bucket.isOverdue -> MaterialTheme.colorScheme.error
                    bucket.label.startsWith("0-30") -> Color(0xFF4CAF50)
                    else -> Color(0xFFFF9800)
                }
                val maxAmount = aging.agingBuckets.maxOfOrNull { it.amount } ?: 1.0
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${bucket.label} ${if (bucket.isOverdue) "⚠" else ""}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = formatCurrency(bucket.amount),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth((bucket.amount / maxAmount).toFloat().coerceIn(0f, 1f))
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(4.dp))
                                .background(barColor)
                        )
                    }
                }
            }
        }
    }
}

// ── Cash Position Card ───────────────────────────────────────────────────

@Composable
private fun CashPositionCard(cash: CashPosition) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF00897B), Color(0xFF26A69A))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AttachMoney,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Cash Position",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Projected 7-day: ${formatCurrency(cash.projected7DayBalance)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Balance",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(cash.currentBalance),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Inflow Today",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(cash.todayInflow),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Outflow Today",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(cash.todayOutflow),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            if (cash.bankAccounts.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                Text(
                    text = "Bank Accounts",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                cash.bankAccounts.forEach { account ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${account.accountName} (${account.accountNumber})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = formatCurrency(account.balance),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

// ── Purchase Summary Card ────────────────────────────────────────────────

@Composable
private fun PurchaseSummaryCard(summary: PurchaseSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF5C6BC0), Color(0xFF7986CB))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Inventory,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Purchase Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${summary.openPOCount} open orders | ${summary.poDueThisWeek} due this week",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Open PO Value",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(summary.openPOValue),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (summary.pendingApprovals > 0) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Pending Approval",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${summary.pendingApprovals}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Due This Week",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(summary.poDueThisWeekValue),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF9800)
                    )
                }
            }

            if (summary.recentPurchaseOrders.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                Text(
                    text = "Recent Orders",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                summary.recentPurchaseOrders.take(3).forEach { po ->
                    val statusColor = when (po.status) {
                        "Confirmed" -> Color(0xFF4CAF50)
                        "Pending" -> Color(0xFFFF9800)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = po.poNumber,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = po.vendorName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = formatCurrency(po.amount),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = po.status,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = statusColor
                            )
                        }
                    }
                }
            }
        }
    }
}

private val indiaCurrencyFmt: NumberFormat =
    NumberFormat.getNumberInstance(Locale.forLanguageTag("en-IN")).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }

fun formatCurrency(amount: Double): String = "₹${indiaCurrencyFmt.format(amount)}"
