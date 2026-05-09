package com.havos.lubricerp.feature_reports.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class NotificationItemDto(
    val id: Long,
    val title: String,
    val message: String,
    val type: String,
    val linkUrl: String,
    val isRead: Boolean,
    val readAt: String? = null,
    val createdAt: String,
    val timeAgo: String
)

@Serializable
data class NotificationPageDataDto(
    val unreadCount: Int,
    val items: List<NotificationItemDto>
)

@Serializable
data class NotificationPageApiResponseDto(
    val success: Boolean,
    val data: NotificationPageDataDto? = null,
    val message: String? = null,
    val errors: List<String>? = null
)

@Serializable
data class UnreadCountDataDto(
    val unreadCount: Int
)

@Serializable
data class UnreadCountApiResponseDto(
    val success: Boolean,
    val data: UnreadCountDataDto? = null,
    val message: String? = null,
    val errors: List<String>? = null
)

@Serializable
data class NotificationActionResponseDto(
    val success: Boolean,
    val message: String? = null,
    val errors: List<String>? = null
)
