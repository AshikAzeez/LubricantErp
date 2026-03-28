package com.havos.lubricerp.core.network

import android.content.Context
import android.content.pm.ApplicationInfo
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class AppConfigDataSource(
    private val context: Context,
    private val json: Json
) {
    fun load(): AppConfigDto {
        val configText = runCatching {
            context.assets.open(CONFIG_ASSET_PATH).bufferedReader().use { it.readText() }
        }.getOrNull()

        return runCatching {
            if (configText.isNullOrBlank()) AppConfigDto() else json.decodeFromString<AppConfigDto>(configText)
        }.getOrDefault(AppConfigDto())
    }

    private companion object {
        const val CONFIG_ASSET_PATH = "config/app_config.json"
    }
}

class NetworkConfigResolver(
    private val context: Context,
    private val appConfigDataSource: AppConfigDataSource
) {
    fun resolve(): ResolvedNetworkConfig {
        val raw = appConfigDataSource.load()
        val isDebuggableApp = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

        val productionUrl = raw.baseUrls["production"] ?: DEFAULT_PRODUCTION_URL
        if (!isDebuggableApp) {
            return ResolvedNetworkConfig(
                environment = AppEnvironment.PRODUCTION,
                useMockEngine = false,
                baseUrl = productionUrl
            )
        }

        val environment = AppEnvironment.from(raw.environment)
        val key = when (environment) {
            AppEnvironment.TEST -> "test"
            AppEnvironment.STAGE -> "stage"
            AppEnvironment.PRODUCTION -> "production"
        }

        val baseUrl = raw.baseUrls[key] ?: when (environment) {
            AppEnvironment.TEST -> DEFAULT_TEST_URL
            AppEnvironment.STAGE -> DEFAULT_STAGE_URL
            AppEnvironment.PRODUCTION -> DEFAULT_PRODUCTION_URL
        }

        return ResolvedNetworkConfig(
            environment = environment,
            useMockEngine = raw.useMockEngine,
            baseUrl = baseUrl
        )
    }

    private companion object {
        const val DEFAULT_TEST_URL = "https://test.goal-erp.local/"
        const val DEFAULT_STAGE_URL = "https://stage.goal-erp.local/"
        const val DEFAULT_PRODUCTION_URL = "https://api.goal-erp.com/"
    }
}
