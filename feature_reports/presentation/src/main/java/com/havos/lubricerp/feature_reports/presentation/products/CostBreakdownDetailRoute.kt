package com.havos.lubricerp.feature_reports.presentation.products

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.havos.lubricerp.core.ui.components.CollectEffect
import org.koin.androidx.compose.koinViewModel

@Composable
fun CostBreakdownDetailRoute(
    id: Long,
    onBackClick: () -> Unit,
    onEditClick: (Long) -> Unit,
    refreshTrigger: Boolean = false,
    viewModel: CostBreakdownDetailViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Keyed on refreshTrigger too, so returning from a successful edit reloads exactly once.
    LaunchedEffect(id, refreshTrigger) {
        viewModel.onIntent(CostBreakdownDetailIntent.Load(id))
    }

    CollectEffect(effects = viewModel.effect) { effect ->
        when (effect) {
            is CostBreakdownDetailEffect.Toast ->
                Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
            is CostBreakdownDetailEffect.NavigateToEdit -> onEditClick(effect.id)
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
