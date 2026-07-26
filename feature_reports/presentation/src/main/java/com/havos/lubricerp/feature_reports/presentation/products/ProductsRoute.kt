package com.havos.lubricerp.feature_reports.presentation.products

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.havos.lubricerp.core.ui.components.CollectEffect
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProductsRoute(
    onBackClick: () -> Unit,
    onNavigateDetail: (Long) -> Unit = {},
    viewModel: ProductsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onIntent(ProductsIntent.Load)
    }

    CollectEffect(effects = viewModel.effect) { effect ->
        when (effect) {
            is ProductsEffect.Toast -> { /* Toast handling via scaffold */ }
            is ProductsEffect.NavigateToCreate -> onBackClick()
            is ProductsEffect.NavigateToDetail -> onNavigateDetail(effect.id)
            is ProductsEffect.OpenDetails -> onNavigateDetail(effect.item.id)
            is ProductsEffect.OpenEdit -> onBackClick()
            is ProductsEffect.ConvertToPi -> onBackClick()
        }
    }

    ProductsScreen(
        state = state,
        onBackClick = onBackClick,
        onAction = viewModel::onAction
    )
}
