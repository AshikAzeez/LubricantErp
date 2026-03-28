package com.havos.lubricerp.core.network

import kotlinx.serialization.Serializable

@Serializable
data class AppConfigDto(
    val environment: String = "TEST",
    val useMockEngine: Boolean = true,
    val baseUrls: Map<String, String> = emptyMap()
)

data class ResolvedNetworkConfig(
    val environment: AppEnvironment,
    val useMockEngine: Boolean,
    val baseUrl: String
)
