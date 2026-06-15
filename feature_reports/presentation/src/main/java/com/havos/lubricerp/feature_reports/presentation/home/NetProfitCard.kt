package com.havos.lubricerp.feature_reports.presentation.home

/**
 * Created by Ashik on 13/06/26.
 */

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun NetProfitCard(
    state: HomeTabUiState,
    onIntent: (HomeTabIntent) -> Unit,
    customFrom: String,
    customTo: String,
    onCustomFromChange: (String) -> Unit,
    onCustomToChange: (String) -> Unit,
) {
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }

    val isProfit = (state.netProfit ?: 0.0) >= 0
    val accentColor =
        if (isProfit) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = accentColor.copy(alpha = 0.2f),
                spotColor = accentColor.copy(alpha = 0.2f)
            ),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        Box {
            // Decorative gradient glow in the corner
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 50.dp, y = (-50).dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.18f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(accentColor.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AccountBalanceWallet,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "Net Profit",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Period selector — pill style
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NetProfitPeriod.entries.forEach { period ->
                        val selected = state.netProfitPeriod == period
                        val bg by animateColorAsState(
                            targetValue = if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            animationSpec = tween(250), label = "chipBg"
                        )
                        val fg by animateColorAsState(
                            targetValue = if (selected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            animationSpec = tween(250), label = "chipFg"
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(bg)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { onIntent(HomeTabIntent.NetProfitPeriodChanged(period)) }
                                .padding(horizontal = 14.dp, vertical = 7.dp)

                        ) {
                            Text(
                                text = period.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = fg
                            )
                        }
                    }
                }

                // Custom date range pickers
                AnimatedVisibility(
                    visible = state.netProfitPeriod == NetProfitPeriod.CUSTOM,
                    enter = expandVertically(animationSpec = tween(250)) + fadeIn(),
                    exit = shrinkVertically(animationSpec = tween(200)) + fadeOut()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DatePillButton(
                            label = "From",
                            value = customFrom,
                            modifier = Modifier.weight(1f),
                            onClick = { showFromPicker = true }
                        )
                        DatePillButton(
                            label = "To",
                            value = customTo,
                            modifier = Modifier.weight(1f),
                            onClick = { showToPicker = true }
                        )
                    }
                }

                if (showFromPicker) {
                    NetProfitDatePickerDialog(
                        title = "From Date",
                        onDateSelected = { dateStr ->
                            onCustomFromChange(dateStr)
                            showFromPicker = false
                            if (customTo.isNotBlank()) {
                                onIntent(
                                    HomeTabIntent.NetProfitCustomDateChanged(
                                        dateStr,
                                        customTo
                                    )
                                )
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
                            onCustomToChange(dateStr)
                            showToPicker = false
                            if (customFrom.isNotBlank()) {
                                onIntent(
                                    HomeTabIntent.NetProfitCustomDateChanged(
                                        customFrom,
                                        dateStr
                                    )
                                )
                                onIntent(HomeTabIntent.NetProfitCustomApply)
                            }
                        },
                        onDismiss = { showToPicker = false }
                    )
                }

                // Value display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    when {
                        state.isNetProfitLoading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                strokeWidth = 3.dp,
                                color = accentColor
                            )
                        }

                        state.netProfit != null -> {
                            AnimatedNetProfitValue(
                                value = state.netProfit,
                                accentColor = accentColor,
                                isProfit = isProfit
                            )
                        }

                        else -> {
                            Text(
                                text = "—",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedNetProfitValue(
    value: Double,
    accentColor: Color,
    isProfit: Boolean
) {
    val animatedValue = remember { Animatable(0f) }

    LaunchedEffect(value) {
        animatedValue.snapTo(0f)
        animatedValue.animateTo(
            targetValue = value.toFloat(),
            animationSpec = tween(durationMillis = 800)
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = formatCurrency(animatedValue.value.toDouble()),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = accentColor
        )

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(accentColor.copy(alpha = 0.12f))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = if (isProfit) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = if (isProfit) "Profit" else "Loss",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }
        }
    }
}

@Composable
private fun DatePillButton(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value.ifBlank { "Pick date" },
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
