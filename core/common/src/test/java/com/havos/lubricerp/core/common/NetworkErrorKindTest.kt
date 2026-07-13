package com.havos.lubricerp.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkErrorKindTest {

    @Test
    fun `OFFLINE is not retryable`() {
        assertFalse(NetworkErrorKind.OFFLINE.isRetryable)
    }

    @Test
    fun `TIMEOUT is retryable`() {
        assertTrue(NetworkErrorKind.TIMEOUT.isRetryable)
    }

    @Test
    fun `SERVER_ERROR is retryable`() {
        assertTrue(NetworkErrorKind.SERVER_ERROR.isRetryable)
    }

    @Test
    fun `AUTH_ERROR is not retryable`() {
        assertFalse(NetworkErrorKind.AUTH_ERROR.isRetryable)
    }

    @Test
    fun `UNKNOWN is retryable`() {
        assertTrue(NetworkErrorKind.UNKNOWN.isRetryable)
    }

    @Test
    fun `enum has exactly five entries`() {
        assertEquals(5, NetworkErrorKind.entries.size)
    }

    @Test
    fun `ResultState Error isOffline returns true when networkErrorKind is OFFLINE`() {
        val error = ResultState.Error("Offline", networkErrorKind = NetworkErrorKind.OFFLINE)
        assertTrue(error.isOffline)
    }

    @Test
    fun `ResultState Error isOffline returns false when networkErrorKind is not OFFLINE`() {
        val error = ResultState.Error("Error", networkErrorKind = NetworkErrorKind.TIMEOUT)
        assertFalse(error.isOffline)
    }

    @Test
    fun `ResultState Error isOffline returns false when networkErrorKind is null`() {
        val error = ResultState.Error("Generic error", networkErrorKind = null)
        assertFalse(error.isOffline)
    }

    @Test
    fun `ResultState Error isTimeout returns true when networkErrorKind is TIMEOUT`() {
        val error = ResultState.Error("Timeout", networkErrorKind = NetworkErrorKind.TIMEOUT)
        assertTrue(error.isTimeout)
    }

    @Test
    fun `ResultState Error isTimeout returns false when networkErrorKind is not TIMEOUT`() {
        val error = ResultState.Error("Error", networkErrorKind = NetworkErrorKind.OFFLINE)
        assertFalse(error.isTimeout)
    }

    @Test
    fun `ResultState Error isTimeout returns false when networkErrorKind is null`() {
        val error = ResultState.Error("Generic error", networkErrorKind = null)
        assertFalse(error.isTimeout)
    }

    @Test
    fun `ResultState Error isRetryable returns true for retryable errors`() {
        val timeout = ResultState.Error("Timeout", networkErrorKind = NetworkErrorKind.TIMEOUT)
        val serverError = ResultState.Error("Server", networkErrorKind = NetworkErrorKind.SERVER_ERROR)
        val unknown = ResultState.Error("Unknown", networkErrorKind = NetworkErrorKind.UNKNOWN)

        assertTrue(timeout.isRetryable)
        assertTrue(serverError.isRetryable)
        assertTrue(unknown.isRetryable)
    }

    @Test
    fun `ResultState Error isRetryable returns false for non-retryable errors`() {
        val offline = ResultState.Error("Offline", networkErrorKind = NetworkErrorKind.OFFLINE)
        val auth = ResultState.Error("Auth", networkErrorKind = NetworkErrorKind.AUTH_ERROR)

        assertFalse(offline.isRetryable)
        assertFalse(auth.isRetryable)
    }

    @Test
    fun `ResultState Error isRetryable returns false when networkErrorKind is null`() {
        val error = ResultState.Error("Generic error", networkErrorKind = null)
        assertFalse(error.isRetryable)
    }
}
