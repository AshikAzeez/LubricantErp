package com.havos.lubricerp.feature_reports.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    val username: String,
    val password: String
)

@Serializable
data class LoginResponseDto(
    val username: String,
    val token: String
)

@Serializable
data class LogoutResponseDto(
    val success: Boolean,
    val message: String
)
