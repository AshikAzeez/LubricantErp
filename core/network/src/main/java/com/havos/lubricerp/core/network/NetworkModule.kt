package com.havos.lubricerp.core.network

import android.content.pm.ApplicationInfo
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.encodedPath
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import okhttp3.ConnectionSpec
import okhttp3.TlsVersion
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreNetworkModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
        }
    }

    single { MockAssetResponseProvider(androidContext()) }
    single { NetworkMonitor(androidContext()) }

    single {
        val networkConfig = get<ResolvedNetworkConfig>()
        val json = get<Json>()
        val koinScope = this
        val isDebuggableApp =
            (androidContext().applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        val enableVerboseLogs = isDebuggableApp /*&& networkConfig.environment != AppEnvironment.PRODUCTION*/
        val logLevel = if (enableVerboseLogs) LogLevel.ALL else LogLevel.NONE
        val logTag = if (networkConfig.useMockEngine) {
            "GoalERP-Mock(${networkConfig.environment})"
        } else {
            "GoalERP-Network(${networkConfig.environment})"
        }

        fun io.ktor.client.HttpClientConfig<*>.installAuth() {
            install(Auth) {
                bearer {
                    loadTokens {
                        val tp = koinScope.getOrNull<SessionTokenProvider>() ?: return@loadTokens null
                        val access = tp.getAccessToken()
                        val refresh = tp.getRefreshToken().orEmpty()
                        if (access != null) BearerTokens(access, refresh) else null
                    }
                    refreshTokens {
                        val tp = koinScope.getOrNull<SessionTokenProvider>() ?: return@refreshTokens null
                        // First try returning the current stored token (may have been
                        // saved by a recent login and the Auth plugin cache is just stale).
                        val currentAccess = tp.getAccessToken()
                        val currentRefresh = tp.getRefreshToken().orEmpty()
                        if (currentAccess != null && oldTokens?.accessToken != currentAccess) {
                            return@refreshTokens BearerTokens(currentAccess, currentRefresh)
                        }
                        // Current token matches what was already sent → truly expired,
                        // attempt a server-side refresh.
                        val newAccess = tp.refreshAndSave()
                        if (newAccess == null) {
                            tp.clearSession()
                            Handler(Looper.getMainLooper()).post {
                                Toast.makeText(
                                    androidContext(),
                                    "Session expired. Please log in again.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            return@refreshTokens null
                        }
                        val newRefresh = tp.getRefreshToken().orEmpty()
                        BearerTokens(newAccess, newRefresh)
                    }
                    sendWithoutRequest { request ->
                        val path = request.url.encodedPath
                        !path.contains("api/auth/login") &&
                            !path.contains("api/auth/refresh")
                    }
                }
            }
        }

        if (networkConfig.useMockEngine) {
            val mockAssetResponseProvider = get<MockAssetResponseProvider>()
            HttpClient(MockEngine) {
                engine {
                    addHandler { request ->
                        with(mockAssetResponseProvider) {
                            respondFor(request)
                        }
                    }
                }
                defaultRequest {
                    url(networkConfig.baseUrl)
                }
                install(Logging) {
                    logger = object : Logger {
                        override fun log(message: String) {
                            Log.d(logTag, message)
                        }
                    }
                    level = logLevel
                }
                install(HttpTimeout) {
                    requestTimeoutMillis = 15_000
                    connectTimeoutMillis = 10_000
                    socketTimeoutMillis = 15_000
                }
                install(HttpRequestRetry) {
                    maxRetries = 2
                    retryIf { _, response ->
                        response.status.value in 500..599 || response.status.value == 429
                    }
                    retryOnExceptionIf { _, cause ->
                        cause is io.ktor.client.plugins.HttpRequestTimeoutException ||
                        cause is io.ktor.client.network.sockets.ConnectTimeoutException ||
                        cause is io.ktor.client.network.sockets.SocketTimeoutException
                    }
                    exponentialDelay()
                }
                install(ContentNegotiation) {
                    json(json)
                }
                installAuth()
            }
        } else {
            HttpClient(OkHttp) {
                engine {
                    // Enforce TLS 1.2+ for HTTPS; allow cleartext for HTTP endpoints
                    config {
                        connectionSpecs(
                            listOf(
                                ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                                    .tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_3)
                                    .build(),
                                ConnectionSpec.CLEARTEXT
                            )
                        )
                    }
                }
                defaultRequest {
                    url(networkConfig.baseUrl)
                }
                install(Logging) {
                    logger = object : Logger {
                        override fun log(message: String) {
                            Log.d(logTag, message)
                        }
                    }
                    level = logLevel
                }
                install(HttpTimeout) {
                    requestTimeoutMillis = 15_000
                    connectTimeoutMillis = 10_000
                    socketTimeoutMillis = 15_000
                }
                install(HttpRequestRetry) {
                    maxRetries = 2
                    retryIf { _, response ->
                        response.status.value in 500..599 || response.status.value == 429
                    }
                    retryOnExceptionIf { _, cause ->
                        cause is io.ktor.client.plugins.HttpRequestTimeoutException ||
                        cause is io.ktor.client.network.sockets.ConnectTimeoutException ||
                        cause is io.ktor.client.network.sockets.SocketTimeoutException
                    }
                    exponentialDelay()
                }
                install(ContentNegotiation) {
                    json(json)
                }
                installAuth()
            }
        }
    }
}
