package com.simats.naturepulse.ui.reports

import android.Manifest
import android.content.Context
import android.location.Geocoder
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.simats.naturepulse.ui.components.IncidentMapView
import com.simats.naturepulse.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

val CATEGORIES = mapOf(
    "wildlife"  to listOf("Wildlife sighting", "Injured animal", "Human-wildlife conflict", "Roadkill", "Poaching / illegal activity", "Other"),
    "pollution" to listOf("Air pollution", "Water pollution", "Land pollution", "Noise pollution", "Illegal dumping", "Other")
)

@Composable
fun AddReportScreen(
    viewModel: AddReportViewModel,
    onSuccess: (Int) -> Unit,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var type by remember { mutableStateOf("wildlife") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(CATEGORIES["wildlife"]!!.first()) }
    var severity by remember { mutableStateOf("medium") }
    var tags by remember { mutableStateOf("") }
    var lat by remember { mutableStateOf(0.0) }
    var lng by remember { mutableStateOf(0.0) }
    var locationName by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageFile by remember { mutableStateOf<File?>(null) }
    var catExpanded by remember { mutableStateOf(false) }
    var sevExpanded by remember { mutableStateOf(false) }
    var locationLoading by remember { mutableStateOf(false) }

    val locationPermission = rememberMultiplePermissionsState(
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    )

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
        uri?.let { imageFile = copyUriToTempFile(context, it) }
    }

    val scope = rememberCoroutineScope()

    LaunchedEffect(state.success) {
        if (state.success && state.createdReportId != null) onSuccess(state.createdReportId!!)
    }
    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showSnackbar(it); viewModel.clearError() }
    }
    LaunchedEffect(type) { category = CATEGORIES[type]!!.first() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report an Incident", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ForestGreen)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Type toggle ───────────────────────────────────────────────
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.border(1.dp, Color(0xFFE2EBE2), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Report Type", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OnSurface)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        TypeToggleBtn("🦌 Wildlife", "wildlife", type) { type = it }
                        TypeToggleBtn("🏭 Pollution", "pollution", type) { type = it }
                    }
                }
            }

            // ── Details Form ──────────────────────────────────────────────
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.border(1.dp, Color(0xFFE2EBE2), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Incident Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OnSurface)

                    OutlinedTextField(
                        value = title, onValueChange = { title = it },
                        label = { Text("Title *") },
                        placeholder = { Text("Brief incident title…", color = OnSurfaceFaint) },
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        colors = npOutlinedFieldColors(), shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = description, onValueChange = { description = it },
                        label = { Text("Description *") },
                        placeholder = { Text("Describe what you observed…", color = OnSurfaceFaint) },
                        minLines = 3, maxLines = 6, modifier = Modifier.fillMaxWidth(),
                        colors = npOutlinedFieldColors(), shape = RoundedCornerShape(12.dp)
                    )

                    // Category dropdown
                    ExposedDropdownMenuBox(expanded = catExpanded, onExpandedChange = { catExpanded = it }) {
                        OutlinedTextField(
                            value = category, onValueChange = {}, readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            colors = npOutlinedFieldColors(), shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                            CATEGORIES[type]!!.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat, color = OnSurface) },
                                    onClick = { category = cat; catExpanded = false },
                                    modifier = Modifier.background(SurfaceDark)
                                )
                            }
                        }
                    }

                    // Severity dropdown
                    ExposedDropdownMenuBox(expanded = sevExpanded, onExpandedChange = { sevExpanded = it }) {
                        OutlinedTextField(
                            value = severity.replaceFirstChar { it.uppercase() }, onValueChange = {}, readOnly = true,
                            label = { Text("Severity") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sevExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            colors = npOutlinedFieldColors(), shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(expanded = sevExpanded, onDismissRequest = { sevExpanded = false }) {
                            listOf("low", "medium", "high", "critical").forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s.replaceFirstChar { it.uppercase() }, color = OnSurface) },
                                    onClick = { severity = s; sevExpanded = false },
                                    modifier = Modifier.background(SurfaceDark)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = tags, onValueChange = { tags = it },
                        label = { Text("Tags (comma separated)") },
                        placeholder = { Text("e.g. river,eagle,urgent", color = OnSurfaceFaint) },
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        colors = npOutlinedFieldColors(), shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // ── Location Picker Map (Web App Parity) ───────────────────────
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.border(1.dp, Color(0xFFE2EBE2), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📍 Location Map Selector", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OnSurface)
                        Spacer(Modifier.weight(1f))
                        Text("Tap map to pick", style = MaterialTheme.typography.labelSmall, color = WarmGold, fontWeight = FontWeight.Bold)
                    }

                    // Interactive Map Picker
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, GoldBorder, RoundedCornerShape(12.dp))
                    ) {
                        IncidentMapView(
                            userLat = lat,
                            userLng = lng,
                            isPickerMode = true,
                            pickedLat = lat,
                            pickedLng = lng,
                            onLocationPicked = { pickedLat, pickedLng ->
                                lat = pickedLat
                                lng = pickedLng
                                scope.launch {
                                    locationName = reverseGeocode(context, pickedLat, pickedLng)
                                }
                            }
                        )
                    }

                    // My Location Button
                    Button(
                        onClick = {
                            if (locationPermission.allPermissionsGranted) {
                                locationLoading = true
                                getDeviceLocation(context) { gotLat, gotLng ->
                                    lat = gotLat
                                    lng = gotLng
                                    locationLoading = false
                                    scope.launch {
                                        locationName = reverseGeocode(context, gotLat, gotLng)
                                    }
                                }
                            } else {
                                locationPermission.launchMultiplePermissionRequest()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WarmGold),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (locationLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = ForestGreenDark, strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                        } else {
                            Icon(Icons.Default.MyLocation, null, tint = ForestGreenDark, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(
                            if (lat != 0.0) "Location set ✓" else "Use Current GPS Location",
                            color = ForestGreenDark, fontWeight = FontWeight.Bold
                        )
                    }

                    if (lat != 0.0) {
                        Text(
                            "🧭 Coordinates: %.5f, %.5f".format(lat, lng),
                            style = MaterialTheme.typography.labelSmall, color = ForestGreenLight, fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedTextField(
                        value = locationName, onValueChange = { locationName = it },
                        label = { Text("Location Name (Auto-filled or manual)") },
                        placeholder = { Text("e.g. Riverside Park, Bangalore", color = OnSurfaceFaint) },
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        colors = npOutlinedFieldColors(), shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // ── Image Upload Section ───────────────────────────────────────
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.border(1.dp, Color(0xFFE2EBE2), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📷 Photo (optional)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OnSurface)
                    Spacer(Modifier.height(10.dp))
                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri, contentDescription = "Preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { imagePickerLauncher.launch("image/*") }
                        )
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = { imageUri = null; imageFile = null }) {
                            Text("Remove photo", color = ErrorRed, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceVariantDark)
                                .border(1.dp, Color(0xFFD4DEC9), RoundedCornerShape(12.dp))
                                .clickable { imagePickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.AddPhotoAlternate, null, tint = ForestGreenLight, modifier = Modifier.size(36.dp))
                                Spacer(Modifier.height(6.dp))
                                Text("Tap to select photo", style = MaterialTheme.typography.bodySmall, color = OnSurfaceMuted, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            // ── Submit Button ─────────────────────────────────────────────
            Button(
                onClick = {
                    viewModel.submit(title, description, type, category, severity, tags, lat, lng, locationName, imageFile)
                },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ForestGreen,
                    disabledContainerColor = ForestGreen.copy(alpha = 0.5f)
                )
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.5.dp)
                    Spacer(Modifier.width(10.dp))
                    Text("Submitting…", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                } else {
                    Text("Submit Report 🌿", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun RowScope.TypeToggleBtn(label: String, value: String, selected: String, onSelect: (String) -> Unit) {
    val isSelected = selected == value
    Button(
        onClick = { onSelect(value) },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) ForestGreen else SurfaceVariantDark,
            contentColor = if (isSelected) Color.White else OnSurfaceMuted
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.weight(1f)
    ) { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) }
}

private fun getDeviceLocation(context: Context, callback: (Double, Double) -> Unit) {
    try {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
        val provider = if (lm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER))
            android.location.LocationManager.GPS_PROVIDER
        else android.location.LocationManager.NETWORK_PROVIDER

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

private suspend fun reverseGeocode(context: Context, lat: Double, lng: Double): String {
    return withContext(Dispatchers.IO) {
        try {
            val geo = Geocoder(context, Locale.getDefault())
            @Suppress("DEPRECATION")
            val list = geo.getFromLocation(lat, lng, 1)
            if (!list.isNullOrEmpty()) {
                val a = list[0]
                listOfNotNull(a.subLocality ?: a.locality, a.subAdminArea ?: a.adminArea, a.countryName)
                    .joinToString(", ").ifBlank { "%.4f, %.4f".format(lat, lng) }
            } else "%.4f, %.4f".format(lat, lng)
        } catch (_: Exception) { "%.4f, %.4f".format(lat, lng) }
    }
}

private fun copyUriToTempFile(context: Context, uri: Uri): File? {
    return try {
        val input = context.contentResolver.openInputStream(uri) ?: return null
        val ext = context.contentResolver.getType(uri)?.substringAfter("/") ?: "jpg"
        val file = File.createTempFile("np_upload_", ".$ext", context.cacheDir)
        FileOutputStream(file).use { out -> input.copyTo(out) }
        file
    } catch (_: Exception) { null }
}
