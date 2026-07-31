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
fun ProductsRoute(
    onBackClick: () -> Unit,
    onNavigateDetail: (Long) -> Unit = {},
    onNavigateCreate: () -> Unit = {},
    onNavigateEdit: (Long) -> Unit = {},
    refreshTrigger: Boolean = false,
    viewModel: ProductsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.onIntent(ProductsIntent.Load)
    }

    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger) {
            viewModel.onIntent(ProductsIntent.Load)
        }
    }

    CollectEffect(effects = viewModel.effect) { effect ->
        when (effect) {
            is ProductsEffect.Toast ->
                Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
            is ProductsEffect.NavigateToCreate -> onNavigateCreate()
            is ProductsEffect.NavigateToDetail -> onNavigateDetail(effect.id)
            is ProductsEffect.OpenDetails -> onNavigateDetail(effect.item.id)
            is ProductsEffect.OpenEdit -> onNavigateEdit(effect.item.id)
            is ProductsEffect.ConvertToPi -> onBackClick()
        }
    }

    ProductsScreen(
        state = state,
        onBackClick = onBackClick,
        onAction = viewModel::onAction
    )
}
