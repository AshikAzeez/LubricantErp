package com.havos.lubricerp.feature_reports.presentation.products

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.havos.lubricerp.core.ui.components.CollectEffect
import org.koin.androidx.compose.koinViewModel

@Composable
fun CostBreakdownDetailRoute(
    id: Long,
    onBackClick: () -> Unit,
    viewModel: CostBreakdownDetailViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(id) {
        viewModel.onIntent(CostBreakdownDetailIntent.Load(id))
    }

    CollectEffect(effects = viewModel.effect) { effect ->
        when (effect) {
            CostBreakdownDetailEffect.NavigateToEdit -> onBackClick()
            is CostBreakdownDetailEffect.NavigateToConvertToPi -> onBackClick()
            CostBreakdownDetailEffect.Deleted -> onBackClick()
            CostBreakdownDetailEffect.NavigateBack -> onBackClick()
        }
    }

    CostBreakdownDetailScreen(
        state = state,
        onBackClick = onBackClick,
        onAction = viewModel::onAction
    )
}
