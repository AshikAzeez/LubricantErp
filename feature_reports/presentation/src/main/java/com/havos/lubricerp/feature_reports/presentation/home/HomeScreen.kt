package com.havos.lubricerp.feature_reports.presentation.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.havos.lubricerp.feature_reports.presentation.reports.ReportMenu
import org.koin.androidx.compose.koinViewModel

private enum class BottomNavItem(val title: String) {
    HOME("Home"),
    REPORTS("Reports")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeRoute(
    onOpenReport: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onNavigateLogin: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
    homeTabViewModel: HomeTabViewModel = koinViewModel(),
    reportsTabViewModel: ReportsTabViewModel = koinViewModel()
) {
    val reportsState by reportsTabViewModel.state.collectAsStateWithLifecycle()
    var showLogoutConfirmation by rememberSaveable { mutableStateOf(false) }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    HomeScreen(
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it },
        reportsState = reportsState,
        reportsTabViewModel = reportsTabViewModel,
        onOpenReport = onOpenReport,
        onOpenSettings = onOpenSettings,
        onLogoutClick = { showLogoutConfirmation = true }
    )

    if (showLogoutConfirmation) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmation = false },
            title = { Text("Confirm Logout") },
            text = { Text("Do you want to logout from Goal Lubricants ERP?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutConfirmation = false
                        viewModel.onIntent(HomeIntent.LogoutClicked)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    reportsState: ReportsTabUiState,
    reportsTabViewModel: ReportsTabViewModel,
    onOpenReport: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row {
                        Text(
                            text = "Goal ERP",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                    IconButton(onClick = onLogoutClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Logout"
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.navigationBarsPadding()
            ) {
                BottomNavItem.entries.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector =                                 if (selectedTab == index) {
                                    when (item) {
                                        BottomNavItem.HOME -> Icons.Filled.Home
                                        BottomNavItem.REPORTS -> Icons.AutoMirrored.Filled.List
                                    }
                                } else {
                                    when (item) {
                                        BottomNavItem.HOME -> Icons.Outlined.Home
                                        BottomNavItem.REPORTS -> Icons.AutoMirrored.Outlined.List
                                    }
                                },
                                contentDescription = item.title
                            )
                        },
                        label = { Text(item.title) },
                        selected = selectedTab == index,
                        onClick = { onTabSelected(index) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> HomeTabContent()
                1 -> ReportsTabContent(
                    state = reportsState,
                    reportsTabViewModel = reportsTabViewModel,
                    onOpenReport = onOpenReport
                )
            }
        }
    }
}

@Composable
private fun HomeTabContent() {
    val viewModel: HomeTabViewModel = koinViewModel()
    HomeTabScreen(viewModel = viewModel)
}

@Composable
private fun ReportsTabContent(
    state: ReportsTabUiState,
    reportsTabViewModel: ReportsTabViewModel,
    onOpenReport: (String) -> Unit
) {
    ReportsTabScreen(
        state = state,
        onAction = { action ->
            when (action) {
                is ReportsTabAction.CardClicked -> {
                    ReportMenu.entries.firstOrNull { it.key == action.menuKey }?.let { menu ->
                        reportsTabViewModel.onIntent(ReportsTabIntent.CardClicked(menu))
                    }
                }
                is ReportsTabAction.SubMenuClicked -> {
                    reportsTabViewModel.onSubMenuClicked(action.reportItem)
                }
                ReportsTabAction.DismissBottomSheet -> {
                    reportsTabViewModel.onIntent(ReportsTabIntent.BottomSheetDismissed)
                }
            }
        },
        onOpenReport = onOpenReport
    )
}
