package com.simats.naturepulse.data.repository

import com.simats.naturepulse.data.model.Report
import com.simats.naturepulse.data.model.ReportStats
import com.simats.naturepulse.data.remote.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class ReportRepository(private val api: ApiService) {

    suspend fun reports(
        type: String? = null,
        status: String? = null,
        severity: String? = null,
        q: String? = null,
        limit: Int? = null
    ): Result<List<Report>> = safeCall {
        api.reports(type?.ifBlank { null }, status?.ifBlank { null }, severity?.ifBlank { null }, q?.ifBlank { null }, limit)
    }

    suspend fun nearby(lat: Double, lng: Double, radius: Double? = null, type: String? = null): Result<List<Report>> =
        safeCall { api.nearby(lat, lng, radius, type?.ifBlank { null }) }

    suspend fun getById(id: Int): Result<Report> = safeCall { api.reportById(id) }

    suspend fun myReports(status: String? = null, q: String? = null): Result<List<Report>> =
        safeCall { api.myReports(status?.ifBlank { null }, q?.ifBlank { null }) }

    suspend fun stats(): Result<ReportStats> = safeCall { api.stats() }

    suspend fun createReport(
        title: String,
        description: String,
        type: String,
        category: String,
        severity: String,
        tags: String,
        latitude: Double,
        longitude: Double,
        locationName: String,
        imageFile: File? = null
    ): Result<Report> {
        val toBody = { s: String -> s.toRequestBody("text/plain".toMediaTypeOrNull()) }
        val imagePart = imageFile?.let {
            val rb = it.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("image", it.name, rb)
        }
        return safeCall {
            api.createReport(
                toBody(title), toBody(description), toBody(type), toBody(category),
                toBody(severity), toBody(tags), toBody(latitude.toString()),
                toBody(longitude.toString()), toBody(locationName), imagePart
            )
        }
    }

    suspend fun updateReport(id: Int, title: String, description: String, severity: String): Result<Report> =
        safeCall { api.updateReport(UpdateReportRequest(id, title, description, severity)) }

    suspend fun updateStatus(id: Int, status: String, note: String): Result<Report> =
        safeCall { api.updateStatus(UpdateStatusRequest(id, status, note)) }

    suspend fun feedback(id: Int, type: String, message: String, parentId: String? = null): Result<FeedbackData> =
        safeCall { api.feedback(FeedbackRequest(id, type, message, parentId)) }

    suspend fun uploadImage(file: File): Result<String> {
        val rb = file.asRequestBody("image/*".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("image", file.name, rb)
        return safeCall { api.uploadImage(part) }.map { it.path }
    }
}
