package com.havos.lubricerp.feature_reports.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
    val rootState by rootViewModel.state.collectAsStateWithLifecycle()
    koinInject<ResolvedNetworkConfig>()

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

    }
}
