package com.havos.lubricerp.core.common

interface UiIntent

interface UiState

fun <S : UiState> S.reduce(reducer: S.() -> S): S = reducer()
