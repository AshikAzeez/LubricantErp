package com.havos.lubricerp.core.network

import android.content.Context
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf

class MockAssetResponseProvider(
    private val context: Context
) {

    fun MockRequestHandleScope.respondFor(request: HttpRequestData) = when {
        request.method == HttpMethod.Post && normalizedPath(request).contains("auth/login") -> {
            respondAsset(this, "mock/auth/login_success.json")
        }

        request.method == HttpMethod.Post && normalizedPath(request).contains("auth/logout") -> {
            respondAsset(this, "mock/auth/logout_success.json")
        }

        request.method == HttpMethod.Get && normalizedPath(request).contains("reports/tank-stock-summary") -> {
            respondAsset(this, "mock/reports/tank_stock_summary.json")
        }

        request.method == HttpMethod.Get && normalizedPath(request).contains("reports/raw-material-stock") -> {
            respondAsset(this, "mock/reports/raw_material_stock.json")
        }

        request.method == HttpMethod.Get && normalizedPath(request).contains("reports/packaging-loss-gain") -> {
            respondAsset(this, "mock/reports/packaging_loss_gain.json")
        }

        else -> this.respond(
            content = "{\"error\":\"Mock route not found\",\"path\":\"${normalizedPath(request)}\",\"method\":\"${request.method.value}\"}",
            status = HttpStatusCode.NotFound,
            headers = jsonHeaders
        )
    }

    private fun respondAsset(scope: MockRequestHandleScope, path: String) = run {
        val json = context.assets.open(path).bufferedReader().use { it.readText() }
        scope.respond(
            content = json,
            status = HttpStatusCode.OK,
            headers = jsonHeaders
        )
    }

    private companion object {
        val jsonHeaders = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
    }

    private fun normalizedPath(request: HttpRequestData): String {
        return request.url.encodedPath.trim('/')
    }
}
