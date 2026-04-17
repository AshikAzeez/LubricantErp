package com.havos.lubricerp.feature_reports.data.remote.auth

import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.feature_reports.data.dto.LoginRequestDto
import com.havos.lubricerp.feature_reports.data.dto.LoginResponseDto
import com.havos.lubricerp.feature_reports.data.dto.ProfileDataDto

interface AuthRemoteDataSource {
    suspend fun login(request: LoginRequestDto): ResultState<LoginResponseDto>
    suspend fun logout(): ResultState<Unit>
    suspend fun getProfile(token: String): ResultState<ProfileDataDto>
}
