package com.havos.lubricerp.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

@Composable
fun <T> CollectEffect(
    effects: Flow<T>,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    onEffect: (T) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnEffect = rememberUpdatedState(onEffect)

    LaunchedEffect(effects, lifecycleOwner, minActiveState) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(minActiveState) {
            effects.collectLatest { effect ->
                currentOnEffect.value(effect)
            }
        }
    }
}
