package com.havos.lubricerp.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium)
            Text(text = value, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

data class DashboardCardUi(
    val id: String,
    val title: String,
    val icon: ImageVector
)

@Composable
fun DashboardCardGrid(
    items: List<DashboardCardUi>,
    onCardClick: (DashboardCardUi) -> Unit,
    minColumnWidth: Dp = 180.dp,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val adaptiveCell = when {
            maxWidth >= 1024.dp -> minColumnWidth
            maxWidth >= 640.dp -> minColumnWidth
            else -> 160.dp
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(adaptiveCell),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(items, key = { it.id }) { card ->
                val interactionSource = remember { MutableInteractionSource() }
                val pressed by interactionSource.collectIsPressedAsState()
                val focused by interactionSource.collectIsFocusedAsState()
                val hovered by interactionSource.collectIsHoveredAsState()
                val scale by animateFloatAsState(
                    targetValue = if (pressed) 0.97f else 1f,
                    animationSpec = tween(150),
                    label = "dashboardCardScale"
                )

                Card(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.medium)
                        .defaultMinSize(minHeight = 124.dp)
                        .scale(scale)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClickLabel = card.title,
                            role = Role.Button
                        ) { onCardClick(card) },
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = when {
                            pressed || focused -> MaterialTheme.colorScheme.primaryContainer
                            hovered -> MaterialTheme.colorScheme.secondaryContainer
                            else -> MaterialTheme.colorScheme.surfaceContainer
                        }
                    ),
                    elevation = CardDefaults.elevatedCardElevation(
                        defaultElevation = 2.dp,
                        pressedElevation = 8.dp,
                        focusedElevation = 6.dp,
                        hoveredElevation = 4.dp
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp, horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = card.icon,
                                contentDescription = card.title,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = card.title,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(top = 12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
