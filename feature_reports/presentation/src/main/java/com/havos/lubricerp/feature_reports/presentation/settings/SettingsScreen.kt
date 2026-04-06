package com.havos.lubricerp.feature_reports.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.havos.lubricerp.core.common.ThemeMode
import com.havos.lubricerp.core.ui.components.ThemeRevealTransitionBus
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsRoute(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    SettingsScreen(
        state = state,
        onAction = { action ->
            when (action) {
                is SettingsAction.ThemeSelected -> {
                    viewModel.onIntent(SettingsIntent.ThemeChanged(action.mode))
                }
            }
        },
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    state: SettingsUiState,
    onAction: (SettingsAction) -> Unit,
    onBackClick: () -> Unit
) {
    val options = remember {
        listOf(
            ThemeMode.SYSTEM to "System default",
            ThemeMode.LIGHT to "Light",
            ThemeMode.DARK to "Dark"
        )
    }
    val optionCenters = remember { mutableStateMapOf<ThemeMode, Offset>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            item {
                Text(
                    text = "Profile",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                )
            }

            item {
                ProfileOverviewCard(profile = state.profile)
            }

            item {
                Text(
                    text = "Appearance",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 12.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    options.forEach { (mode, title) ->
                        val selected = state.selectedThemeMode == mode
                        ListItem(
                            headlineContent = { Text(title) },
                            trailingContent = {
                                RadioButton(
                                    selected = selected,
                                    onClick = null,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .width(48.dp)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onGloballyPositioned { coordinates ->
                                    val topLeft = coordinates.positionInRoot()
                                    val center = Offset(
                                        x = topLeft.x + (coordinates.size.width / 2f),
                                        y = topLeft.y + (coordinates.size.height / 2f)
                                    )
                                    optionCenters[mode] = center
                                }
                                .clickable {
                                    ThemeRevealTransitionBus.emitOrigin(optionCenters[mode])
                                    onAction(SettingsAction.ThemeSelected(mode))
                                }
                        )
                    }
                }
            }

            item { Box(modifier = Modifier.height(20.dp)) }
        }
    }
}

@Composable
private fun ProfileOverviewCard(profile: SettingsProfileUi?) {
    if (profile == null) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            ListItem(
                leadingContent = {
                    Icon(Icons.Default.Person, contentDescription = null)
                },
                headlineContent = { Text("Profile not available") },
                supportingContent = {
                    Text("Open dashboard once after login to sync profile details.")
                }
            )
        }
        return
    }

    val initials = remember(profile.fullName) {
        profile.fullName
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifBlank { "U" }
    }
    val roles = remember(profile.rolesText) {
        profile.rolesText.split(",").map { it.trim() }.filter { it.isNotBlank() }
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 4.dp,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = profile.fullName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = profile.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    ListItem(
                        leadingContent = { Icon(Icons.Default.Business, contentDescription = null) },
                        headlineContent = { Text("Branch") },
                        supportingContent = { Text("Branch ID: ${profile.branchId}") }
                    )
                    ListItem(
                        leadingContent = { Icon(Icons.Default.Email, contentDescription = null) },
                        headlineContent = { Text("Email") },
                        supportingContent = { Text(profile.email) }
                    )
                    ListItem(
                        leadingContent = { Icon(Icons.Default.Badge, contentDescription = null) },
                        headlineContent = { Text("Roles") },
                        supportingContent = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (roles.isEmpty()) {
                                    Text("-")
                                } else {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        roles.take(3).forEach { role ->
                                            AssistChip(
                                                onClick = {},
                                                enabled = false,
                                                label = { Text(role) },
                                                colors = AssistChipDefaults.assistChipColors(
                                                    disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                                    disabledLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
