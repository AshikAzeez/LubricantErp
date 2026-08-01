package com.havos.lubricerp.core.network

import com.havos.lubricerp.core.common.NetworkErrorKind
import com.havos.lubricerp.core.common.ResultState
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiResultTest {

    @Serializable
    data class TestBody(val name: String, val value: Int)

    private val testJson = Json { ignoreUnknownKeys = true }

    private fun createClient(
        status: HttpStatusCode,
        body: String,
        contentType: ContentType = ContentType.Application.Json
    ): HttpClient {
        val engine = MockEngine {
            respond(
                content = ByteReadChannel(body),
                status = status,
                headers = headersOf(HttpHeaders.ContentType, contentType.toString())
            )
        }
        return HttpClient(engine) {
            install(ContentNegotiation) {
                json(testJson)
            }
        }
    }

    @Test
    fun `returns Success for 200 response with valid JSON`() = runTest {
        val client = createClient(
            HttpStatusCode.OK,
            """{"name":"test","value":42}"""
        )

        val result = safeApiCall<TestBody> { client.get("https://test.com/api") }

        assertTrue(result is ResultState.Success)
        val data = (result as ResultState.Success).data
        assertEquals("test", data.name)
        assertEquals(42, data.value)
    }

    @Test
    fun `returns Success for 201 Created response`() = runTest {
        val client = createClient(
            HttpStatusCode.Created,
            """{"name":"created","value":100}"""
        )

        val result = safeApiCall<TestBody> { client.get("https://test.com/api") }

        assertTrue(result is ResultState.Success)
        assertEquals("created", (result as ResultState.Success).data.name)
    }

    @Test
    fun `returns Error with SERVER_ERROR for 500 response`() = runTest {
        val client = createClient(
            HttpStatusCode.InternalServerError,
            """{"message":"Internal error occurred"}"""
        )

        val result = safeApiCall<TestBody> { client.get("https://test.com/api") }

        assertTrue(result is ResultState.Error)
        val error = result as ResultState.Error
        assertEquals("Internal error occurred", error.message)
        assertEquals(NetworkErrorKind.SERVER_ERROR, error.networkErrorKind)
    }

    @Test
    fun `returns Error with SERVER_ERROR for 502 Bad Gateway`() = runTest {
        val client = createClient(
            HttpStatusCode(502, "Bad Gateway"),
            """{"message":"Bad gateway"}"""
        )

        val result = safeApiCall<TestBody> { client.get("https://test.com/api") }

        assertTrue(result is ResultState.Error)
        assertEquals(NetworkErrorKind.SERVER_ERROR, (result as ResultState.Error).networkErrorKind)
    }

    @Test
    fun `returns Error with AUTH_ERROR for 401 response`() = runTest {
        val client = createClient(
            HttpStatusCode.Unauthorized,
            """{"message":"Unauthorized"}"""
        )

        val result = safeApiCall<TestBody> { client.get("https://test.com/api") }

        assertTrue(result is ResultState.Error)
        val error = result as ResultState.Error
        assertEquals("Unauthorized", error.message)
        assertEquals(NetworkErrorKind.AUTH_ERROR, error.networkErrorKind)
    }

    @Test
    fun `returns Error with AUTH_ERROR for 403 response`() = runTest {
        val client = createClient(
            HttpStatusCode.Forbidden,
            """{"message":"Forbidden"}"""
        )

        val result = safeApiCall<TestBody> { client.get("https://test.com/api") }

        assertTrue(result is ResultState.Error)
        assertEquals(NetworkErrorKind.AUTH_ERROR, (result as ResultState.Error).networkErrorKind)
    }

    @Test
    fun `returns Error with UNKNOWN for 404 response`() = runTest {
        val client = createClient(
            HttpStatusCode.NotFound,
            """{"message":"Not found"}"""
        )

        val result = safeApiCall<TestBody> { client.get("https://test.com/api") }

        assertTrue(result is ResultState.Error)
        assertEquals(NetworkErrorKind.UNKNOWN, (result as ResultState.Error).networkErrorKind)
    }

    @Test
    fun `returns Error with UNKNOWN for 422 response`() = runTest {
        val client = createClient(
            HttpStatusCode(422, "Unprocessable Entity"),
            """{"message":"Validation failed"}"""
        )

        val result = safeApiCall<TestBody> { client.get("https://test.com/api") }

        assertTrue(result is ResultState.Error)
        assertEquals(NetworkErrorKind.UNKNOWN, (result as ResultState.Error).networkErrorKind)
    }

    @Test
    fun `parses message from JSON error body`() = runTest {
        val client = createClient(
            HttpStatusCode.BadRequest,
            """{"message":"Invalid request parameters"}"""
        )

        val result = safeApiCall<TestBody> { client.get("https://test.com/api") }

        assertEquals(
            "Invalid request parameters",
            (result as ResultState.Error).message
        )
    }

    @Test
    fun `falls back to status code message when error body is not JSON`() = runTest {
        val client = createClient(
            HttpStatusCode.InternalServerError,
            "plain text error",
            ContentType.Text.Plain
        )

        val result = safeApiCall<TestBody> { client.get("https://test.com/api") }

        assertTrue((result as ResultState.Error).message.contains("500"))
    }

    @Test
    fun `falls back to status code message when error body has no message field`() = runTest {
        val client = createClient(
            HttpStatusCode.BadRequest,
            """{"error":"bad request"}"""
        )

        val result = safeApiCall<TestBody> { client.get("https://test.com/api") }

        assertTrue((result as ResultState.Error).message.contains("400"))
    }

    @Test
    fun `returns OFFLINE error for IOException`() = runTest {
        val result = safeApiCall<TestBody> {
            throw IOException("Network is unreachable")
        }

        assertTrue(result is ResultState.Error)
        val error = result as ResultState.Error
        assertEquals("Network is unreachable", error.message)
        assertEquals(NetworkErrorKind.OFFLINE, error.networkErrorKind)
        assertNotNull(error.cause)
    }

    @Test
    fun `returns OFFLINE for unable to resolve host`() = runTest {
        val result = safeApiCall<TestBody> {
            throw IOException("unable to resolve host")
        }

        assertEquals(NetworkErrorKind.OFFLINE, (result as ResultState.Error).networkErrorKind)
    }

    @Test
    fun `returns OFFLINE for failure to connect`() = runTest {
        val result = safeApiCall<TestBody> {
            throw IOException("failed to connect to server")
        }

        assertEquals(NetworkErrorKind.OFFLINE, (result as ResultState.Error).networkErrorKind)
    }

    @Test
    fun `returns CONNECTION_ERROR for general IOException`() = runTest {
        val result = safeApiCall<TestBody> {
            throw IOException("Some IO error")
        }

        assertEquals(NetworkErrorKind.CONNECTION_ERROR, (result as ResultState.Error).networkErrorKind)
    }

    @Test
    fun `returns CONNECTION_ERROR for SocketException`() = runTest {
        val result = safeApiCall<TestBody> {
            throw java.net.SocketException("Connection reset")
        }

        assertEquals(NetworkErrorKind.CONNECTION_ERROR, (result as ResultState.Error).networkErrorKind)
    }

    @Test
    fun `returns TIMEOUT for ConnectTimeoutException`() = runTest {
        val result = safeApiCall<TestBody> {
            throw ConnectTimeoutException("Connection timed out", null)
        }

        assertEquals(NetworkErrorKind.TIMEOUT, (result as ResultState.Error).networkErrorKind)
    }

    @Test
    fun `returns TIMEOUT for SocketTimeoutException`() = runTest {
        val result = safeApiCall<TestBody> {
            throw SocketTimeoutException("Socket timed out", null)
        }

        assertEquals(NetworkErrorKind.TIMEOUT, (result as ResultState.Error).networkErrorKind)
    }

    @Test
    fun `returns UNKNOWN for unexpected exception`() = runTest {
        val result = safeApiCall<TestBody> {
            throw IllegalStateException("Unexpected state")
        }

        assertTrue(result is ResultState.Error)
        val error = result as ResultState.Error
        assertEquals("Unexpected state", error.message)
        assertEquals(NetworkErrorKind.UNKNOWN, error.networkErrorKind)
        assertNotNull(error.cause)
    }

    @Test
    fun `returns UNKNOWN with default message when exception has null message`() = runTest {
        val result = safeApiCall<TestBody> {
            throw RuntimeException()
        }

        assertTrue(result is ResultState.Error)
        val error = result as ResultState.Error
        assertEquals("Unexpected network error", error.message)
        assertEquals(NetworkErrorKind.UNKNOWN, error.networkErrorKind)
    }

    @Test
    fun `Error cause is set correctly`() = runTest {
        val cause = IOException("Connection lost")
        val result = safeApiCall<TestBody> { throw cause }

        assertEquals(cause, (result as ResultState.Error).cause)
    }

    @Test
    fun `Success has no cause or networkErrorKind`() = runTest {
        val client = createClient(
            HttpStatusCode.OK,
            """{"name":"ok","value":1}"""
        )

        val result = safeApiCall<TestBody> { client.get("https://test.com/api") }

        val success = result as ResultState.Success
        assertEquals("ok", success.data.name)
    }

    @Test
    fun `Error from HTTP response has null cause but has networkErrorKind`() = runTest {
        val client = createClient(
            HttpStatusCode.InternalServerError,
            """{"message":"Server error"}"""
        )

        val result = safeApiCall<TestBody> { client.get("https://test.com/api") }

        assertNull((result as ResultState.Error).cause)
        assertNotNull((result as ResultState.Error).networkErrorKind)
    }
}
