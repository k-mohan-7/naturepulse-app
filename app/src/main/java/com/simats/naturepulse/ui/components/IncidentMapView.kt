package com.simats.naturepulse.ui.components

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.simats.naturepulse.data.model.Report
import com.simats.naturepulse.ui.theme.ForestGreen
import com.simats.naturepulse.ui.theme.WarmGold
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

/**
 * Interactive OpenStreetMap component with custom emoji/icon markers:
 * - User Location: 🧍 Human icon (no background)
 * - Wildlife Incidents: 🦌 Deer icon
 * - Pollution Incidents: 🏭 Factory icon
 * Includes Touch Interception fix and always-visible Recenter button.
 *
 * The recenter button (compass/target icon) is ALWAYS visible so the user can
 * re-center the map even when location permission was just granted.
 */
@Composable
fun IncidentMapView(
    modifier: Modifier = Modifier,
    userLat: Double = 0.0,
    userLng: Double = 0.0,
    reports: List<Report> = emptyList(),
    selectedReportId: Int? = null,
    onReportClick: ((Report) -> Unit)? = null,
    isPickerMode: Boolean = false,
    pickedLat: Double = 0.0,
    pickedLng: Double = 0.0,
    onLocationPicked: ((Double, Double) -> Unit)? = null
) {
    val context = LocalContext.current

    // Stable MapView reference — only created once per composition
    val mapView = remember {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            // Hide built-in zoom buttons — we use pinch-to-zoom
            zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER)

            // Touch Interception Fix: prevents parent LazyColumn from stealing
            // scroll gestures while the user is panning/zooming the map
            setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE ->
                        v.parent?.requestDisallowInterceptTouchEvent(true)
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                        v.parent?.requestDisallowInterceptTouchEvent(false)
                }
                false
            }
        }
    }

    // Cleanup when composable leaves composition
    DisposableEffect(Unit) {
        onDispose { mapView.onDetach() }
    }

    // Determine the focal point for map center
    val centerPoint = remember(userLat, userLng, pickedLat, pickedLng, reports) {
        when {
            pickedLat != 0.0 && pickedLng != 0.0 -> GeoPoint(pickedLat, pickedLng)
            userLat != 0.0 && userLng != 0.0     -> GeoPoint(userLat, userLng)
            reports.isNotEmpty()                  -> GeoPoint(reports.first().latitude, reports.first().longitude)
            else                                  -> GeoPoint(12.9716, 77.5946) // Default: Bangalore
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize(),
            update = { map ->
                map.overlays.clear()

                map.controller.setZoom(if (isPickerMode) 15.0 else 13.5)
                map.controller.setCenter(centerPoint)

                // 🧍 Human Icon for User Location (only when GPS is available)
                if (userLat != 0.0 && userLng != 0.0 && !isPickerMode) {
                    val userMarker = Marker(map).apply {
                        position = GeoPoint(userLat, userLng)
                        title = "Your Location"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        icon = createEmojiDrawable(context, "🧍", sizeDp = 40)
                    }
                    map.overlays.add(userMarker)
                }

                // 🦌 Deer and 🏭 Factory markers for incidents
                if (!isPickerMode) {
                    reports.forEach { report ->
                        val isSelected = report.id == selectedReportId
                        val emoji = if (report.type == "pollution") "🏭" else "🦌"
                        val iconSize = if (isSelected) 46 else 34

                        val reportMarker = Marker(map).apply {
                            position = GeoPoint(report.latitude, report.longitude)
                            title = report.title
                            snippet = report.locationName ?: report.type
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            icon = createEmojiDrawable(context, emoji, sizeDp = iconSize, isSelected = isSelected)
                            setOnMarkerClickListener { _, _ ->
                                onReportClick?.invoke(report)
                                true
                            }
                        }
                        map.overlays.add(reportMarker)
                    }
                }

                // Picker Mode: tap-to-place pin
                if (isPickerMode) {
                    if (pickedLat != 0.0 && pickedLng != 0.0) {
                        val pickMarker = Marker(map).apply {
                            position = GeoPoint(pickedLat, pickedLng)
                            title = "Selected Location"
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            icon = createEmojiDrawable(context, "📍", sizeDp = 42)
                        }
                        map.overlays.add(pickMarker)
                    }

                    val eventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                            onLocationPicked?.invoke(p.latitude, p.longitude)
                            return true
                        }
                        override fun longPressHelper(p: GeoPoint): Boolean = false
                    })
                    map.overlays.add(eventsOverlay)
                }

                map.invalidate()
            }
        )

        // ── Recenter / Compass Button ─────────────────────────────────────────
        // Always shown so the user can always snap back to their location or
        // the incident location. Visibility does NOT depend on userLat/userLng.
        if (!isPickerMode) {
            Surface(
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 6.dp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable {
                        // Recenter: prefer user location, fall back to first report / center
                        val target = when {
                            userLat != 0.0 && userLng != 0.0 -> GeoPoint(userLat, userLng)
                            reports.isNotEmpty()              -> GeoPoint(reports.first().latitude, reports.first().longitude)
                            else                              -> centerPoint
                        }
                        mapView.controller.animateTo(target, 14.5, 800L)
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Recenter Map",
                        tint = if (userLat != 0.0 && userLng != 0.0) ForestGreen else WarmGold,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

/**
 * Renders crisp transparent emoji icons (🧍 Human, 🦌 Deer, 🏭 Factory, 📍 Pin)
 * without any background box or border.
 */
private fun createEmojiDrawable(
    context: Context,
    emoji: String,
    sizeDp: Int = 36,
    isSelected: Boolean = false
): BitmapDrawable {
    val density = context.resources.displayMetrics.density
    val px = (sizeDp * density).toInt()
    val bitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Subtle gold glow ring around selected marker
    if (isSelected) {
        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(200, 255, 193, 7)
            style = Paint.Style.STROKE
            strokeWidth = 5f
        }
        canvas.drawCircle(px / 2f, px / 2f, (px / 2f) - 4f, glowPaint)
    }

    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = px * 0.75f
        textAlign = Paint.Align.CENTER
    }

    val yPos = (canvas.height / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f)
    canvas.drawText(emoji, px / 2f, yPos, textPaint)

    return BitmapDrawable(context.resources, bitmap)
}
