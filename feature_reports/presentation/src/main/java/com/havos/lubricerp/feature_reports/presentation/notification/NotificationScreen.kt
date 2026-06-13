package com.havos.lubricerp.feature_reports.presentation.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.havos.lubricerp.feature_reports.domain.model.NotificationItem
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import androidx.compose.foundation.shape.RoundedCornerShape


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
                            fontWeight = FontWeight.Bold
                        )
                        if (state.unreadCount > 0) {
                            Spacer(Modifier.width(8.dp))
                            Badge {
                                Text(
                                    text = if (state.unreadCount > 99) "99+" else state.unreadCount.toString()
                                )
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
                    if (state.unreadCount > 0) {
                        IconButton(
                            onClick = onMarkAllAsRead,
                            enabled = !state.isMarkingAllRead
                        ) {
                            if (state.isMarkingAllRead) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.DoneAll,
                                    contentDescription = "Mark all as read"
                                )
                            }
                        }
                    }
                }
            )
        }
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
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.NotificationsNone,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = error.message ?: "Failed to load notifications",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(onClick = { pagingItems.retry() }) {
                            Text("Retry")
                        }
                    }
                }
                pagingItems.itemCount == 0 && pagingItems.loadState.refresh is LoadState.NotLoading -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.NotificationsNone,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "No notifications",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
                        Column(
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.NotificationsNone,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "No notifications for selected filters",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                count = visibleItems.size,
                                key = { visibleItems[it].id }
                            ) { index ->
                                val item = visibleItems[index]
                                NotificationCard(
                                    item = item,
                                    onMarkAsRead = { onMarkAsRead(item.id) }
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

@Composable
private fun NotificationFilterChipRow(
    selectedTypes: Set<String>,
    onFilterToggled: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NotificationFilterTypes.forEach { type ->
            val selected = type in selectedTypes
            FilterChip(
                selected = selected,
                onClick = { onFilterToggled(type) },
                label = { Text(text = type, style = MaterialTheme.typography.labelMedium) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@Composable
private fun NotificationCard(
    item: NotificationItem,
    onMarkAsRead: () -> Unit
) {
    val containerColor = if (item.isRead) {
        MaterialTheme.colorScheme.surfaceContainerLow
    } else {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.08f)
    }
    val borderColor = if (item.isRead) {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
    }
    val borderWidth = if (item.isRead) 0.5.dp else 1.2.dp

    androidx.compose.material3.Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = containerColor
        ),
        border = androidx.compose.foundation.BorderStroke(borderWidth, borderColor),
        onClick = { if (!item.isRead) onMarkAsRead() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (!item.isRead) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (item.isRead) FontWeight.Medium else FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                NotificationTypeBadge(type = item.type)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = item.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.timeAgo,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!item.isRead) {
                    TextButton(
                        onClick = onMarkAsRead,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "Mark as read",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationTypeBadge(type: String) {
    val (bgColor, textColor) = when (type.lowercase()) {
        "approval" -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f) to MaterialTheme.colorScheme.onErrorContainer
        "warning"  -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.8f) to MaterialTheme.colorScheme.onTertiaryContainer
        "info"     -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f) to MaterialTheme.colorScheme.onPrimaryContainer
        else       -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f) to MaterialTheme.colorScheme.onSecondaryContainer
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Text(
            text = type,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
