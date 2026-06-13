package com.havos.lubricerp.feature_reports.presentation.home

 import androidx.compose.animation.*
 import androidx.compose.animation.core.*
 import androidx.compose.foundation.background
 import androidx.compose.foundation.clickable
 import androidx.compose.foundation.interaction.MutableInteractionSource
 import androidx.compose.foundation.layout.fillMaxWidth
 import androidx.compose.foundation.layout.*
 import androidx.compose.foundation.shape.CircleShape
 import androidx.compose.foundation.shape.RoundedCornerShape
 import androidx.compose.material.icons.Icons
 import androidx.compose.material.icons.automirrored.filled.List
 import androidx.compose.material.icons.automirrored.outlined.List
 import androidx.compose.material.icons.filled.Home
 import androidx.compose.material.icons.filled.Notifications
 import androidx.compose.material.icons.outlined.Home
 import androidx.compose.material.icons.outlined.Notifications
 import androidx.compose.material3.Icon
 import androidx.compose.material3.MaterialTheme
 import androidx.compose.material3.Surface
 import androidx.compose.material3.Text
 import androidx.compose.runtime.Composable
 import androidx.compose.runtime.getValue
 import androidx.compose.runtime.remember
 import androidx.compose.ui.Alignment
 import androidx.compose.ui.Modifier
 import androidx.compose.ui.draw.clip
 import androidx.compose.ui.draw.shadow
 import androidx.compose.ui.graphics.Color
 import androidx.compose.ui.graphics.graphicsLayer
 import androidx.compose.ui.graphics.vector.ImageVector
 import androidx.compose.ui.unit.dp

/**
 * Created by Ashik on 13/06/26.
 */

@Composable
fun PremiumBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onNavigate: (HomeNavigation) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
            ),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem.entries.forEachIndexed { index, item ->
                PremiumNavItem(
                    item = item,
                    selected = selectedTab == index,
                    onClick = {
                        if (item == BottomNavItem.NOTIFICATIONS) {
                            onNavigate(HomeNavigation.OpenNotifications)
                        } else {
                            onTabSelected(index)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun PremiumNavItem(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        animationSpec = tween(300), label = "bgColor"
    )
    val iconTint by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(300), label = "iconTint"
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.15f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box {
                AnimatedContent(
                    targetState = selected,
                    transitionSpec = {
                        (scaleIn(initialScale = 0.6f) + fadeIn()) togetherWith
                                (scaleOut(targetScale = 0.6f) + fadeOut())
                    },
                    label = "iconSwap"
                ) { isSelected ->
                    Icon(
                        imageVector = getIconForItem(item, isSelected),
                        contentDescription = item.title,
                        tint = iconTint,
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer(scaleX = scale, scaleY = scale)
                    )
                }

                if (item == BottomNavItem.NOTIFICATIONS) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .align(Alignment.TopEnd)
                            .background(MaterialTheme.colorScheme.error, CircleShape)
                    )
                }
            }

            AnimatedVisibility(visible = selected) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.labelSmall,
                    color = iconTint,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

private fun getIconForItem(item: BottomNavItem, selected: Boolean): ImageVector = when (item) {
    BottomNavItem.NOTIFICATIONS -> if (selected) Icons.Filled.Notifications else Icons.Outlined.Notifications
    BottomNavItem.HOME -> if (selected) Icons.Filled.Home else Icons.Outlined.Home
    else -> if (selected) Icons.AutoMirrored.Filled.List else Icons.AutoMirrored.Outlined.List
}
