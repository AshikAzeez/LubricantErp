package com.havos.lubricerp.core.network

import com.havos.lubricerp.core.common.ResultState
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse

suspend inline fun <reified T> safeApiCall(
    crossinline block: suspend () -> HttpResponse
): ResultState<T> {
    return runCatching {
        val response = block()
        if (response.status.value in 200..299) {
            ResultState.Success(response.body<T>())
        } else {
            ResultState.Error("Request failed with code ${response.status.value}")
        }
    }.getOrElse { throwable ->
        ResultState.Error(throwable.message ?: "Unexpected network error", throwable)
    }
}
