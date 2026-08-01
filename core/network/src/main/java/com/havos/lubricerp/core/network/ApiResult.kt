package com.havos.lubricerp.core.network

import com.havos.lubricerp.core.common.NetworkErrorKind
import com.havos.lubricerp.core.common.ResultState
import io.ktor.client.call.body
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.statement.HttpResponse
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.CancellationException

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

suspend inline fun <reified T> safeApiCall(
    crossinline block: suspend () -> HttpResponse
): ResultState<T> {
    return runCatching {
        val response = block()
        if (response.status.value in 200..299) {
            ResultState.Success(response.body<T>())
        } else {
            val errorBody = runCatching { response.body<String>() }.getOrNull()
            val parsedMessage = errorBody?.let { bodyStr ->
                runCatching {
                    val json = Json { ignoreUnknownKeys = true }
                    val element = json.parseToJsonElement(bodyStr)
                    element.jsonObject["message"]?.jsonPrimitive?.content
                }.getOrNull()
            }
            ResultState.Error(
                message = parsedMessage ?: "Request failed with code ${response.status.value}",
                networkErrorKind = when (response.status.value) {
                    in 500..599 -> NetworkErrorKind.SERVER_ERROR
                    401, 403    -> NetworkErrorKind.AUTH_ERROR
                    else        -> NetworkErrorKind.UNKNOWN
                }
            )
        }
    }.getOrElse { throwable ->
        // Cancellation must propagate — never convert it into an error state.
        if (throwable is CancellationException) throw throwable
        val msg = throwable.message?.lowercase() ?: ""
        val kind = when {
            throwable is HttpRequestTimeoutException ||
            throwable is ConnectTimeoutException ||
            throwable is SocketTimeoutException -> NetworkErrorKind.TIMEOUT
            // True connectivity loss: DNS failure or no route
            throwable is java.net.UnknownHostException ||
            "unable to resolve host" in msg ||
            "network is unreachable" in msg -> NetworkErrorKind.OFFLINE
            // Transient socket errors (connection reset, refused, broken pipe)
            throwable is java.net.SocketException ||
            "connection reset" in msg ||
            "connection refused" in msg ||
            "broken pipe" in msg -> NetworkErrorKind.CONNECTION_ERROR
            // Generic IO errors that mention connectivity
            "failed to connect" in msg -> NetworkErrorKind.OFFLINE
            throwable is IOException -> NetworkErrorKind.CONNECTION_ERROR
            else -> NetworkErrorKind.UNKNOWN
        }
        ResultState.Error(
            message = throwable.message ?: "Unexpected network error",
            cause = throwable,
            networkErrorKind = kind
        )
    }
}
