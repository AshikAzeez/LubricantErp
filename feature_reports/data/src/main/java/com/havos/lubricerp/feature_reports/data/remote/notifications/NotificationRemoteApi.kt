package com.havos.lubricerp.feature_reports.data.remote.notifications

import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.core.network.safeApiCall
import com.havos.lubricerp.feature_reports.data.dto.NotificationActionResponseDto
import com.havos.lubricerp.feature_reports.data.dto.NotificationPageApiResponseDto
import com.havos.lubricerp.feature_reports.data.dto.NotificationPageDataDto
import com.havos.lubricerp.feature_reports.data.dto.UnreadCountApiResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post

class NotificationRemoteApi(
    private val client: HttpClient
) : NotificationRemoteDataSource {

    override suspend fun getNotifications(page: Int, pageSize: Int): ResultState<NotificationPageDataDto> {
        return when (
            val result = safeApiCall<NotificationPageApiResponseDto> {
                client.get("api/notifications") {
                    parameter("page", page)
                    parameter("pageSize", pageSize)
                }
            }
        ) {
            is ResultState.Success -> {
                val payload = result.data
                if (!payload.success || payload.data == null) {
                    ResultState.Error(payload.message ?: "Unable to fetch notifications")
                } else {
                    ResultState.Success(payload.data)
                }
            }
            is ResultState.Error -> ResultState.Error(result.message)
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getUnreadCount(): ResultState<Int> {
        return when (
            val result = safeApiCall<UnreadCountApiResponseDto> {
                client.get("api/notifications/unread-count")
            }
        ) {
            is ResultState.Success -> {
                val payload = result.data
                if (!payload.success || payload.data == null) {
                    ResultState.Error(payload.message ?: "Unable to fetch unread count")
                } else {
                    ResultState.Success(payload.data.unreadCount)
                }
            }
            is ResultState.Error -> ResultState.Error(result.message)
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun markAsRead(id: Long): ResultState<Unit> {
        return when (
            val result = safeApiCall<NotificationActionResponseDto> {
                client.post("api/notifications/$id/read")
            }
        ) {
            is ResultState.Success -> {
                if (!result.data.success) {
                    ResultState.Error(result.data.message ?: "Failed to mark as read")
                } else {
                    ResultState.Success(Unit)
                }
            }
            is ResultState.Error -> ResultState.Error(result.message)
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun markAllAsRead(): ResultState<Unit> {
        return when (
            val result = safeApiCall<NotificationActionResponseDto> {
                client.post("api/notifications/read-all")
            }
        ) {
            is ResultState.Success -> {
                if (!result.data.success) {
                    ResultState.Error(result.data.message ?: "Failed to mark all as read")
                } else {
                    ResultState.Success(Unit)
                }
            }
            is ResultState.Error -> ResultState.Error(result.message)
            ResultState.Loading -> ResultState.Loading
        }
    }
}
