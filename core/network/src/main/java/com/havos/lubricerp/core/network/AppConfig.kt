package com.havos.lubricerp.core.network

data class ResolvedNetworkConfig(
    val environment: AppEnvironment,
    val useMockEngine: Boolean,
    val baseUrl: String
)
