package com.simats.naturepulse.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.simats.naturepulse.data.model.*

// ─── Generic API wrapper ─────────────────────────────────────────────────────

/** Every backend response is wrapped: { success, message, data } */
@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    val success: Boolean = false,
    val message: String? = null,
    val data: T? = null
)

// ─── Auth ─────────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class LoginRequest(val email: String, val password: String)

@JsonClass(generateAdapter = true)
data class RegisterRequest(val name: String, val email: String, val password: String)

@JsonClass(generateAdapter = true)
data class AuthData(val token: String, val user: User)

// ─── User ─────────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class UpdateProfileRequest(
    val name: String,
    val phone: String = "",
    val city: String = "",
    val region: String = "",
    val bio: String = "",
    @Json(name = "avatar_url") val avatarUrl: String = "",
    @Json(name = "radius_km") val radiusKm: Float = 25f,
    @Json(name = "notify_nearby") val notifyNearby: Boolean = false,
    @Json(name = "notify_status") val notifyStatus: Boolean = false,
)

@JsonClass(generateAdapter = true)
data class ChangePasswordRequest(
    @Json(name = "current_password") val currentPassword: String,
    @Json(name = "new_password") val newPassword: String
)

// ─── Reports ──────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class UpdateReportRequest(
    val id: Int,
    val title: String,
    val description: String,
    val severity: String
)

@JsonClass(generateAdapter = true)
data class UpdateStatusRequest(
    val id: Int,
    val status: String,
    val note: String = ""
)

@JsonClass(generateAdapter = true)
data class FeedbackRequest(
    val id: Int,
    val type: String,          // "reaction" | "comment" | "reply"
    val message: String,
    @Json(name = "parent_id") val parentId: String? = null
)

// ─── Notifications ─────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class NotificationsData(
    val items: List<AppNotification> = emptyList(),
    val unread: Int = 0
)

@JsonClass(generateAdapter = true)
data class MarkReadRequest(val id: Int? = null)

// ─── Upload ───────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class UploadData(val path: String)

// ─── Feedback response ───────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class FeedbackData(
    val feedback: List<com.simats.naturepulse.data.model.FeedbackItem> = emptyList(),
    val report: Report? = null
)
