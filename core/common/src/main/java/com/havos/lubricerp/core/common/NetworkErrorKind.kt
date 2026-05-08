package com.havos.lubricerp.core.common

enum class NetworkErrorKind {
    OFFLINE,
    TIMEOUT,
    SERVER_ERROR,
    AUTH_ERROR,
    UNKNOWN;

    val isRetryable: Boolean get() = this == TIMEOUT || this == SERVER_ERROR || this == UNKNOWN
}

val ResultState.Error.isOffline: Boolean
    get() = networkErrorKind == NetworkErrorKind.OFFLINE

val ResultState.Error.isTimeout: Boolean
    get() = networkErrorKind == NetworkErrorKind.TIMEOUT

val ResultState.Error.isRetryable: Boolean
    get() = networkErrorKind?.isRetryable == true
