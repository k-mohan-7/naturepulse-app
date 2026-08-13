package com.simats.naturepulse.ui.notifications

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.simats.naturepulse.core.util.NotificationHelper
import com.simats.naturepulse.core.util.toTimeAgo
import com.simats.naturepulse.data.model.AppNotification
import com.simats.naturepulse.ui.components.EmptyState
import com.simats.naturepulse.ui.components.ErrorState
import com.simats.naturepulse.ui.components.LoadingState
import com.simats.naturepulse.ui.theme.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding

private val NOTIF_ICONS = mapOf(
    "nearby_report" to "📍",
    "status_change" to "🔄",
    "reaction"      to "👍",
    "feedback"      to "💬",
    "system"        to "🌿"
)

@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel,
    onOpenReport: (Int) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Post notification permission launcher (Android 13+)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        viewModel.load()
    }

    // Trigger system push notification when new unread notifications are fetched
    LaunchedEffect(state.items) {
        val unreadItem = state.items.firstOrNull { !it.isRead }
        if (unreadItem != null) {
            NotificationHelper.showNotification(
                context = context,
                notificationId = unreadItem.id,
                title = unreadItem.title,
                message = unreadItem.message,
                reportId = unreadItem.reportId
            )
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {
        // Header — extends behind status bar with gradient
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(ForestGreenDark, ForestGreen, BackgroundDark)
                    )
                )
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "🔔 Incident Alerts",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (state.unread > 0) {
                    Text(
                        "${state.unread} unread alert${if (state.unread > 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = LightGold
                    )
                } else {
                    Text(
                        "All notifications caught up",
                        style = MaterialTheme.typography.bodySmall,
                        color = MintGreen
                    )
                }
            }
            if (state.unread > 0) {
                Button(
                    onClick = { viewModel.markAllRead() },
                    colors = ButtonDefaults.buttonColors(containerColor = WarmGold)
                ) {
                    Text("Mark all read", color = ForestGreenDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        when {
            state.isLoading -> LoadingState()
            state.error != null -> ErrorState(state.error!!, onRetry = { viewModel.load() })
            state.items.isEmpty() -> EmptyState(
                message = "No notifications yet",
                subtitle = "Alerts about nearby incidents will appear here.",
                emoji = "🔔"
            )
            else -> LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                items(state.items, key = { it.id }) { notif ->
                    NotificationItem(
                        notif = notif,
                        onClick = {
                            if (!notif.isRead) viewModel.markRead(notif.id)
                            notif.reportId?.let { onOpenReport(it) }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationItem(
    notif: AppNotification,
    onClick: () -> Unit
) {
    val bg = if (!notif.isRead) LightGold.copy(alpha = 0.5f) else SurfaceDark
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Icon circle
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(if (!notif.isRead) WarmGold else SurfaceVariantDark),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = NOTIF_ICONS[notif.type] ?: "🔔",
                fontSize = 20.sp
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = notif.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface,
                    modifier = Modifier.weight(1f)
                )
                if (!notif.isRead) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(WarmGold)
                    )
                }
            }
            Spacer(Modifier.height(3.dp))
            Text(
                text = notif.message,
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceMuted,
                maxLines = 2
            )
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = notif.createdAt.toTimeAgo(),
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceFaint
                )
                if (notif.reportId != null) {
                    Text(
                        text = "· Open report →",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreenLight
                    )
                }
            }
        }
    }
    HorizontalDivider(color = Color(0xFFE2EBE2), thickness = 1.dp)
}
