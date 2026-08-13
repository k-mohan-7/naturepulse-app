package com.simats.naturepulse.ui.profile

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.simats.naturepulse.core.network.AppConfig
import com.simats.naturepulse.core.util.statusLabel
import com.simats.naturepulse.core.util.toDisplayDate
import com.simats.naturepulse.data.model.User
import com.simats.naturepulse.ui.theme.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding

enum class ProfileMode { VIEW, EDIT, CHANGE_PASSWORD }

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onLogout: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var mode by remember { mutableStateOf(ProfileMode.VIEW) }

    LaunchedEffect(Unit) { viewModel.load() }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
            mode = ProfileMode.VIEW
        }
    }
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundDark,
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
                .verticalScroll(rememberScrollState())
        ) {
            // ── Header banner ───────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(ForestGreenDark, ForestGreen, BackgroundDark)))
                    .windowInsetsPadding(WindowInsets.statusBars)
            ) {
                state.user?.let { user ->
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val avatarUrl = AppConfig.imageUrl(user.avatarUrl)
                        if (avatarUrl != null) {
                            AsyncImage(
                                model = avatarUrl,
                                contentDescription = "Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(72.dp).clip(CircleShape).border(2.dp, WarmGold, CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier.size(72.dp).clip(CircleShape).background(SurfaceDark).border(2.dp, WarmGold, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = user.name.firstOrNull()?.uppercase() ?: "?",
                                    fontSize = 28.sp, fontWeight = FontWeight.Bold, color = ForestGreen
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(user.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(user.role.statusLabel(), style = MaterialTheme.typography.labelMedium, color = WarmGold, fontWeight = FontWeight.Bold)
                    }
                }
            }

            when (mode) {
                ProfileMode.VIEW -> state.user?.let { ProfileViewMode(it) { mode = it } }
                ProfileMode.EDIT -> state.user?.let {
                    ProfileEditMode(
                        user = it,
                        isSaving = state.isSaving,
                        onSave = { name, phone, city, region, bio, avatarUrl, radius, near, status ->
                            viewModel.updateProfile(name, phone, city, region, bio, avatarUrl, radius, near, status)
                        },
                        onCancel = { mode = ProfileMode.VIEW }
                    )
                }
                ProfileMode.CHANGE_PASSWORD -> ChangePasswordMode(
                    isSaving = state.isSaving,
                    onSave = { cur, new -> viewModel.changePassword(cur, new) },
                    onCancel = { mode = ProfileMode.VIEW }
                )
            }

            if (mode == ProfileMode.VIEW) {
                Spacer(Modifier.height(8.dp))
                // Action buttons row
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { mode = ProfileMode.EDIT },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ForestGreen)
                    ) { Text("Edit Profile", color = ForestGreen, fontWeight = FontWeight.Bold) }
                    OutlinedButton(
                        onClick = { mode = ProfileMode.CHANGE_PASSWORD },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, WarmGold)
                    ) { Text("Change Password", color = WarmAmber, fontWeight = FontWeight.Bold) }
                }
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = { viewModel.logout(onLogout) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Logout", color = Color.White, fontWeight = FontWeight.Bold) }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun ProfileViewMode(user: User, onModeChange: (ProfileMode) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(1.dp, Color(0xFFE2EBE2), RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ProfileRow("📧", "Email", user.email)
            if (!user.phone.isNullOrBlank()) ProfileRow("📞", "Phone", user.phone)
            val location = listOfNotNull(user.city, user.region).joinToString(", ")
            if (location.isNotBlank()) ProfileRow("📍", "Location", location)
            if (!user.bio.isNullOrBlank()) ProfileRow("📝", "Bio", user.bio)
            ProfileRow("📅", "Joined", user.joinedAt?.toDisplayDate() ?: "—")
            ProfileRow("📊", "Reports", "${user.reportsCount} submitted")
            ProfileRow("🔔", "Notify nearby", if (user.notifyNearby) "Yes" else "No")
            ProfileRow("🔔", "Notify status", if (user.notifyStatus) "Yes" else "No")
            ProfileRow("📡", "Alert radius", "${user.radiusKm.toInt()} km")
        }
    }
}

