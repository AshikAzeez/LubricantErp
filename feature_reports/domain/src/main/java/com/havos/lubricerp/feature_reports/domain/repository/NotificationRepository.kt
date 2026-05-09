package com.havos.lubricerp.feature_reports.domain.repository

import androidx.paging.PagingData
import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.feature_reports.domain.model.NotificationItem
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getNotificationsPaged(): Flow<PagingData<NotificationItem>>
    suspend fun getUnreadCount(): ResultState<Int>
    suspend fun markAsRead(id: Long): ResultState<Unit>
    suspend fun markAllAsRead(): ResultState<Unit>
}
