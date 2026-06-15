package com.havos.lubricerp.feature_reports.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class NotificationItemDto(
    val id: Long = 0L,
    val title: String = "",
    val message: String = "",
    val type: String = "",
    val linkUrl: String = "",
    val isRead: Boolean = false,
    val readAt: String? = null,
    val createdAt: String = "",
    val timeAgo: String = ""
)

@Serializable
data class NotificationPageDataDto(
    val unreadCount: Int = 0,
    val items: List<NotificationItemDto> = emptyList()
)

@Serializable
data class NotificationPageApiResponseDto(
    val success: Boolean = false,
    val data: NotificationPageDataDto? = null,
    val message: String? = null,
    val errors: List<String>? = null
)

@Serializable
data class UnreadCountDataDto(
    val unreadCount: Int = 0
)

@Serializable
data class UnreadCountApiResponseDto(
    val success: Boolean = false,
    val data: UnreadCountDataDto? = null,
    val message: String? = null,
    val errors: List<String>? = null
)

@Serializable
data class NotificationActionResponseDto(
    val success: Boolean = false,
    val message: String? = null,
    val errors: List<String>? = null
)
