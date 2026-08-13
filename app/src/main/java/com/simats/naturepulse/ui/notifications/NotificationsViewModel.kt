package com.simats.naturepulse.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.simats.naturepulse.data.model.AppNotification
import com.simats.naturepulse.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class NotificationsUiState(
    val items: List<AppNotification> = emptyList(),
    val unread: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)

class NotificationsViewModel(private val repo: NotificationRepository) : ViewModel() {

    private val _state = MutableStateFlow(NotificationsUiState())
    val state: StateFlow<NotificationsUiState> = _state

    fun load() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            repo.list()
                .onSuccess { data ->
                    _state.value = _state.value.copy(
                        items = data.items,
                        unread = data.unread,
                        isLoading = false
                    )
                }
                .onFailure { _state.value = _state.value.copy(error = it.message, isLoading = false) }
        }
    }

    fun markRead(id: Int) {
        viewModelScope.launch {
            repo.markRead(id)
            _state.value = _state.value.copy(
                items = _state.value.items.map {
                    if (it.id == id) it.copy(isRead = true) else it
                },
                unread = maxOf(0, _state.value.unread - 1)
            )
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            repo.markRead(null)
            _state.value = _state.value.copy(
                items = _state.value.items.map { it.copy(isRead = true) },
                unread = 0
            )
        }
    }

    fun getUnreadCount(): Int = _state.value.unread

    class Factory(private val repo: NotificationRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = NotificationsViewModel(repo) as T
    }
}
