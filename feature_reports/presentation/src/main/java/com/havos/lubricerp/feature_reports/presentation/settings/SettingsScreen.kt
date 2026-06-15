package com.havos.lubricerp.feature_reports.presentation.settings

import android.content.Intent
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.havos.lubricerp.core.common.ThemeMode
import com.havos.lubricerp.core.ui.components.CollectEffect
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsRoute(
    onBackClick: () -> Unit,
    onNavigateLogin: () -> Unit = {},
    viewModel: SettingsViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val state = viewModel.state.collectAsStateWithLifecycle().value
    var showLogoutConfirmation by rememberSaveable { mutableStateOf(false) }

    CollectEffect(effects = viewModel.effect) { effect ->
        when (effect) {
            SettingsEffect.NavigateToLogin -> onNavigateLogin()
            SettingsEffect.CacheCleared -> {
                Toast.makeText(context, "Cached data cleared successfully", Toast.LENGTH_SHORT).show()
            }
        }
    }

    SettingsScreen(
        state = state,
        onAction = { action ->
            when (action) {
                is SettingsAction.ThemeSelected -> {
                    viewModel.onIntent(SettingsIntent.ThemeChanged(action.mode))
                }
                is SettingsAction.LogoutClicked -> {
                    showLogoutConfirmation = true
                }
                SettingsAction.ClearCacheClicked -> {
                    viewModel.onIntent(SettingsIntent.ClearCacheClicked)
                }
                SettingsAction.DismissCacheDialog -> {
                    viewModel.onIntent(SettingsIntent.DismissCacheDialog)
                }
                SettingsAction.ConfirmClearCache -> {
                    viewModel.onIntent(SettingsIntent.ConfirmClearCache)
                }
            }
        },
        onBackClick = onBackClick
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
                        viewModel.onIntent(SettingsIntent.LogoutClicked)
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

    if (state.showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onIntent(SettingsIntent.DismissCacheDialog) },
            title = { Text("Clear Cache") },
            text = { Text("Remove locally stored profile and temporary data? Your session will not be affected.") },
            confirmButton = {
                TextButton(onClick = { viewModel.onIntent(SettingsIntent.ConfirmClearCache) }) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onIntent(SettingsIntent.DismissCacheDialog) }) {
                    Text("Cancel")
                }
            }
        )
    }
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
            ThemeMode.SYSTEM to Triple("System default", "Follow device setting", Icons.Filled.PhoneAndroid),
            ThemeMode.LIGHT to Triple("Light", "Bright and clean look", Icons.Filled.LightMode),
            ThemeMode.DARK to Triple("Dark", "Easy on the eyes at night", Icons.Filled.DarkMode)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item { ProfileOverviewCard(profile = state.profile) }

                item {
                    SectionHeader(
                        title = "Data & Cache",
                        icon = Icons.Outlined.Storage,
                        modifier = Modifier.padding(top = 24.dp, bottom = 10.dp)
                    )
                }

                item { CacheCard(onClick = { onAction(SettingsAction.ClearCacheClicked) }) }

                item {
                    SectionHeader(
                        title = "Appearance",
                        icon = Icons.Outlined.Palette,
                        modifier = Modifier.padding(top = 24.dp, bottom = 10.dp)
                    )
                }

                item {
                    ThemeSelectorCard(
                        options = options,
                        selectedMode = state.selectedThemeMode,
                        onOptionClick = { mode ->
                            onAction(SettingsAction.ThemeSelected(mode))
                        }
                    )
                }

                item {
                    SectionHeader(
                        title = "Account",
                        icon = Icons.Outlined.Shield,
                        modifier = Modifier.padding(top = 24.dp, bottom = 10.dp)
                    )
                }

                item { LogoutCard(onClick = { onAction(SettingsAction.LogoutClicked) }) }

                item {
                    SectionHeader(
                        title = "App Info",
                        icon = Icons.Filled.Info,
                        modifier = Modifier.padding(top = 24.dp, bottom = 10.dp)
                    )
                }

                item { AppInfoCard(flavorSuffix = state.flavorDisplaySuffix) }

                item { Box(modifier = Modifier.height(24.dp)) }
            }
        }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ProfileOverviewCard(profile: SettingsProfileUi?) {
    if (profile == null) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(
                0.5.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column {
                    Text(
                        text = "Profile not available",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Open dashboard once after login to sync profile details.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
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

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shadowElevation = 3.dp,
        border = BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Column {
            // ---- Hero gradient header ----
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                com.havos.lubricerp.core.ui.theme.GradientSalesStart.copy(alpha = 0.95f),
                                com.havos.lubricerp.core.ui.theme.GradientSalesEnd.copy(alpha = 0.95f)
                            )
                        )
                    )
            ) {
                // Decorative glow circles
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 40.dp, y = (-40).dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                )
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.BottomStart)
                        .offset(x = (-24).dp, y = 24.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.06f))
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.White.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = profile.fullName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = profile.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            // ---- Detail rows ----
            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                InfoRow(
                    icon = Icons.Default.Business,
                    title = "Branch",
                    subtitle = "Branch ID: ${profile.branchId}"
                )
                InfoRow(
                    icon = Icons.Default.Email,
                    title = "Email",
                    subtitle = profile.email
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    IconBadge(icon = Icons.Default.Badge)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Roles",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (roles.isEmpty()) {
                            Text(
                                text = "-",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                roles.take(3).forEach { role ->
                                    RolePill(text = role)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IconBadge(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        IconBadge(icon = icon)
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RolePill(text: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun ThemeSelectorCard(
    options: List<Pair<ThemeMode, Triple<String, String, ImageVector>>>,
    selectedMode: ThemeMode,
    onOptionClick: (ThemeMode) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shadowElevation = 2.dp,
        border = BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(vertical = 6.dp)) {
            options.forEachIndexed { index, (mode, info) ->
                val (title, subtitle, icon) = info
                val selected = selectedMode == mode
                val rowBackground = if (selected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                } else {
                    Color.Transparent
                }
                val iconTint = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(rowBackground)
                        .clickable { onOptionClick(mode) }
                        .padding(horizontal = 12.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (selected) {
                                    Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                                        )
                                    )
                                } else {
                                    Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.surfaceContainerHighest,
                                            MaterialTheme.colorScheme.surfaceContainerHighest
                                        )
                                    )
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = if (selected) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                        contentDescription = if (selected) "Selected" else null,
                        tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }

                if (index != options.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 68.dp, end = 16.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

@Composable
private fun LogoutCard(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
        shadowElevation = 1.dp,
        border = BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.error.copy(alpha = 0.25f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Logout",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Sign out of your account",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun CacheCard(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shadowElevation = 1.dp,
        border = BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Clear Cached Data",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Remove locally stored profile and temporary data",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun AppInfoCard(flavorSuffix: String = "") {
    val context = LocalContext.current
    val packageInfo = remember {
        context.packageManager.getPackageInfo(context.packageName, 0)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shadowElevation = 1.dp,
        border = BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column {
            if (VERSION.SDK_INT >= VERSION_CODES.P) {
                AppDetailRow(
                    icon = Icons.Filled.Info,
                    title = "Version",
                    subtitle = "${packageInfo.versionName}${flavorSuffix} (Build ${packageInfo.longVersionCode?: 0})"
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(start = 72.dp, end = 16.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable {
                        context.startActivity(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                        )
                    },
                color = Color.Transparent
            ) {
                AppDetailRow(
                    icon = Icons.Filled.Badge,
                    title = "Open Source Licenses",
                    subtitle = "View third-party library licenses",
                    showChevron = true
                )
            }
        }
    }
}

@Composable
private fun AppDetailRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    showChevron: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (showChevron) {
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}
