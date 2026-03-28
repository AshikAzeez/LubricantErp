package com.havos.lubricerp.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
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
                            println("GoalERP-Mock(${networkConfig.environment}) -> $message")
                        }
                    }
                    level = LogLevel.BODY
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
                            println("GoalERP-Network(${networkConfig.environment}) -> $message")
                        }
                    }
                    level = LogLevel.BODY
                }
                install(ContentNegotiation) {
                    json(json)
                }
            }
        }
    }
}