@Composable
private fun ProfileRow(emoji: String, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text("$emoji  ", fontSize = 14.sp)
        Text("$label: ", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceFaint, fontWeight = FontWeight.Bold)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = OnSurface, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ProfileEditMode(
    user: User,
    isSaving: Boolean,
    onSave: (String, String, String, String, String, String, Float, Boolean, Boolean) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(user.name) }
    var phone by remember { mutableStateOf(user.phone ?: "") }
    var city by remember { mutableStateOf(user.city ?: "") }
    var region by remember { mutableStateOf(user.region ?: "") }
    var bio by remember { mutableStateOf(user.bio ?: "") }
    var avatarUrl by remember { mutableStateOf(user.avatarUrl ?: "") }
    var radiusKm by remember { mutableStateOf(user.radiusKm) }
    var notifyNearby by remember { mutableStateOf(user.notifyNearby) }
    var notifyStatus by remember { mutableStateOf(user.notifyStatus) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(1.dp, Color(0xFFE2EBE2), RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Edit Profile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OnSurface)

            listOf(
                Triple("Full Name *", name) { v: String -> name = v },
                Triple("Phone", phone) { v: String -> phone = v },
                Triple("City", city) { v: String -> city = v },
                Triple("Region / State", region) { v: String -> region = v },
                Triple("Avatar URL", avatarUrl) { v: String -> avatarUrl = v }
            ).forEach { (label, value, onChange) ->
                OutlinedTextField(value = value, onValueChange = onChange, label = { Text(label) },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    colors = npOutlinedFieldColors(), shape = RoundedCornerShape(12.dp))
            }

            OutlinedTextField(value = bio, onValueChange = { bio = it }, label = { Text("Bio") },
                minLines = 2, maxLines = 4, modifier = Modifier.fillMaxWidth(),
                colors = npOutlinedFieldColors(), shape = RoundedCornerShape(12.dp))

            // Radius slider
            Column {
                Text("Alert radius: ${radiusKm.toInt()} km", style = MaterialTheme.typography.bodySmall, color = OnSurface, fontWeight = FontWeight.Bold)
                Slider(value = radiusKm, onValueChange = { radiusKm = it }, valueRange = 1f..100f,
                    colors = SliderDefaults.colors(thumbColor = WarmGold, activeTrackColor = ForestGreen))
            }

            // Notification toggles
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Notify nearby reports", style = MaterialTheme.typography.bodyMedium, color = OnSurface, modifier = Modifier.weight(1f))
                Switch(checked = notifyNearby, onCheckedChange = { notifyNearby = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ForestGreen))
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Notify status changes", style = MaterialTheme.typography.bodyMedium, color = OnSurface, modifier = Modifier.weight(1f))
                Switch(checked = notifyStatus, onCheckedChange = { notifyStatus = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ForestGreen))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                    Text("Cancel", color = OnSurfaceMuted)
                }
                Button(
                    onClick = { onSave(name, phone, city, region, bio, avatarUrl, radiusKm, notifyNearby, notifyStatus) },
                    enabled = !isSaving,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                ) {
                    if (isSaving) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    else Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ChangePasswordMode(isSaving: Boolean, onSave: (String, String) -> Unit, onCancel: () -> Unit) {
    var current by remember { mutableStateOf("") }
    var newPw by remember { mutableStateOf("") }
    var currentVisible by remember { mutableStateOf(false) }
    var newVisible by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(1.dp, Color(0xFFE2EBE2), RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Change Password", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OnSurface)

            OutlinedTextField(
                value = current, onValueChange = { current = it },
                label = { Text("Current Password") },
                visualTransformation = if (currentVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { currentVisible = !currentVisible }) {
                        Icon(if (currentVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = OnSurfaceMuted)
                    }
                },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                colors = npOutlinedFieldColors(), shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = newPw, onValueChange = { newPw = it },
                label = { Text("New Password (min 6 chars)") },
                visualTransformation = if (newVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { newVisible = !newVisible }) {
                        Icon(if (newVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = OnSurfaceMuted)
                    }
                },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                colors = npOutlinedFieldColors(), shape = RoundedCornerShape(12.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                    Text("Cancel", color = OnSurfaceMuted)
                }
                Button(
                    onClick = { onSave(current, newPw) },
                    enabled = !isSaving,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                ) {
                    if (isSaving) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    else Text("Update", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
