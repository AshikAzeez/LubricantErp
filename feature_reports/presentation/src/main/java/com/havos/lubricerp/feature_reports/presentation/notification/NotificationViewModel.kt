package com.havos.lubricerp.feature_reports.presentation.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.feature_reports.domain.model.NotificationItem
import com.havos.lubricerp.feature_reports.domain.usecase.GetNotificationsPagedUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.GetUnreadNotificationCountUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.MarkAllNotificationsReadUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.MarkNotificationAsReadUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val getNotificationsPagedUseCase: GetNotificationsPagedUseCase,
    private val getUnreadCountUseCase: GetUnreadNotificationCountUseCase,
    private val markAsReadUseCase: MarkNotificationAsReadUseCase,
    private val markAllReadUseCase: MarkAllNotificationsReadUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationUiState())
    val state: StateFlow<NotificationUiState> = _state.asStateFlow()

    private val _effect = Channel<NotificationEffect>(Channel.BUFFERED)
    val effect: Flow<NotificationEffect> = _effect.receiveAsFlow()

    val notificationsPaged: Flow<PagingData<NotificationItem>> =
        getNotificationsPagedUseCase().cachedIn(viewModelScope)

    init {
        onIntent(NotificationIntent.LoadUnreadCount)
    }

    fun onIntent(intent: NotificationIntent) {
        when (intent) {
            is NotificationIntent.LoadUnreadCount -> loadUnreadCount()
            is NotificationIntent.MarkAsRead -> markAsRead(intent.id)
            is NotificationIntent.MarkAllAsRead -> markAllAsRead()
            is NotificationIntent.Refresh -> loadUnreadCount()
            is NotificationIntent.FilterToggled -> {
                val current = _state.value.selectedTypes
                val updated = if (intent.type in current) current - intent.type else current + intent.type
                _state.value = _state.value.copy(selectedTypes = updated)
            }
        }
    }

    private fun loadUnreadCount() {
        viewModelScope.launch {
            when (val result = getUnreadCountUseCase()) {
                is ResultState.Success -> _state.value = _state.value.copy(unreadCount = result.data)
                is ResultState.Error -> { /* badge not critical, ignore silently */ }
                ResultState.Loading -> Unit
            }
        }
    }

    private fun markAsRead(id: Long) {
        viewModelScope.launch {
            when (val result = markAsReadUseCase(id)) {
                is ResultState.Success -> loadUnreadCount()
                is ResultState.Error -> _effect.send(NotificationEffect.ShowError(result.message))
                ResultState.Loading -> Unit
            }
        }
    }

    private fun markAllAsRead() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isMarkingAllRead = true)
            when (val result = markAllReadUseCase()) {
                is ResultState.Success -> {
                    _state.value = _state.value.copy(isMarkingAllRead = false, unreadCount = 0)
                    _effect.send(NotificationEffect.MarkedAllRead)
                }
                is ResultState.Error -> {
                    _state.value = _state.value.copy(isMarkingAllRead = false)
                    _effect.send(NotificationEffect.ShowError(result.message))
                }
                ResultState.Loading -> Unit
            }
        }
    }
}
