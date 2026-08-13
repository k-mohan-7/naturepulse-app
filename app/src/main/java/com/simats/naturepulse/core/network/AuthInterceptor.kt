package com.simats.naturepulse.core.network

import com.simats.naturepulse.core.datastore.PreferencesManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Injects "Authorization: Bearer <token>" header into every outgoing request
 * when a token is stored in DataStore.
 */
class AuthInterceptor(private val prefs: PreferencesManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { prefs.getTokenNow() }
        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }
}
