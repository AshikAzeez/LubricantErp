package com.havos.lubricerp.core.network

interface SessionTokenProvider {
    suspend fun getAccessToken(): String?
    suspend fun getRefreshToken(): String?
    suspend fun refreshAndSave(): String?
    suspend fun clearSession()
}
