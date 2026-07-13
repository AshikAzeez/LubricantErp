package com.havos.lubricerp.feature_reports.presentation.notification

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.havos.lubricerp.feature_reports.domain.model.NotificationItem
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@Composable
fun NotificationRoute(
    onBackClick: () -> Unit,
    viewModel: NotificationViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pagingItems = viewModel.notificationsPaged.collectAsLazyPagingItems()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is NotificationEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
                NotificationEffect.MarkedAllRead -> pagingItems.refresh()
            }
        }
    }

    NotificationScreen(
        state = state,
        pagingItems = pagingItems,
        snackbarHostState = snackbarHostState,
        onBackClick = onBackClick,
        onMarkAsRead = { id -> viewModel.onIntent(NotificationIntent.MarkAsRead(id)) },
        onMarkAllAsRead = { viewModel.onIntent(NotificationIntent.MarkAllAsRead) },
        onFilterToggled = { type -> viewModel.onIntent(NotificationIntent.FilterToggled(type)) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationScreen(
    state: NotificationUiState,
    pagingItems: LazyPagingItems<NotificationItem>,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    onMarkAsRead: (Long) -> Unit,
    onMarkAllAsRead: () -> Unit,
    onFilterToggled: (String) -> Unit = {}
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Notifications",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        AnimatedVisibility(
                            visible = state.unreadCount > 0,
                            enter = scaleIn(animationSpec = tween(200)) + fadeIn(),
                            exit = scaleOut(animationSpec = tween(200)) + fadeOut()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Spacer(Modifier.width(8.dp))
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.error
                                ) {
                                    AnimatedContent(
                                        targetState = state.unreadCount,
                                        transitionSpec = {
                                            (slideInVertically { h -> h } + fadeIn()) togetherWith
                                                    (slideOutVertically { h -> -h } + fadeOut())
                                        },
                                        label = "unreadCount"
                                    ) { count ->
                                        Text(text = if (count > 99) "99+" else count.toString())
                                    }
                                }
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    AnimatedVisibility(
                        visible = state.unreadCount > 0,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        Box(
                            modifier = Modifier.size(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Crossfade(
                                targetState = state.isMarkingAllRead,
                                label = "markAllRead"
                            ) { marking ->
                                if (marking) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    IconButton(onClick = onMarkAllAsRead) {
                                        Icon(
                                            imageVector = Icons.Filled.DoneAll,
                                            contentDescription = "Mark all as read"
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NotificationFilterChipRow(
                selectedTypes = state.selectedTypes,
                onFilterToggled = onFilterToggled
            )
            Box(modifier = Modifier.weight(1f)) {
                when {
                    pagingItems.loadState.refresh is LoadState.Loading -> {
                        com.havos.lubricerp.core.ui.components.NotificationListShimmer(
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    pagingItems.loadState.refresh is LoadState.Error -> {
                        val error = (pagingItems.loadState.refresh as LoadState.Error).error
                        EmptyStateView(
                            modifier = Modifier.align(Alignment.Center),
                            icon = Icons.Filled.NotificationsNone,
                            title = "Something went wrong",
                            subtitle = error.message ?: "Failed to load notifications",
                            isError = true
                        ) {
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = { pagingItems.retry() },
                                shape = RoundedCornerShape(50)
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                    pagingItems.itemCount == 0 && pagingItems.loadState.refresh is LoadState.NotLoading -> {
                        EmptyStateView(
                            modifier = Modifier.align(Alignment.Center),
                            icon = Icons.Filled.NotificationsNone,
                            title = "No notifications",
                            subtitle = "You're all caught up. New updates will appear here."
                        )
                    }
                    else -> {
                        val knownTypes = setOf("info", "warning", "approval")
                        val visibleItems = remember(pagingItems.itemCount, state.selectedTypes) {
                            (0 until pagingItems.itemCount)
                                .mapNotNull { pagingItems[it] }
                                .let { list ->
                                    if (state.selectedTypes.isEmpty()) list
                                    else list.filter { item ->
                                        state.selectedTypes.any { selected ->
                                            if (selected.equals("Other", ignoreCase = true))
                                                item.type.lowercase() !in knownTypes
                                            else
                                                item.type.equals(selected, ignoreCase = true)
                                        }
                                    }
                                }
                        }
                        if (visibleItems.isEmpty() && pagingItems.loadState.refresh is LoadState.NotLoading) {
                            EmptyStateView(
                                modifier = Modifier.align(Alignment.Center),
                                icon = Icons.Filled.NotificationsNone,
                                title = "No notifications for selected filters",
                                subtitle = "Try a different filter to see more updates."
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(
                                    count = visibleItems.size,
                                    key = { visibleItems[it].id }
                                ) { index ->
                                    val item = visibleItems[index]
                                    NotificationCard(
                                        item = item,
                                        onMarkAsRead = { onMarkAsRead(item.id) },
                                        modifier = Modifier.animateItem(
                                            fadeInSpec = tween(300),
                                            placementSpec = tween(300),
                                            fadeOutSpec = tween(150)
                                        )
                                    )
                                }
                                if (pagingItems.loadState.append is LoadState.Loading) {
                                    item {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                        }
                                    }
                                }
                                if (pagingItems.loadState.append is LoadState.Error) {
                                    item {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            TextButton(onClick = { pagingItems.retry() }) {
                                                Text("Load more failed. Tap to retry.")
                                            }
                                        }
                                    }
                                }
                            }
                        } // end else visibleItems not empty
                    } // end else when branch
                } // end when
            } // end Box weight(1f)
        } // end Column
    }
}

/**
 * Reusable, richer empty / error state with a soft icon badge.
 */
@Composable
private fun EmptyStateView(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    isError: Boolean = false,
    extra: @Composable (() -> Unit)? = null
) {
    val tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(34.dp),
                tint = tint
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        extra?.invoke()
    }
}

/**
 * Icon + colors associated with a notification type, used both for the
 * filter pills and the notification cards so the visual language matches.
 */
@Composable
private fun typeIcon(type: String): ImageVector = when (type.lowercase()) {
    "approval" -> Icons.Filled.CheckCircle
    "warning" -> Icons.Filled.Warning
    "info" -> Icons.Filled.Info
    else -> Icons.Filled.Notifications
}

@Composable
private fun typeColors(type: String): Pair<Color, Color> = when (type.lowercase()) {
    "approval" -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f) to MaterialTheme.colorScheme.onErrorContainer
    "warning" -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.8f) to MaterialTheme.colorScheme.onTertiaryContainer
    "info" -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f) to MaterialTheme.colorScheme.onPrimaryContainer
    else -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f) to MaterialTheme.colorScheme.onSecondaryContainer
}

@Composable
private fun NotificationFilterChipRow(
    selectedTypes: Set<String>,
    onFilterToggled: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NotificationFilterTypes.forEach { type ->
            val selected = type in selectedTypes
            FilterPill(
                label = type,
                icon = typeIcon(type),
                selected = selected,
                onClick = { onFilterToggled(type) }
            )
        }
    }
}

@Composable
private fun FilterPill(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surfaceContainerHigh,
        animationSpec = tween(220),
        label = "filterContainer"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(220),
        label = "filterContent"
    )

    Surface(
        shape = RoundedCornerShape(50),
        color = containerColor,
        modifier = Modifier.clip(RoundedCornerShape(50))
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
        }
    }
}

