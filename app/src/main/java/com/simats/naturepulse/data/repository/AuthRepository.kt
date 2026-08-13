package com.simats.naturepulse.data.repository

import com.simats.naturepulse.data.model.User
import com.simats.naturepulse.data.remote.ApiService
import com.simats.naturepulse.data.remote.LoginRequest
import com.simats.naturepulse.data.remote.RegisterRequest
import retrofit2.Response

class AuthRepository(private val api: ApiService) {

    suspend fun login(email: String, password: String): Result<Pair<String, User>> {
        return safeCall { api.login(LoginRequest(email, password)) }
            .map { Pair(it.token, it.user) }
    }

    suspend fun register(name: String, email: String, password: String): Result<Pair<String, User>> {
        return safeCall { api.register(RegisterRequest(name, email, password)) }
            .map { Pair(it.token, it.user) }
    }

    suspend fun logout(): Result<Unit> {
        return safeCall { api.logout() }.map { }
    }

    suspend fun me(): Result<User> {
        return safeCall { api.me() }
    }
}
