package com.simats.naturepulse.data.repository

import com.simats.naturepulse.data.remote.ApiResponse
import retrofit2.Response

/**
 * Generic safe-call wrapper.
 * Converts a Retrofit Response into a Result<T>,
 * extracting the `data` field or wrapping the error message.
 */
suspend fun <T> safeCall(block: suspend () -> Response<ApiResponse<T>>): Result<T> {
    return try {
        val response = block()
        val body = response.body()
        when {
            response.isSuccessful && body?.success == true && body.data != null ->
                Result.success(body.data)
            response.isSuccessful && body?.success == true ->
                Result.success(null as T)   // e.g. logout returns null data
            else -> {
                val msg = body?.message
                    ?: response.errorBody()?.string()
                    ?: "Request failed (${response.code()})"
                Result.failure(Exception(msg))
            }
        }
    } catch (e: Exception) {
        Result.failure(Exception(e.message ?: "Network error. Is the server running?"))
    }
}
