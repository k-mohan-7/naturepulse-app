package com.simats.naturepulse.ui.dashboard

import android.Manifest
import android.content.Context
import android.location.LocationManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.simats.naturepulse.core.network.AppConfig
import com.simats.naturepulse.core.util.distanceText
import com.simats.naturepulse.core.util.toTimeAgo
import com.simats.naturepulse.data.model.Report
import com.simats.naturepulse.data.model.ReportStats
import com.simats.naturepulse.ui.components.*
import com.simats.naturepulse.ui.theme.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    userName: String = "Citizen",
    onReportClick: (Int) -> Unit,
    onAddReport: () -> Unit,
    onLocationReady: (Double, Double) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var selectedType by remember { mutableStateOf("") }
    var selectedMapReport by remember { mutableStateOf<Report?>(null) }
    var isLocating by remember { mutableStateOf(false) }

    val locationPermission = rememberMultiplePermissionsState(
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    )

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddReport,
                containerColor = ForestGreen,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, "Add Report")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundDark,
        // Allow content to draw behind status & nav bars
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding()),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // ── Top Banner + KPI Stats (single gradient container) ──────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                // Extended gradient: dark green → mid green → lighter green → fade out
                                colorStops = arrayOf(
                                    0.0f to ForestGreenDark,
                                    0.30f to ForestGreen,
                                    0.65f to ForestGreen.copy(alpha = 0.55f),
                                    1.0f to BackgroundDark
                                )
                            )
                        )
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(top = 22.dp, bottom = 8.dp)
                ) {
                    Column {
                        // Greeting
                        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                            Text(
                                text = "Hey there, $userName 👋",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Thank you for protecting and saving a life today 🌿",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = LightGold
                            )
                        }
                        // KPI Stats inline within the gradient
                        state.stats?.let { stats ->
                            Spacer(Modifier.height(18.dp))
                            StatsRow(stats = stats)
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            // ── (KPI Stats already rendered above inside gradient) ───────────────

            // ── Interactive Map View (Web App Feature) ────────────────────
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = "Interactive Incident Map",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = "${state.reports.size} pins",
                            style = MaterialTheme.typography.labelSmall,
                            color = ForestGreenLight,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, GoldBorder, RoundedCornerShape(16.dp))
                    ) {
                        IncidentMapView(
                            userLat = state.lat ?: 0.0,
                            userLng = state.lng ?: 0.0,
                            reports = state.reports,
                            selectedReportId = selectedMapReport?.id,
                            onReportClick = { report ->
                                selectedMapReport = report
                            }
                        )

                        // Marker Small Detail Preview Card
                        selectedMapReport?.let { report ->
                            Card(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = AppConfig.imageUrl(report.imagePath, report.type),
                                        contentDescription = report.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = report.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = OnSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            TypeChip(report.type)
                                            SeverityChip(report.severity)
                                        }
                                        if (report.distanceKm != null) {
                                            Text(
                                                text = "🧭 ${distanceText(report.distanceKm)} away",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = WarmGold,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        IconButton(
                                            onClick = { selectedMapReport = null },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Close, "Close", tint = OnSurfaceMuted)
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        Button(
                                            onClick = { onReportClick(report.id) },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text("View Details", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Filter Toolbar ────────────────────────────────────────────
            item {
                LazyRow(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(listOf("" to "🌍 All", "wildlife" to "🦌 Wildlife", "pollution" to "🏭 Pollution")) { (type, label) ->
                        FilterChipButton(
                            label = label,
                            selected = selectedType == type,
                            onClick = {
                                selectedType = type
                                viewModel.load(state.lat, state.lng, type.ifBlank { null })
                            }
                        )
                    }
                }
            }

            // ── Feed Header ───────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Incident Feed",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "${state.reports.size} incidents",
                        style = MaterialTheme.typography.labelMedium,
                        color = OnSurfaceFaint
                    )
                }
            }

            when {
                state.isLoading -> item { LoadingState(Modifier.height(200.dp)) }
                state.error != null -> item {
                    ErrorState(
                        message = state.error!!,
                        onRetry = { viewModel.load(state.lat, state.lng, selectedType.ifBlank { null }) }
                    )
                }
                state.reports.isEmpty() -> item {
                    EmptyState(
                        message = "No incidents in this area yet",
                        subtitle = "Be the first to report something nearby!"
                    )
                }
                else -> items(state.reports, key = { it.id }) { report ->
                    ReportCard(
                        report = report,
                        onClick = { onReportClick(report.id) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatsRow(stats: ReportStats) {
    LazyRow(
        modifier = Modifier.padding(vertical = 4.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { KpiCard("📊", "Total", stats.total, ForestGreen) }
        item { KpiCard("🦌", "Wildlife", stats.wildlife, TypeWildlife) }
        item { KpiCard("🏭", "Pollution", stats.pollution, TypePollution) }
        item { KpiCard("⚠️", "Critical", stats.critical, SeverityCritical) }
        item { KpiCard("✅", "Resolved", stats.resolved, SuccessGreen) }
    }
}

@Composable
private fun KpiCard(emoji: String, label: String, value: Int, color: Color) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .width(105.dp)
            .border(1.dp, Color(0xFFE2EBE2), RoundedCornerShape(14.dp))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 22.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = OnSurfaceMuted
            )
        }
    }
}

@Composable
private fun FilterChipButton(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) ForestGreen else SurfaceDark
    val textColor = if (selected) Color.White else OnSurfaceMuted
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bg,
        shadowElevation = if (selected) 2.dp else 0.dp,
        modifier = Modifier
            .border(1.dp, if (selected) ForestGreen else Color(0xFFE2EBE2), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

private fun getDeviceLocation(context: Context, callback: (Double, Double) -> Unit) {
    try {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val provider = if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
            LocationManager.GPS_PROVIDER
        else LocationManager.NETWORK_PROVIDER

        if (androidx.core.content.ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            val loc = lm.getLastKnownLocation(provider)
            if (loc != null) callback(loc.latitude, loc.longitude)
            else {
                lm.requestSingleUpdate(provider, object : android.location.LocationListener {
                    override fun onLocationChanged(l: android.location.Location) { callback(l.latitude, l.longitude) }
                }, null)
            }
        }
    } catch (_: Exception) {}
}
