package com.simats.naturepulse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.simats.naturepulse.core.network.AppConfig
import com.simats.naturepulse.core.util.distanceText
import com.simats.naturepulse.core.util.toTimeAgo
import com.simats.naturepulse.data.model.Report
import com.simats.naturepulse.ui.theme.*

/**
 * Full report card — used in ReportList, MyReports, and Dashboard feeds.
 * Styled in warm gold & crisp white light theme.
 */
@Composable
fun ReportCard(
    report: Report,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(1.dp, Color(0xFFE2EBE2), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // ── Hero image ────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
            ) {
                AsyncImage(
                    model = AppConfig.imageUrl(report.imagePath, report.type),
                    contentDescription = report.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                )

                // Type + severity chips on top of image
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TypeChip(report.type)
                    SeverityChip(report.severity)
                }

                // Status chip on bottom right of image
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                ) {
                    StatusChip(report.status)
                }
            }

            // ── Content ───────────────────────────────────────────────────
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = report.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = report.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(8.dp))

                // Location + time row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!report.locationName.isNullOrBlank()) {
                        Text(
                            text = "📍 ${report.locationName}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = ForestGreenLight,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Text(
                        text = report.createdAt.toTimeAgo(),
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceFaint
                    )
                }

                // Distance if available
                if (report.distanceKm != null) {
                    Text(
                        text = "🧭 ${distanceText(report.distanceKm)} away",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = WarmGold,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = SurfaceVariantDark, thickness = 1.dp)
                Spacer(Modifier.height(8.dp))

                // Feedback counts
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    FeedbackCountBadge(
                        emoji = "👍",
                        count = report.feedbackSummary.correctCount,
                        label = "confirmed",
                        color = ForestGreenLight
                    )
                    FeedbackCountBadge(
                        emoji = "❌",
                        count = report.feedbackSummary.incorrectCount,
                        label = "not correct",
                        color = ErrorRed
                    )
                    FeedbackCountBadge(
                        emoji = "💬",
                        count = report.feedbackSummary.commentCount,
                        label = "comments",
                        color = OnSurfaceMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedbackCountBadge(emoji: String, count: Int, label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(text = emoji, fontSize = 11.sp)
        Text(
            text = "$count $label",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}
