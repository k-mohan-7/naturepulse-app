package com.simats.naturepulse.core.util

import java.text.SimpleDateFormat
import java.util.*

/** Format "2026-08-12 10:30:00" → "2h ago" style */
fun String.toTimeAgo(): String {
    return try {
        val formats = listOf("yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss'Z'", "yyyy-MM-dd'T'HH:mm:ss")
        var date: Date? = null
        for (fmt in formats) {
            runCatching {
                date = SimpleDateFormat(fmt, Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }.parse(this)
            }
            if (date != null) break
        }
        val d = date ?: return this
        val diff = (System.currentTimeMillis() - d.time) / 1000
        when {
            diff < 60      -> "${diff.coerceAtLeast(1)}s ago"
            diff < 3600    -> "${diff / 60}m ago"
            diff < 86400   -> "${diff / 3600}h ago"
            diff < 604800  -> "${diff / 86400}d ago"
            else           -> SimpleDateFormat("d MMM yyyy", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") }.format(d)
        }
    } catch (e: Exception) { this }
}

fun String.toDisplayDate(): String {
    return try {
        val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
        val d = fmt.parse(this) ?: return this
        SimpleDateFormat("d MMM yyyy", Locale.getDefault()).apply { timeZone = TimeZone.getDefault() }.format(d)
    } catch (e: Exception) { this }
}

fun String.statusLabel(): String = replace('_', ' ').split(" ")
    .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }

fun distanceText(km: Double?): String {
    if (km == null || km.isNaN()) return ""
    return if (km < 1.0) "${(km * 1000).toInt().coerceAtLeast(1)} m away"
    else "%.1f km away".format(km)
}
