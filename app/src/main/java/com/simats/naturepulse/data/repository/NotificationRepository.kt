package com.simats.naturepulse.data.repository

import com.simats.naturepulse.data.model.AppNotification
import com.simats.naturepulse.data.remote.ApiService
import com.simats.naturepulse.data.remote.MarkReadRequest
import com.simats.naturepulse.data.remote.NotificationsData

class NotificationRepository(private val api: ApiService) {

    suspend fun list(): Result<NotificationsData> = safeCall { api.notifications() }

    suspend fun markRead(id: Int? = null): Result<Unit> =
        safeCall { api.markRead(MarkReadRequest(id)) }.map { }
}
