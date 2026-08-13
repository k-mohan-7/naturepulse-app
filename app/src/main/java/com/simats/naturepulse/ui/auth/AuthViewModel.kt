package com.simats.naturepulse.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.simats.naturepulse.core.datastore.PreferencesManager
import com.simats.naturepulse.data.model.User
import com.simats.naturepulse.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val user: User) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(
    private val repo: AuthRepository,
    private val prefs: PreferencesManager
) : ViewModel() {

    private val _state = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val state: StateFlow<AuthUiState> = _state

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = AuthUiState.Error("Please enter your email and password.")
            return
        }
        _state.value = AuthUiState.Loading
        viewModelScope.launch {
            repo.login(email.trim(), password)
                .onSuccess { (token, user) ->
                    prefs.saveToken(token)
                    prefs.saveUser(user)
                    _state.value = AuthUiState.Success(user)
                }
                .onFailure { _state.value = AuthUiState.Error(it.message ?: "Login failed") }
        }
    }

    fun register(name: String, email: String, password: String) {
        when {
            name.trim().length < 2 -> { _state.value = AuthUiState.Error("Please enter your full name."); return }
            !email.contains("@")   -> { _state.value = AuthUiState.Error("Please enter a valid email."); return }
            password.length < 6    -> { _state.value = AuthUiState.Error("Password must be at least 6 characters."); return }
        }
        _state.value = AuthUiState.Loading
        viewModelScope.launch {
            repo.register(name.trim(), email.trim(), password)
                .onSuccess { (token, user) ->
                    prefs.saveToken(token)
                    prefs.saveUser(user)
                    _state.value = AuthUiState.Success(user)
                }
                .onFailure { _state.value = AuthUiState.Error(it.message ?: "Registration failed") }
        }
    }

    fun resetState() { _state.value = AuthUiState.Idle }

    class Factory(private val repo: AuthRepository, private val prefs: PreferencesManager) :
        ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = AuthViewModel(repo, prefs) as T
    }
}
