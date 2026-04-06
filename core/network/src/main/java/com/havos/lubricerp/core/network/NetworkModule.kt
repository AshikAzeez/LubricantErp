package com.havos.lubricerp.core.network

import android.content.pm.ApplicationInfo
import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreNetworkModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }

    single { AppConfigDataSource(androidContext(), get()) }
    single { NetworkConfigResolver(androidContext(), get()) }
    single { MockAssetResponseProvider(androidContext()) }
    single<ResolvedNetworkConfig> { get<NetworkConfigResolver>().resolve() }

    single {
        val networkConfig = get<ResolvedNetworkConfig>()
        val json = get<Json>()
        val isDebuggableApp =
            (androidContext().applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        val enableVerboseLogs = isDebuggableApp && networkConfig.environment != AppEnvironment.PRODUCTION
        val logLevel = if (enableVerboseLogs) LogLevel.BODY else LogLevel.NONE
        val logTag = if (networkConfig.useMockEngine) {
            "GoalERP-Mock(${networkConfig.environment})"
        } else {
            "GoalERP-Network(${networkConfig.environment})"
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
                    retryOnExceptionIf { _, _ -> true }
                    exponentialDelay()
                }
                install(ContentNegotiation) {
                    json(json)
                }
            }
        } else {
            HttpClient(OkHttp) {
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
                    retryOnExceptionIf { _, _ -> true }
                    exponentialDelay()
                }
                install(ContentNegotiation) {
                    json(json)
                }
            }
        }
    }
}
