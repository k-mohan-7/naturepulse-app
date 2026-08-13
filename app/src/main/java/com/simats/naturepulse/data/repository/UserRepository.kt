package com.simats.naturepulse.data.repository

import com.simats.naturepulse.data.model.User
import com.simats.naturepulse.data.remote.ApiService
import com.simats.naturepulse.data.remote.ChangePasswordRequest
import com.simats.naturepulse.data.remote.UpdateProfileRequest

class UserRepository(private val api: ApiService) {

    suspend fun profile(): Result<User> = safeCall { api.profile() }

    suspend fun updateProfile(req: UpdateProfileRequest): Result<User> =
        safeCall { api.updateProfile(req) }

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> =
        safeCall { api.changePassword(ChangePasswordRequest(currentPassword, newPassword)) }
            .map { }
}
