package com.simats.naturepulse.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.simats.naturepulse.core.datastore.PreferencesManager
import com.simats.naturepulse.data.model.User
import com.simats.naturepulse.data.remote.UpdateProfileRequest
import com.simats.naturepulse.data.repository.AuthRepository
import com.simats.naturepulse.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class ProfileViewModel(
    private val userRepo: UserRepository,
    private val authRepo: AuthRepository,
    private val prefs: PreferencesManager
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state

    init {
        // Immediately collect cached user from PreferencesManager so currentUserId is populated on app launch
        viewModelScope.launch {
            prefs.userFlow.collect { savedUser ->
                if (savedUser != null) {
                    _state.value = _state.value.copy(user = savedUser, isLoading = false)
                }
            }
        }
        // Eagerly refresh latest user profile from server
        load()
    }

    fun load() {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            userRepo.profile()
                .onSuccess { user ->
                    prefs.saveUser(user)
                    _state.value = _state.value.copy(user = user, isLoading = false)
                }
                .onFailure { _state.value = _state.value.copy(error = it.message, isLoading = false) }
        }
    }

    fun updateProfile(
        name: String, phone: String, city: String, region: String,
        bio: String, avatarUrl: String, radiusKm: Float,
        notifyNearby: Boolean, notifyStatus: Boolean
    ) {
        if (name.trim().length < 2) {
            _state.value = _state.value.copy(error = "Name must be at least 2 characters")
            return
        }
        _state.value = _state.value.copy(isSaving = true, error = null)
        viewModelScope.launch {
            userRepo.updateProfile(
                UpdateProfileRequest(name.trim(), phone, city, region, bio, avatarUrl, radiusKm, notifyNearby, notifyStatus)
            ).onSuccess { user ->
                prefs.saveUser(user)
                _state.value = _state.value.copy(user = user, isSaving = false, successMessage = "Profile updated ✅")
            }.onFailure {
                _state.value = _state.value.copy(isSaving = false, error = it.message)
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        if (newPassword.length < 6) {
            _state.value = _state.value.copy(error = "New password must be at least 6 characters")
            return
        }
        _state.value = _state.value.copy(isSaving = true, error = null)
        viewModelScope.launch {
            userRepo.changePassword(currentPassword, newPassword)
                .onSuccess { _state.value = _state.value.copy(isSaving = false, successMessage = "Password updated ✅") }
                .onFailure { _state.value = _state.value.copy(isSaving = false, error = it.message) }
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            runCatching { authRepo.logout() }
            prefs.clear()
            onDone()
        }
    }

    fun clearMessages() { _state.value = _state.value.copy(error = null, successMessage = null) }

    class Factory(
        private val userRepo: UserRepository,
        private val authRepo: AuthRepository,
        private val prefs: PreferencesManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ProfileViewModel(userRepo, authRepo, prefs) as T
    }
}
