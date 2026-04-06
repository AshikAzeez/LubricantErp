package com.havos.lubricerp.feature_reports.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Appearance",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
            )

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

            Text(
                text = "Profile",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                val profile = state.profile
                if (profile == null) {
                    ListItem(
                        headlineContent = { Text("Profile not available") },
                        supportingContent = { Text("Login and dashboard sync will populate profile data.") }
                    )
                } else {
                    ListItem(
                        headlineContent = { Text(profile.fullName) },
                        supportingContent = { Text(profile.email) }
                    )
                    ListItem(
                        headlineContent = { Text("Branch ID") },
                        supportingContent = { Text(profile.branchId.toString()) }
                    )
                    ListItem(
                        headlineContent = { Text("Roles") },
                        supportingContent = { Text(profile.rolesText.ifBlank { "-" }) }
                    )
                }
            }
        }
    }
}
