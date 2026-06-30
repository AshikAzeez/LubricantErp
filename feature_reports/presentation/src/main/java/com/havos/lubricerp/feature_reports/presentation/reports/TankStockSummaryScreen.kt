package com.havos.lubricerp.feature_reports.presentation.reports

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.havos.lubricerp.feature_reports.domain.model.TankStockItem
import java.text.NumberFormat
import kotlin.math.PI
import kotlin.math.sin

@Composable
internal fun TankStockSummaryScreen(
    state: ReportDetailUiState,
    modifier: Modifier = Modifier
) {
    val items = state.tankStockItems
    if (items.isEmpty()) return
    val formatter = remember { NumberFormat.getNumberInstance().apply { maximumFractionDigits = 0 } }
    val cardShape = MaterialTheme.shapes.large
    val dottedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
    val totalCapacity = remember(items) { items.sumOf { it.capacity } }
    val totalStock = remember(items) { items.sumOf { it.currentStock } }
    val totalAvailable = remember(items) { items.sumOf { it.availableCapacity } }

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
                            text = formatter.format(totalCapacity),
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
                            value = "${formatter.format(totalStock)} L"
                        )
                        TankMetricBlock(
                            label = "AVAILABLE",
                            value = "${formatter.format(totalAvailable)} L"
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
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 10.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items.forEach { tank ->
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

        items(items, key = { it.tankCode }) { tank ->
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
                                    text = tank.tankCode,
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
                                text = tank.tankName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = tank.lastGrade.ifBlank { "UNASSIGNED GRADE" }.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${tank.utilizationPercent.toInt()}%",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (tank.utilizationPercent <= 0.0) "EMPTY" else tank.lastGrade.ifBlank { "TANK" }.take(6).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    LinearProgressIndicator(
                        progress = { (tank.utilizationPercent.toFloat().coerceIn(0f, 100f)) / 100f },
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
private fun TankLevelCapsule(tank: TankStockItem) {
    val targetFraction = (tank.utilizationPercent.toFloat() / 100f).coerceIn(0f, 1f)

    // Smoothly animates toward the new level whenever tank data updates,
    // instead of snapping the fill instantly.
    val animatedFraction by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "tankFill",
    )

    // Continuous gentle sloshing motion on the liquid surface.
    val infiniteTransition = rememberInfiniteTransition(label = "liquidWave")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
        ),
        label = "wavePhase",
    )

    val tankBaseColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val tankShadeColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val rimColor = MaterialTheme.colorScheme.outlineVariant
    val liquidTop = MaterialTheme.colorScheme.primary.copy(alpha = 0.92f)
    val liquidBottom = MaterialTheme.colorScheme.primary.copy(alpha = 0.62f)
    val liquidHighlight = Color.White.copy(alpha = 0.25f)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(
            modifier = Modifier
                .width(48.dp)
                .height(142.dp)
        ) {
            val w = size.width
            val h = size.height
            val capH = w * 0.32f
            val bodyTop = capH / 2f
            val bodyBottom = h - capH / 2f
            val strokePx = 1.5.dp.toPx()

            // Tank interior silhouette (bottom cap + body only — deliberately
            // excludes the top dome) used purely as a clip mask. Anything
            // drawn through this can never visually escape the tank.
            val tankInterior = Path().apply {
                addOval(Rect(Offset(0f, h - capH), Size(w, capH)))
                addRect(Rect(Offset(0f, bodyTop), Offset(w, bodyBottom)))
            }

            // ---- 1. Bottom cap shadow ----
            drawOval(color = tankShadeColor, topLeft = Offset(0f, h - capH), size = Size(w, capH))

            // ---- 2. Tank body ----
            drawRect(color = tankBaseColor, topLeft = Offset(0f, bodyTop), size = Size(w, bodyBottom - bodyTop))

            // ---- 3. Liquid, clipped to the tank interior ----
            if (animatedFraction > 0.005f) {
                clipPath(tankInterior) {
                    val surfaceY = lerp(bodyBottom, bodyTop, animatedFraction)
                    val waveAmp = 2.dp.toPx() * (1f - animatedFraction * 0.3f) // calms down near full
                    val steps = 20

                    val surfaceLine = Path()
                    val fillPath = Path()
                    for (i in 0..steps) {
                        val x = w * i / steps
                        val y = surfaceY + sin(wavePhase + x / w * 2 * PI.toFloat()) * waveAmp
                        if (i == 0) {
                            surfaceLine.moveTo(x, y)
                            fillPath.moveTo(x, y)
                        } else {
                            surfaceLine.lineTo(x, y)
                            fillPath.lineTo(x, y)
                        }
                    }
                    fillPath.lineTo(w, bodyBottom + capH)
                    fillPath.lineTo(0f, bodyBottom + capH)
                    fillPath.close()

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(liquidTop, liquidBottom),
                            startY = surfaceY - waveAmp,
                            endY = bodyBottom,
                        ),
                    )
                    drawPath(path = surfaceLine, color = liquidHighlight, style = Stroke(width = 1.dp.toPx()))
                }
            }

            // ---- 4. Lid — drawn after the liquid so the rim always reads as
            // tank metal, never gets painted over even at 100% fill ----
            drawOval(
                brush = Brush.verticalGradient(colors = listOf(tankBaseColor, tankShadeColor)),
                topLeft = Offset(0f, 0f),
                size = Size(w, capH),
            )

            // ---- 5. Outlines ----
            drawOval(rimColor, topLeft = Offset(0f, 0f), size = Size(w, capH), style = Stroke(strokePx))
            drawLine(rimColor, Offset(0f, bodyTop), Offset(0f, bodyBottom), strokeWidth = strokePx)
            drawLine(rimColor, Offset(w, bodyTop), Offset(w, bodyBottom), strokeWidth = strokePx)
            drawArc(
                color = rimColor,
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(0f, h - capH),
                size = Size(w, capH),
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )

            // ---- 6. Barrel ridges ----
            listOf(0.4f, 0.7f).forEach { t ->
                val y = bodyTop + (bodyBottom - bodyTop) * t
                drawLine(rimColor.copy(alpha = 0.6f), Offset(0f, y), Offset(w, y), strokeWidth = 2.dp.toPx())
            }

            // ---- 7. Bung cap ----
            drawCircle(color = rimColor, radius = 3.dp.toPx(), center = Offset(w / 2f, capH * 0.3f))

            // ---- 8. Metal sheen ----
            drawRect(
                color = Color.White.copy(alpha = 0.12f),
                topLeft = Offset(w * 0.16f, bodyTop + 2.dp.toPx()),
                size = Size(w * 0.14f, bodyBottom - bodyTop - 4.dp.toPx()),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = tank.tankCode, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
        Text(
            text = "${tank.utilizationPercent}%",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
