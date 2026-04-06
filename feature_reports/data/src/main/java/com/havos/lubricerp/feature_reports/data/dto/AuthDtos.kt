package com.havos.lubricerp.feature_reports.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String
)

@Serializable
data class LoginResponseDto(
    val username: String,
    val token: String,
    val expiry: String? = null
)

@Serializable
data class LoginApiResponseDto(
    val success: Boolean,
    val data: LoginApiDataDto? = null,
    val message: String? = null,
    val errors: List<String>? = null
)

@Serializable
data class LoginApiDataDto(
    val token: String,
    val expiry: String? = null,
    val user: LoginApiUserDto? = null
)

@Serializable
data class LoginApiUserDto(
    val id: Long? = null,
    val email: String? = null,
    val fullName: String? = null,
    val branchId: Long? = null,
    val roles: List<String> = emptyList()
)

@Serializable
data class LogoutResponseDto(
    val success: Boolean,
    val message: String
)
