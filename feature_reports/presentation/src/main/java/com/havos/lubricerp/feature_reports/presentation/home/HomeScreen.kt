package com.havos.lubricerp.feature_reports.presentation.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.havos.lubricerp.core.ui.components.CollectEffect
import com.havos.lubricerp.core.ui.components.DashboardCardGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Settings
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeRoute(
    onOpenReport: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onNavigateLogin: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    CollectEffect(effects = viewModel.effect) { effect ->
        when (effect) {
            is HomeEffect.OpenReport -> onOpenReport(effect.reportItem.key)
            HomeEffect.NavigateToLogin -> onNavigateLogin()
        }
    }

    HomeScreen(
        state = state,
        onAction = { action ->
            when (action) {
                is HomeAction.CardClicked -> {
                    ReportMenu.entries.firstOrNull { it.key == action.menuKey }?.let {
                        viewModel.onIntent(HomeIntent.CardClicked(it))
                    }
                }

                is HomeAction.SubMenuClicked -> viewModel.onSubMenuClicked(action.reportItem)
                HomeAction.DismissBottomSheet -> viewModel.onIntent(HomeIntent.BottomSheetDismissed)
                HomeAction.SettingsClicked -> onOpenSettings()
                HomeAction.LogoutClicked -> viewModel.onIntent(HomeIntent.LogoutClicked)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(
    state: HomeUiState,
    onAction: (HomeAction) -> Unit,
) {
    var showLogoutConfirmation by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val logoResId = remember {
        context.resources.getIdentifier("erp_logo", "drawable", context.packageName)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (logoResId != 0) {
                            Image(
                                painter = painterResource(id = logoResId),
                                contentDescription = "Goal ERP",
                                modifier = Modifier
                                    .height(32.dp)
                                    .widthIn(max = 110.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(text = "Goal Lubricants ERP")
                            Text(
                                text = "Reports Dashboard",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { onAction(HomeAction.SettingsClicked) }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                    IconButton(onClick = { showLogoutConfirmation = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Logout"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
        ) {
            val maxContentWidth = if (maxWidth >= 1024.dp) 1100.dp else 840.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = maxContentWidth)
                    .align(Alignment.TopCenter)
            ) {
                if (state.username.isNotBlank()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        tonalElevation = 1.dp
                    ) {
                        Text(
                            text = "Hello, ${state.username}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                        )
                    }
                }

                DashboardCardGrid(
                    items = state.cards,
                    onCardClick = { onAction(HomeAction.CardClicked(it.id)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        }

        state.selectedMenu?.let { menu ->
            ModalBottomSheet(onDismissRequest = { onAction(HomeAction.DismissBottomSheet) }) {
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
                            .clickable { onAction(HomeAction.SubMenuClicked(report)) },
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
            }
        }

        if (showLogoutConfirmation) {
            AlertDialog(
                onDismissRequest = { showLogoutConfirmation = false },
                title = { Text("Confirm Logout") },
                text = { Text("Do you want to logout from Goal Lubricants ERP?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showLogoutConfirmation = false
                            onAction(HomeAction.LogoutClicked)
                        }
                    ) {
                        Text("Logout")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutConfirmation = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

private typealias ReportMenu = com.havos.lubricerp.feature_reports.presentation.reports.ReportMenu
