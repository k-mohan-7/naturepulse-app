package com.simats.naturepulse.data.remote

import com.simats.naturepulse.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

/**
 * NaturePulse — Retrofit API Service.
 * All 20 backend endpoints mapped to their exact PHP routes.
 */
interface ApiService {

    // ─── Auth ─────────────────────────────────────────────────────────────────

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<AuthData>>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthData>>

    @POST("auth/logout")
    suspend fun logout(): Response<ApiResponse<Unit>>

    @GET("auth/me")
    suspend fun me(): Response<ApiResponse<User>>

    // ─── Users ────────────────────────────────────────────────────────────────

    @GET("users/profile")
    suspend fun profile(): Response<ApiResponse<User>>

    @POST("users/update")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<ApiResponse<User>>

    @POST("users/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<ApiResponse<Unit>>

    // ─── Reports ──────────────────────────────────────────────────────────────

    @GET("reports/list")
    suspend fun reports(
        @Query("type") type: String? = null,
        @Query("status") status: String? = null,
        @Query("severity") severity: String? = null,
        @Query("q") q: String? = null,
        @Query("limit") limit: Int? = null
    ): Response<ApiResponse<List<Report>>>

    @GET("reports/nearby")
    suspend fun nearby(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radius") radius: Double? = null,
        @Query("type") type: String? = null
    ): Response<ApiResponse<List<Report>>>

    @GET("reports/get-by-id")
    suspend fun reportById(@Query("id") id: Int): Response<ApiResponse<Report>>

    @GET("reports/my-reports")
    suspend fun myReports(
        @Query("status") status: String? = null,
        @Query("q") q: String? = null
    ): Response<ApiResponse<List<Report>>>

    @GET("reports/stats")
    suspend fun stats(): Response<ApiResponse<ReportStats>>

    @Multipart
    @POST("reports/create")
    suspend fun createReport(
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("type") type: RequestBody,
        @Part("category") category: RequestBody,
        @Part("severity") severity: RequestBody,
        @Part("tags") tags: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part("location_name") locationName: RequestBody,
        @Part image: MultipartBody.Part? = null
    ): Response<ApiResponse<Report>>

    @POST("reports/update")
    suspend fun updateReport(@Body request: UpdateReportRequest): Response<ApiResponse<Report>>

    @POST("reports/update-status")
    suspend fun updateStatus(@Body request: UpdateStatusRequest): Response<ApiResponse<Report>>

    @POST("reports/feedback")
    suspend fun feedback(@Body request: FeedbackRequest): Response<ApiResponse<FeedbackData>>

    // ─── Notifications ────────────────────────────────────────────────────────

    @GET("notifications/list")
    suspend fun notifications(): Response<ApiResponse<NotificationsData>>

    @POST("notifications/mark-read")
    suspend fun markRead(@Body request: MarkReadRequest): Response<ApiResponse<Map<String, Int>>>

    // ─── Upload ───────────────────────────────────────────────────────────────

    @Multipart
    @POST("upload/image")
    suspend fun uploadImage(@Part image: MultipartBody.Part): Response<ApiResponse<UploadData>>
}