@Composable
private fun NotificationCard(
    item: NotificationItem,
    onMarkAsRead: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        targetValue = if (item.isRead) {
            MaterialTheme.colorScheme.surfaceContainerLow
        } else {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.10f)
        },
        animationSpec = tween(400),
        label = "cardContainer"
    )
    val borderColor by animateColorAsState(
        targetValue = if (item.isRead) {
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        } else {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        },
        animationSpec = tween(400),
        label = "cardBorder"
    )
    val borderWidth by animateDpAsState(
        targetValue = if (item.isRead) 0.5.dp else 1.2.dp,
        animationSpec = tween(400),
        label = "cardBorderWidth"
    )

    val (typeBg, typeFg) = typeColors(item.type)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(300)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(borderWidth, borderColor),
        onClick = { if (!item.isRead) onMarkAsRead() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Type icon badge
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(typeBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = typeIcon(item.type),
                        contentDescription = null,
                        tint = typeFg,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (item.isRead) FontWeight.Medium else FontWeight.Bold,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        AnimatedVisibility(
                            visible = !item.isRead,
                            enter = scaleIn() + fadeIn(),
                            exit = scaleOut() + fadeOut()
                        ) {
                            Box(modifier = Modifier.padding(start = 8.dp)) {
                                PulsingDot(color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = item.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = item.timeAgo,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = typeBg
                    ) {
                        Text(
                            text = item.type,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = typeFg,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                AnimatedVisibility(
                    visible = !item.isRead,
                    enter = fadeIn() + expandHorizontally(),
                    exit = fadeOut() + shrinkHorizontally()
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                        modifier = Modifier.clip(RoundedCornerShape(50))
                    ) {
                        Row(
                            modifier = Modifier
                                .clickable(onClick = onMarkAsRead)
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Done,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Mark as read",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Soft pulsing dot used to draw attention to unread notifications without
 * being distracting — gives the screen a "live" premium feel.
 */
@Composable
private fun PulsingDot(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "unreadPulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "unreadPulseAlpha"
    )
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = alpha))
    )
}
