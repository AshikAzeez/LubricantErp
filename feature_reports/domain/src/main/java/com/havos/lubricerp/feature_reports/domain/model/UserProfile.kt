package com.havos.lubricerp.feature_reports.domain.model

data class UserProfile(
    val id: Long,
    val email: String,
    val fullName: String,
    val branchId: Long,
    val roles: List<String>
)
