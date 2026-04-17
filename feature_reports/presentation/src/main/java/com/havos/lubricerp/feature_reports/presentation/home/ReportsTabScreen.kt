package com.havos.lubricerp.feature_reports.presentation.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.havos.lubricerp.core.ui.components.DashboardCardUi
import com.havos.lubricerp.feature_reports.presentation.reports.ReportMenu
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsTabScreen(
    state: ReportsTabUiState,
    onAction: (ReportsTabAction) -> Unit,
    onOpenReport: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(Unit) {
        state.selectedMenu
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            if (state.isProfileLoading) {
                GreetingShimmerCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            } else if (state.greetingName.isNotBlank()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    tonalElevation = 1.dp
                ) {
                    Text(
                        text = "Hello, ${state.greetingName}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                    )
                }
            }

            Text(
                text = "Reports",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            DashboardCardGrid(
                items = state.cards,
                onCardClick = { onAction(ReportsTabAction.CardClicked(it.id)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }

    state.selectedMenu?.let { menu ->
        ModalBottomSheet(
            onDismissRequest = { onAction(ReportsTabAction.DismissBottomSheet) },
            sheetState = sheetState
        ) {
            Text(
                text = menu.title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
            menu.subMenus.forEach { report ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .clickable { onAction(ReportsTabAction.SubMenuClicked(report)) },
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    ListItem(
                        headlineContent = {
                            Text(
                                text = report.title,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DashboardCardGrid(
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
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(items, key = { it.id }) { card ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCardClick(card) },
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp, horizontal = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = card.icon,
                            contentDescription = card.title,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = card.title,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GreetingShimmerCard(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            ShimmerBar(width = 180.dp, height = 20.dp)
            Spacer(modifier = Modifier.height(8.dp))
            ShimmerBar(width = 120.dp, height = 14.dp)
        }
    }
}

@Composable
private fun ShimmerBar(
    width: Dp,
    height: Dp
) {
    val transition = rememberInfiniteTransition(label = "greeting_shimmer")
    val animationValue by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "greeting_shimmer_anim"
    )
    val brush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
            Color.White.copy(alpha = 0.85f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        ),
        start = androidx.compose.ui.geometry.Offset(animationValue * 300f, 0f),
        end = androidx.compose.ui.geometry.Offset((animationValue + 1f) * 300f, 120f)
    )
    Spacer(
        modifier = Modifier
            .width(width)
            .height(height)
            .background(brush = brush, shape = MaterialTheme.shapes.small)
    )
}
