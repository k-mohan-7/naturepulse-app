package com.simats.naturepulse.ui.reports

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.simats.naturepulse.core.network.AppConfig
import com.simats.naturepulse.core.util.statusLabel
import com.simats.naturepulse.core.util.toDisplayDate
import com.simats.naturepulse.core.util.toTimeAgo
import com.simats.naturepulse.data.model.Report
import com.simats.naturepulse.data.model.StatusHistory
import com.simats.naturepulse.ui.components.*
import com.simats.naturepulse.ui.theme.*

private val STATUS_LIST = listOf("open", "pending", "under_review", "resolved", "closed")
private val SEVERITY_LIST = listOf("low", "medium", "high", "critical")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    viewModel: ReportDetailViewModel,
    reportId: Int,
    currentUserId: Int,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(reportId) { viewModel.load(reportId) }

    LaunchedEffect(state.actionMessage) {
        state.actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Incident Details", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ForestGreen)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundDark
    ) { padding ->
        when {
            state.isLoading -> LoadingState(Modifier.padding(padding))
            state.error != null -> ErrorState(state.error!!, onRetry = { viewModel.load(reportId) }, modifier = Modifier.padding(padding))
            state.report != null -> {
                val report = state.report!!
                // isOwner is determined ONLY by matching reporter_id from the API response
                // to the currently logged-in user's ID. No admin bypass.
                val isOwner = currentUserId != 0 && report.reporterId == currentUserId

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                ) {
                    // ── 1. Hero Uploaded Image (Top) ──────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp)
                    ) {
                        AsyncImage(
                            model = AppConfig.imageUrl(report.imagePath, report.type),
                            contentDescription = report.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Chips on image
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            TypeChip(report.type)
                            SeverityChip(report.severity)
                            StatusChip(report.status)
                        }
                    }

                    // ── 2. Incident Location & Map Card (Below Hero Image) ────
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .border(1.dp, Color(0xFFE2EBE2), RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "📍 Location Details",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            )

                            // Interactive OSM Map
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, GoldBorder, RoundedCornerShape(12.dp))
                            ) {
                                IncidentMapView(
                                    userLat = report.latitude,
                                    userLng = report.longitude,
                                    reports = listOf(report)
                                )
                            }

                            InfoRow("📍", "Address", report.locationName ?: "Unknown location")
                            InfoRow("🧭", "Coordinates", "%.5f, %.5f".format(report.latitude, report.longitude))
                        }
                    }

                    // ── 3. Main Info Card ─────────────────────────────────────
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .border(1.dp, Color(0xFFE2EBE2), RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            if (!report.category.isNullOrBlank()) CategoryChip(report.category)
                            Spacer(Modifier.height(6.dp))
                            Text(report.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = OnSurface)
                            Spacer(Modifier.height(8.dp))
                            Text(report.description, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceMuted)

                            if (report.tags.isNotEmpty()) {
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    report.tags.forEach { TagChip(it) }
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SurfaceVariantDark)

                            InfoRow("🕒", "Reported", "${report.createdAt.toDisplayDate()} (${report.createdAt.toTimeAgo()})")
                            InfoRow("🔄", "Updated", report.updatedAt.toTimeAgo())
                            InfoRow("👤", "Reporter", report.reporterName ?: "Citizen")

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SurfaceVariantDark)

                            // Feedback summary counts
                            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                FeedbackBadge("👍", report.feedbackSummary.correctCount, "confirmed", ForestGreenLight)
                                FeedbackBadge("❌", report.feedbackSummary.incorrectCount, "not correct", ErrorRed)
                                val reactionCount = report.feedback.count { it.kind == "reaction" }
                                FeedbackBadge("👥", reactionCount, "reactions", WarmAmber)
                            }
                        }
                    }

                    // ── 4. Status Timeline ────────────────────────────────────
                    if (report.history.isNotEmpty()) {
                        SectionCard(title = "📋 Status Timeline") {
                            report.history.forEach { h -> StatusHistoryItem(h) }
                        }
                    }

                    // ── 5. Community Feedback & Single-Reaction Toggle ──
                    // Non-owners see: react buttons + comment box + all comments + reply
                    // Owners see: comment box + all comments + reply (no reaction buttons on own report)
                    // If userId is 0 (still loading profile), we hold off showing action buttons
                    if (currentUserId != 0) {
                        FeedbackSection(
                            report = report,
                            isOwner = isOwner,
                            currentUserId = currentUserId,
                            isLoading = state.isActionLoading,
                            onFeedback = { kind, value, parentId ->
                                viewModel.sendFeedback(report.id, kind, value, parentId)
                            }
                        )
                    }

                    // ── 6. Owner Actions (status update + edit) ──
                    // Only shown when we are certain this is the owner's report
                    if (isOwner) {
                        OwnerActionsSection(
                            report = report,
                            isLoading = state.isActionLoading,
                            onUpdateStatus = { status, note -> viewModel.updateStatus(report.id, status, note) },
                            onUpdateReport = { title, desc, severity -> viewModel.updateReport(report.id, title, desc, severity) }
                        )
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun FeedbackSection(
    report: Report,
    isOwner: Boolean,
    currentUserId: Int,
    isLoading: Boolean,
    onFeedback: (String, String, String?) -> Unit
) {
    var commentText by remember { mutableStateOf("") }
    var replyText by remember { mutableStateOf("") }
    var replyTargetId by remember { mutableStateOf<String?>(null) }
    var replyTargetName by remember { mutableStateOf("") }

    val userReaction = report.feedback.lastOrNull { it.userId == currentUserId && it.kind == "reaction" }
    val currentReaction = userReaction?.reaction // "correct", "incorrect", or null

    val comments = report.feedback.filter { it.kind == "comment" }
    val replies = report.feedback.filter { it.kind == "reply" }

    SectionCard(title = "💬 Community Feedback") {
        // Toggle Reaction Buttons for Citizens (Non-Owner)
        if (!isOwner) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                // Confirm Button — disabled if already confirmed so double click can never send proxy/duplicate votes
                Button(
                    onClick = {
                        if (currentReaction != "correct") {
                            onFeedback("reaction", "correct", null)
                        }
                    },
                    enabled = !isLoading && currentReaction != "correct",
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentReaction == "correct") ForestGreenLight else WarmGold,
                        disabledContainerColor = ForestGreenLight.copy(alpha = 0.85f),
                        disabledContentColor = Color.White
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        if (currentReaction == "correct") "✓ Confirmed" else "👍 Confirm",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Not Correct Button — enabled when confirmed to allow shifting vote to Not Correct
                Button(
                    onClick = {
                        if (currentReaction != "incorrect") {
                            onFeedback("reaction", "incorrect", null)
                        }
                    },
                    enabled = !isLoading && currentReaction != "incorrect",
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentReaction == "incorrect") ErrorRed else Color(0xFF6C757D),
                        disabledContainerColor = ErrorRed.copy(alpha = 0.85f),
                        disabledContentColor = Color.White
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        if (currentReaction == "incorrect") "✓ Not Correct" else "❌ Not Correct",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
        }

        // Comment input
        OutlinedTextField(
            value = commentText,
            onValueChange = { commentText = it },
            placeholder = { Text("Add a community comment…", color = OnSurfaceFaint) },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 4,
            colors = npOutlinedFieldColors(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                if (commentText.isNotBlank()) {
                    onFeedback("comment", commentText.trim(), null)
                    commentText = ""
                }
            },
            enabled = !isLoading && commentText.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Post Comment", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(14.dp))

        // Comment list
        if (comments.isEmpty()) {
            Text("No comments yet. Be the first to share your observations!", style = MaterialTheme.typography.bodySmall, color = OnSurfaceMuted)
        } else {
            comments.forEach { c ->
                CommentItem(
                    comment = c,
                    replies = replies.filter { it.parentId == c.id.toString() },
                    isOwner = isOwner,
                    onReplyClick = { id, name ->
                        replyTargetId = id
                        replyTargetName = name
                    }
                )
                Spacer(Modifier.height(10.dp))
            }
        }

        // Reply field (when owner or user selects a comment)
        if (replyTargetId != null) {
            Spacer(Modifier.height(10.dp))
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Replying to $replyTargetName", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = ForestGreenLight, modifier = Modifier.weight(1f))
                        IconButton(onClick = { replyTargetId = null }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Close, null, tint = OnSurfaceMuted)
                        }
                    }
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        placeholder = { Text("Write your reply…", color = OnSurfaceFaint) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        colors = npOutlinedFieldColors(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (replyText.isNotBlank()) {
                                onFeedback("reply", replyText.trim(), replyTargetId)
                                replyText = ""
                                replyTargetId = null
                            }
                        },
                        enabled = !isLoading && replyTargetId != null && replyText.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = WarmAmber),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("Post Reply", color = Color.White, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
private fun CommentItem(
    comment: com.simats.naturepulse.data.model.FeedbackItem,
    replies: List<com.simats.naturepulse.data.model.FeedbackItem>,
    isOwner: Boolean,
    onReplyClick: (String, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceVariantDark)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(comment.userName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = OnSurface, modifier = Modifier.weight(1f))
            Text(comment.createdAt.toTimeAgo(), style = MaterialTheme.typography.labelSmall, color = OnSurfaceFaint)
        }
        Spacer(Modifier.height(4.dp))
        Text(comment.message, style = MaterialTheme.typography.bodySmall, color = OnSurface)

        // Reply button — visible to ALL logged-in users (not just owner)
        TextButton(
            onClick = { onReplyClick(comment.id.toString(), comment.userName) },
            contentPadding = PaddingValues(0.dp)
        ) {
            Text("Reply →", style = MaterialTheme.typography.labelSmall, color = ForestGreenLight, fontWeight = FontWeight.Bold)
        }

        // Replies
        replies.forEach { reply ->
            Spacer(Modifier.height(6.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
                    .padding(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("↩ ${reply.userName}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = ForestGreenLight, modifier = Modifier.weight(1f))
                    Text(reply.createdAt.toTimeAgo(), style = MaterialTheme.typography.labelSmall, color = OnSurfaceFaint)
                }
                Spacer(Modifier.height(2.dp))
                Text(reply.message, style = MaterialTheme.typography.bodySmall, color = OnSurface)
            }
        }
    }
}

@Composable
private fun OwnerActionsSection(
    report: Report,
    isLoading: Boolean,
    onUpdateStatus: (String, String) -> Unit,
    onUpdateReport: (String, String, String) -> Unit
) {
    var selectedStatus by remember(report.status) { mutableStateOf(report.status) }
    var note by remember { mutableStateOf("") }
    var eTitle by remember(report.title) { mutableStateOf(report.title) }
    var eDesc by remember(report.description) { mutableStateOf(report.description) }
    var eSeverity by remember(report.severity) { mutableStateOf(report.severity) }

    var statusExpanded by remember { mutableStateOf(false) }
    var sevExpanded by remember { mutableStateOf(false) }

    SectionCard(title = "🔧 Update Status") {
        ExposedDropdownMenuBox(expanded = statusExpanded, onExpandedChange = { statusExpanded = it }) {
            OutlinedTextField(
                value = selectedStatus.statusLabel(),
                onValueChange = {},
                readOnly = true,
                label = { Text("Status") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                colors = npOutlinedFieldColors()
            )
            ExposedDropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                STATUS_LIST.forEach { s ->
                    DropdownMenuItem(
                        text = { Text(s.statusLabel(), color = OnSurface) },
                        onClick = { selectedStatus = s; statusExpanded = false },
                        modifier = Modifier.background(SurfaceDark)
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Status note (optional)") },
            placeholder = { Text("e.g. Ranger dispatched to location", color = OnSurfaceFaint) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = npOutlinedFieldColors(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(10.dp))

        Button(
            onClick = { onUpdateStatus(selectedStatus, note.trim()); note = "" },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Update Status", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }

    SectionCard(title = "✏️ Edit Incident Details") {
        OutlinedTextField(
            value = eTitle,
            onValueChange = { eTitle = it },
            label = { Text("Title") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = npOutlinedFieldColors(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = eDesc,
            onValueChange = { eDesc = it },
            label = { Text("Description") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth(),
            colors = npOutlinedFieldColors(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(8.dp))

        ExposedDropdownMenuBox(expanded = sevExpanded, onExpandedChange = { sevExpanded = it }) {
            OutlinedTextField(
                value = eSeverity.replaceFirstChar { it.uppercase() },
                onValueChange = {},
                readOnly = true,
                label = { Text("Severity") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sevExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                colors = npOutlinedFieldColors()
            )
            ExposedDropdownMenu(expanded = sevExpanded, onDismissRequest = { sevExpanded = false }) {
                SEVERITY_LIST.forEach { s ->
                    DropdownMenuItem(
                        text = { Text(s.replaceFirstChar { it.uppercase() }, color = OnSurface) },
                        onClick = { eSeverity = s; sevExpanded = false },
                        modifier = Modifier.background(SurfaceDark)
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        Button(
            onClick = { onUpdateReport(eTitle.trim(), eDesc.trim(), eSeverity) },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Save Changes", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun StatusHistoryItem(item: StatusHistory) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(10.dp)
                .clip(CircleShape)
                .background(ForestGreen)
        )
        Spacer(Modifier.width(10.dp))
        Column {
            Row {
                StatusChip(item.newStatus)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = item.createdAt.toTimeAgo(),
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceFaint
                )
            }
            if (!item.note.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "“${item.note}”",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceMuted
                )
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .border(1.dp, Color(0xFFE2EBE2), RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OnSurface)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun InfoRow(emoji: String, label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.Top) {
        Text("$emoji  ", fontSize = 14.sp)
        Text("$label: ", style = MaterialTheme.typography.bodySmall, color = OnSurfaceFaint, fontWeight = FontWeight.Bold)
        Text(value, style = MaterialTheme.typography.bodySmall, color = OnSurface, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun FeedbackBadge(emoji: String, count: Int, label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(emoji, fontSize = 14.sp)
        Text("$count $label", style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
    }
}
