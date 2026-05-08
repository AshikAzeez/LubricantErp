package com.havos.lubricerp.feature_reports.presentation.reports

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.havos.lubricerp.feature_reports.domain.model.TankInfo
import com.havos.lubricerp.feature_reports.domain.model.TankStockSummary
import java.text.NumberFormat

@Composable
internal fun TankStockSummaryScreen(
    state: ReportDetailUiState,
    modifier: Modifier = Modifier
) {
    val summary = state.tankStockSummary ?: mockTankStockSummary()
    val formatter = remember { NumberFormat.getIntegerInstance() }
    val cardShape = MaterialTheme.shapes.large
    val dottedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)

    LazyColumn(
        modifier = modifier
            .drawBehind {
                val stepX = 26.dp.toPx()
                val stepY = 26.dp.toPx()
                var y = 0f
                while (y < size.height) {
                    var x = 0f
                    while (x < size.width) {
                        drawCircle(dottedColor, radius = 1.3.dp.toPx(), center = Offset(x, y))
                        x += stepX
                    }
                    y += stepY
                }
            }
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = cardShape,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "TOTAL CAPACITY",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = formatter.format(summary.totalCapacityLiters),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = " L",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TankMetricBlock(
                            label = "CURRENT STOCK",
                            value = "${formatter.format(summary.currentStockLiters)} L"
                        )
                        TankMetricBlock(
                            label = "AVAILABLE",
                            value = "${formatter.format(summary.availableCapacityLiters)} L"
                        )
                    }
                }
            }
        }

        item {
            SectionTitleWithBadge(
                title = "TANK FILL LEVELS",
                badge = "LIVE FEED"
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = cardShape,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    summary.tanks.forEach { tank ->
                        TankLevelCapsule(tank = tank)
                    }
                }
            }
        }

        item {
            SectionTitleWithBadge(
                title = "DETAILED INVENTORY",
                badge = null
            )
        }

        items(summary.tanks, key = { it.code }) { tank ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surfaceContainerHighest
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = tank.code,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.Default.Storage,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 10.dp)
                        ) {
                            Text(
                                text = "${tank.location} / Zone A",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = tank.productGrade.ifBlank { "UNASSIGNED GRADE" }.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${tank.fillPercent}%",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (tank.fillPercent == 0) "EMPTY" else "ACTIVE",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    LinearProgressIndicator(
                        progress = { (tank.fillPercent.coerceIn(0, 100)) / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

fun mockTankStockSummary(): TankStockSummary {
    return TankStockSummary(
        totalCapacityLiters = 126000,
        currentStockLiters = 0,
        availableCapacityLiters = 126000,
        tanks = listOf(
            TankInfo(
                name = "Tank1",
                code = "TK-01",
                location = "Main Plant",
                productGrade = "Unassigned Grade",
                capacityLiters = 20000,
                currentStockLiters = 0,
                availableLiters = 20000,
                fillPercent = 0
            ),
            TankInfo(
                name = "Tank2",
                code = "TK-02",
                location = "Main Plant",
                productGrade = "Unassigned Grade",
                capacityLiters = 20000,
                currentStockLiters = 0,
                availableLiters = 20000,
                fillPercent = 0
            ),
            TankInfo(
                name = "Tank3",
                code = "TK-03",
                location = "Main Plant",
                productGrade = "Unassigned Grade",
                capacityLiters = 68000,
                currentStockLiters = 0,
                availableLiters = 68000,
                fillPercent = 0
            ),
            TankInfo(
                name = "Tank4",
                code = "TK-04",
                location = "Main Plant",
                productGrade = "Unassigned Grade",
                capacityLiters = 18000,
                currentStockLiters = 0,
                availableLiters = 18000,
                fillPercent = 0
            )
        )
    )
}

@Composable
private fun TankMetricBlock(
    label: String,
    value: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SectionTitleWithBadge(
    title: String,
    badge: String?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        if (!badge.isNullOrBlank()) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceContainerHighest
            ) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun TankLevelCapsule(tank: TankInfo) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .width(44.dp)
                .height(142.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = MaterialTheme.shapes.medium
                )
                .padding(horizontal = 5.dp, vertical = 6.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight((tank.fillPercent.coerceIn(0, 100)) / 100f)
                    .clip(MaterialTheme.shapes.small)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.95f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                            )
                        )
                    )
            )
            repeat(3) { marker ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .align(
                            when (marker) {
                                0 -> Alignment.Center
                                1 -> Alignment.TopCenter
                                else -> Alignment.BottomCenter
                            }
                        )
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = tank.code,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center
        )
        Text(
            text = "${tank.fillPercent}%",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
internal fun TankStockSummaryShimmerScreen(modifier: Modifier = Modifier) {
    val shimmerBrush = rememberShimmerBrush()

    LazyColumn(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ShimmerBlock(
                        brush = shimmerBrush,
                        modifier = Modifier
                            .fillMaxWidth(0.35f)
                            .height(12.dp)
                    )
                    ShimmerBlock(
                        brush = shimmerBrush,
                        modifier = Modifier
                            .fillMaxWidth(0.62f)
                            .height(44.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ShimmerBlock(
                            brush = shimmerBrush,
                            modifier = Modifier
                                .weight(1f)
                                .height(30.dp)
                        )
                        ShimmerBlock(
                            brush = shimmerBrush,
                            modifier = Modifier
                                .weight(1f)
                                .height(30.dp)
                        )
                    }
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    repeat(4) {
                        ShimmerBlock(
                            brush = shimmerBrush,
                            modifier = Modifier
                                .width(38.dp)
                                .height(120.dp)
                                .clip(MaterialTheme.shapes.medium)
                        )
                    }
                }
            }
        }
        items(4) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ShimmerBlock(
                            brush = shimmerBrush,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(MaterialTheme.shapes.medium)
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ShimmerBlock(
                                brush = shimmerBrush,
                                modifier = Modifier
                                    .fillMaxWidth(0.82f)
                                    .height(16.dp)
                            )
                            ShimmerBlock(
                                brush = shimmerBrush,
                                modifier = Modifier
                                    .fillMaxWidth(0.56f)
                                    .height(12.dp)
                            )
                        }
                        ShimmerBlock(
                            brush = shimmerBrush,
                            modifier = Modifier
                                .width(44.dp)
                                .height(30.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun rememberShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "tank_standard_shimmer")
    val translate by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "tank_standard_shimmer_anim"
    )
    val base = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    val highlight = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    return Brush.linearGradient(
        colors = listOf(base, highlight, base),
        start = Offset(translate * 600f, 0f),
        end = Offset((translate + 1f) * 600f, 320f)
    )
}

@Composable
internal fun ShimmerBlock(
    brush: Brush,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(brush)
    )
}
