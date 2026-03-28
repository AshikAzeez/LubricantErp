package com.havos.lubricerp.core.common

sealed interface ResultState<out T> {
    data object Loading : ResultState<Nothing>
    data class Success<T>(val data: T) : ResultState<T>
    data class Error(val message: String, val cause: Throwable? = null) : ResultState<Nothing>
}
