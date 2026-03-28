package com.havos.lubricerp.core.ui.components

import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object ThemeRevealTransitionBus {
    private val _originEvents = MutableSharedFlow<Offset?>(extraBufferCapacity = 1)
    val originEvents: SharedFlow<Offset?> = _originEvents.asSharedFlow()

    fun emitOrigin(origin: Offset?) {
        _originEvents.tryEmit(origin)
    }
}
