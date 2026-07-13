package com.havos.lubricerp.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class ResultStateTest {

    @Test
    fun `Loading is a singleton`() {
        val a = ResultState.Loading
        val b = ResultState.Loading
        assertSame(a, b)
    }

    @Test
    fun `Success holds data correctly`() {
        val data = "Hello"
        val success = ResultState.Success(data)
        assertEquals("Hello", success.data)
    }

    @Test
    fun `Success with null data is allowed`() {
        val success = ResultState.Success(null)
        assertNull(success.data)
    }

    @Test
    fun `Success with complex data types`() {
        val items = listOf(1, 2, 3)
        val success = ResultState.Success(items)
        assertEquals(listOf(1, 2, 3), success.data)
        assertEquals(3, success.data.size)
    }

    @Test
    fun `Error holds message only`() {
        val error = ResultState.Error("Something went wrong")
        assertEquals("Something went wrong", error.message)
        assertNull(error.cause)
        assertNull(error.networkErrorKind)
    }

    @Test
    fun `Error holds message and cause`() {
        val cause = RuntimeException("Root cause")
        val error = ResultState.Error("Failure", cause = cause)
        assertEquals("Failure", error.message)
        assertEquals(cause, error.cause)
    }

    @Test
    fun `Error holds message and networkErrorKind`() {
        val error = ResultState.Error("Network down", networkErrorKind = NetworkErrorKind.OFFLINE)
        assertEquals("Network down", error.message)
        assertEquals(NetworkErrorKind.OFFLINE, error.networkErrorKind)
    }

    @Test
    fun `Error holds message, cause, and networkErrorKind`() {
        val cause = RuntimeException("Timeout")
        val error = ResultState.Error(
            message = "Request timed out",
            cause = cause,
            networkErrorKind = NetworkErrorKind.TIMEOUT
        )

        assertEquals("Request timed out", error.message)
        assertEquals(cause, error.cause)
        assertEquals(NetworkErrorKind.TIMEOUT, error.networkErrorKind)
    }

    @Test
    fun `when expression covers all ResultState variants`() {
        val states: List<ResultState<String>> = listOf(
            ResultState.Loading,
            ResultState.Success("data"),
            ResultState.Error("error")
        )

        for (state in states) {
            when (state) {
                is ResultState.Loading -> { }
                is ResultState.Success -> assertEquals("data", state.data)
                is ResultState.Error -> assertFalse(state.message.isEmpty())
            }
        }
    }

    @Test
    fun `Error extends ResultState Nothing allowing any type usage`() {
        val error = ResultState.Error("test")
        val stringState: ResultState<String> = error
        val intState: ResultState<Int> = error

        // Both assignments are valid because Error extends ResultState<Nothing>
        assertEquals("test", (stringState as ResultState.Error).message)
        assertEquals("test", (intState as ResultState.Error).message)
    }

    @Test
    fun `Success equality works for same data`() {
        val a = ResultState.Success("test")
        val b = ResultState.Success("test")
        assertEquals(a, b)
    }

    @Test
    fun `Error equality works for same message`() {
        val a = ResultState.Error("test")
        val b = ResultState.Error("test")
        assertEquals(a, b)
    }

    @Test
    fun `Error equality considers cause`() {
        val cause = RuntimeException("cause")
        val a = ResultState.Error("test", cause = cause)
        val b = ResultState.Error("test", cause = cause)
        val c = ResultState.Error("test", cause = null)

        assertEquals(a, b)
        assertFalse(a == c)
    }
}
