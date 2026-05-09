package com.havos.lubricerp.feature_reports.data.remote.notifications

import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.feature_reports.data.dto.NotificationPageDataDto

interface NotificationRemoteDataSource {
    suspend fun getNotifications(page: Int, pageSize: Int): ResultState<NotificationPageDataDto>
    suspend fun getUnreadCount(): ResultState<Int>
    suspend fun markAsRead(id: Long): ResultState<Unit>
    suspend fun markAllAsRead(): ResultState<Unit>
}
