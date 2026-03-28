package com.havos.lubricerp.feature_reports.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.havos.lubricerp.core.network.ResolvedNetworkConfig
import com.havos.lubricerp.feature_reports.presentation.home.HomeRoute
import com.havos.lubricerp.feature_reports.presentation.login.LoginRoute
import com.havos.lubricerp.feature_reports.presentation.reports.ReportDetailRoute
import com.havos.lubricerp.feature_reports.presentation.settings.SettingsRoute
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun GoalErpNavGraph(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val rootViewModel: RootViewModel = koinViewModel()
    val networkConfig: ResolvedNetworkConfig = koinInject()
    val rootState by rootViewModel.state.collectAsStateWithLifecycle()
    val environmentBadge = remember(networkConfig) {
        "${networkConfig.environment.name} | ${if (networkConfig.useMockEngine) "MOCK" else "LIVE"}"
    }

    Box(modifier = modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = AppRoutes.GATE,
            modifier = Modifier.fillMaxSize(),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(320)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(320)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            }
        ) {
            composable(AppRoutes.GATE) {
                LaunchedEffect(rootState.isLoading, rootState.isAuthenticated) {
                    if (!rootState.isLoading) {
                        val destination = if (rootState.isAuthenticated) AppRoutes.HOME else AppRoutes.LOGIN
                        navController.navigate(destination) {
                            popUpTo(AppRoutes.GATE) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            }

            composable(AppRoutes.LOGIN) {
                LoginRoute(
                    onNavigateHome = {
                        navController.navigate(AppRoutes.HOME) {
                            popUpTo(AppRoutes.LOGIN) { inclusive = true }
                        }
                    }
                )
            }

            composable(AppRoutes.HOME) {
                HomeRoute(
                    onOpenReport = { reportKey ->
                        navController.navigate(AppRoutes.reportDetail(reportKey))
                    },
                    onOpenSettings = {
                        navController.navigate(AppRoutes.SETTINGS)
                    },
                    onNavigateLogin = {
                        navController.navigate(AppRoutes.LOGIN) {
                            popUpTo(AppRoutes.HOME) { inclusive = true }
                        }
                    }
                )
            }

            composable(AppRoutes.SETTINGS) {
                SettingsRoute(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(
                route = AppRoutes.REPORT_DETAIL,
                arguments = listOf(navArgument("reportKey") { type = NavType.StringType })
            ) { backStackEntry ->
                val reportKey = backStackEntry.arguments?.getString("reportKey").orEmpty()
                ReportDetailRoute(
                    reportKey = reportKey,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        Surface(
            tonalElevation = 6.dp,
            shadowElevation = 6.dp,
            color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.92f),
            contentColor = MaterialTheme.colorScheme.inverseOnSurface,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 8.dp, end = 12.dp)
                .zIndex(10f)
                .clip(MaterialTheme.shapes.small)
                .alpha(0.98f)
        ) {
            Text(
                text = environmentBadge,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
    }
}
