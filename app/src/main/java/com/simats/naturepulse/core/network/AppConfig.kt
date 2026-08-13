package com.simats.naturepulse.core.network

/**
 * NaturePulse — Central configuration.
 *
 * ┌──────────────────────────────────────────────────────────────────┐
 * │  CHANGE YOUR SERVER IP / BASE URL HERE IN ONE PLACE!            │
 * │                                                                  │
 * │  Just edit SERVER_IP_OR_URL below. Everything else —            │
 * │  Retrofit base URL, image URL root — updates automatically.     │
 * │                                                                  │
 * │  TIP: If your IP changes often (home WiFi, college network)     │
 * │  add extra IPs to FALLBACK_IPS list. The app will try each      │
 * │  one in order when building URLs.                               │
 * └──────────────────────────────────────────────────────────────────┘
 */
object AppConfig {

    /**
     * 🟢 PRIMARY SERVER IP — CHANGE THIS ONLY (one edit updates everything) 🟢
     *
     * Examples:
     *   "192.168.1.6"          — plain IP (auto-prefixed with http://)
     *   "10.37.35.64"          — plain IP
     *   "http://192.168.1.6"   — explicit URL
     */
    const val SERVER_IP_OR_URL = "10.37.35.64"

    /**
     * 🔄 FALLBACK IPs (optional — add your common network IPs here)
     * The primary IP above is always tried first.
     * These are only used for constructing alternative URLs if needed.
     */
    val FALLBACK_IPS: List<String> = listOf(
        // "192.168.1.6",
        // "192.168.0.105",
    )

    /**
     * Sub-path where NaturePulse is installed on the server.
     * The backend stores images at /naturepulse/uploads/...
     * Never change this unless you moved the app on the server.
     */
    private const val APP_SUB_PATH = "/naturepulse"

    // ─── Derived Properties (automatically updated from SERVER_IP_OR_URL) ───

    /**
     * Bare scheme + host, e.g. "http://10.37.35.64"
     * (no trailing slash, no sub-path).
     */
    val SERVER_HOST: String
        get() = parseHost(SERVER_IP_OR_URL)

    /**
     * Root for the NaturePulse web app: http://IP/naturepulse
     * Used to resolve uploaded image paths from the backend.
     */
    val UPLOADS_ROOT: String
        get() = "$SERVER_HOST$APP_SUB_PATH"

    /**
     * Retrofit base URL for all API calls.
     * e.g. "http://10.37.35.64/naturepulse/backend/index.php/"
     * Must end with "/" for Retrofit.
     */
    val BASE_URL: String
        get() = "$UPLOADS_ROOT/backend/index.php/"

    // ─── Image URL Helpers ───────────────────────────────────────────────────

    /**
     * High-quality fallback images (matching web app UI.fallbackImage).
     * Shown when a report has no uploaded photo.
     */
    fun fallbackImage(type: String?): String {
        return if (type == "pollution")
            "https://images.unsplash.com/photo-1611273426858-450d8e3c9fce?auto=format&fit=crop&w=800&q=80"
        else
            "https://images.unsplash.com/photo-1546182990-dffeafbe841d?auto=format&fit=crop&w=800&q=80"
    }

    /**
     * Resolves a backend image_path to a full loadable URL.
     *
     * Backend returns paths like: /uploads/report-images/20260812_xxx.png
     * These are relative to the NaturePulse app root (UPLOADS_ROOT).
     *
     * Full resolved URL: http://IP/naturepulse/uploads/report-images/xxx.png
     *
     * Falls back to a curated nature/pollution photo from Unsplash when the
     * report has no uploaded image (matching web app behavior).
     */
    fun imageUrl(path: String?, type: String? = null): String {
        if (path.isNullOrBlank()) return fallbackImage(type)
        if (path.startsWith("http://") || path.startsWith("https://")) return path
        val cleanPath = if (path.startsWith("/")) path else "/$path"
        return "$UPLOADS_ROOT$cleanPath"
    }

    // ─── Internal Helpers ────────────────────────────────────────────────────

    /**
     * Parses a raw input (plain IP or full URL) into a bare "scheme://host[:port]" string.
     */
    private fun parseHost(input: String): String {
        val trimmed = input.trim()
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            val scheme = if (trimmed.startsWith("https://")) "https" else "http"
            val withoutScheme = trimmed.removePrefix("https://").removePrefix("http://")
            val hostOnly = withoutScheme.substringBefore("/")
            return "$scheme://$hostOnly"
        }
        return "http://$trimmed"
    }
}
