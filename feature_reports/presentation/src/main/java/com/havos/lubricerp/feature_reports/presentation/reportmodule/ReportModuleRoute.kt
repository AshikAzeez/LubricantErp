package com.havos.lubricerp.feature_reports.presentation.reportmodule

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

@Composable
fun ReportModuleRoute(
    reportItemKey: String,
    onBackClick: () -> Unit,
    viewModel: ReportModuleViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ReportModuleScreen(
        reportItemKey = reportItemKey,
        state = state,
        onAction = viewModel::onAction,
        onBackClick = onBackClick
    )
}
