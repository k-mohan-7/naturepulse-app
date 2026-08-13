package com.simats.naturepulse.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ─── Auth ──────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class User(
    val id: Int = 0,
    val name: String = "",
    val email: String = "",
    val role: String = "citizen",
    @Json(name = "avatar_url") val avatarUrl: String? = null,
    @Json(name = "joined_at") val joinedAt: String? = null,
    val phone: String? = null,
    val city: String? = null,
    val region: String? = null,
    val bio: String? = null,
    @Json(name = "notify_nearby") val notifyNearby: Boolean = false,
    @Json(name = "notify_status") val notifyStatus: Boolean = false,
    @Json(name = "radius_km") val radiusKm: Float = 25f,
    @Json(name = "reports_count") val reportsCount: Int = 0
)

// ─── Report ─────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class FeedbackItem(
    val id: String = "",
    val kind: String = "comment",         // "reaction" | "comment" | "reply"
    val reaction: String? = null,          // "correct" | "incorrect"
    val message: String = "",
    @Json(name = "user_id") val userId: Int = 0,
    @Json(name = "user_name") val userName: String = "Citizen",
    @Json(name = "parent_id") val parentId: String? = null,
    @Json(name = "created_at") val createdAt: String = ""
)

@JsonClass(generateAdapter = true)
data class FeedbackSummary(
    @Json(name = "correct_count") val correctCount: Int = 0,
    @Json(name = "incorrect_count") val incorrectCount: Int = 0,
    @Json(name = "comment_count") val commentCount: Int = 0,
    @Json(name = "reply_count") val replyCount: Int = 0
)

@JsonClass(generateAdapter = true)
data class StatusHistory(
    val id: Int = 0,
    @Json(name = "report_id") val reportId: Int = 0,
    @Json(name = "old_status") val oldStatus: String? = null,
    @Json(name = "new_status") val newStatus: String = "",
    val note: String? = null,
    @Json(name = "actor_name") val actorName: String? = null,
    @Json(name = "created_at") val createdAt: String = ""
)

@JsonClass(generateAdapter = true)
data class Report(
    val id: Int = 0,
    @Json(name = "reporter_id") val reporterId: Int = 0,
    @Json(name = "reporter_name") val reporterName: String? = null,
    @Json(name = "reporter_avatar") val reporterAvatar: String? = null,
    val title: String = "",
    val description: String = "",
    val type: String = "wildlife",         // "wildlife" | "pollution"
    val category: String? = null,
    val tags: List<String> = emptyList(),
    val severity: String = "medium",       // "low" | "medium" | "high" | "critical"
    val status: String = "open",           // "open" | "pending" | "under_review" | "resolved" | "closed"
    @Json(name = "image_path") val imagePath: String? = null,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    @Json(name = "location_name") val locationName: String? = null,
    val reactions: Int = 0,
    val feedback: List<FeedbackItem> = emptyList(),
    @Json(name = "feedback_summary") val feedbackSummary: FeedbackSummary = FeedbackSummary(),
    @Json(name = "distance_km") val distanceKm: Double? = null,
    @Json(name = "created_at") val createdAt: String = "",
    @Json(name = "updated_at") val updatedAt: String = "",
    val history: List<StatusHistory> = emptyList()  // only on detail
)

// ─── Stats ──────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class ReportStats(
    val total: Int = 0,
    val wildlife: Int = 0,
    val pollution: Int = 0,
    val active: Int = 0,
    val resolved: Int = 0,
    val critical: Int = 0
)

// ─── Notification ────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class AppNotification(
    val id: Int = 0,
    @Json(name = "user_id") val userId: Int = 0,
    @Json(name = "report_id") val reportId: Int? = null,
    val title: String = "",
    val message: String = "",
    val type: String = "system",   // "nearby_report" | "status_change" | "reaction" | "feedback" | "system"
    @Json(name = "is_read") val isRead: Boolean = false,
    @Json(name = "created_at") val createdAt: String = ""
)
