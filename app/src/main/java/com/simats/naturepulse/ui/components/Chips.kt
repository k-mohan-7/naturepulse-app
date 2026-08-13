package com.simats.naturepulse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.naturepulse.core.util.statusLabel
import com.simats.naturepulse.ui.theme.*

/**
 * All chip variants — TypeChip, SeverityChip, StatusChip, CategoryChip.
 * Styled with clean light theme colors and bold badges.
 */

@Composable
fun TypeChip(type: String, modifier: Modifier = Modifier) {
    val (bg, textColor, text) = when (type.lowercase()) {
        "wildlife" -> Triple(LightGold, WarmGold, "🦌 Wildlife")
        "pollution" -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), "🏭 Pollution")
        else -> Triple(SurfaceVariantDark, OnSurfaceMuted, type.replaceFirstChar { it.uppercase() })
    }
    NpChip(label = text, bg = bg, textColor = textColor, modifier = modifier)
}

@Composable
fun SeverityChip(severity: String, modifier: Modifier = Modifier) {
    val (bg, textColor, text) = when (severity.lowercase()) {
        "low"      -> Triple(Color(0xE8F5E9), Color(0xFF2E7D32), "Low")
        "medium"   -> Triple(Color(0xFFFFF3E0), Color(0xFFE65100), "Medium")
        "high"     -> Triple(Color(0xFFFBE9E7), Color(0xFFD84315), "High")
        "critical" -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), "Critical ⚠")
        else       -> Triple(SurfaceVariantDark, OnSurfaceMuted, severity.replaceFirstChar { it.uppercase() })
    }
    NpChip(label = text, bg = bg, textColor = textColor, modifier = modifier)
}

@Composable
fun StatusChip(status: String, modifier: Modifier = Modifier) {
    val (bg, textColor, text) = when (status.lowercase()) {
        "open"         -> Triple(Color(0xFFE1F5FE), Color(0xFF0288D1), "Open")
        "pending"      -> Triple(Color(0xFFFFF8E1), Color(0xFFF57F17), "Pending")
        "under_review" -> Triple(Color(0xFFF3E5F5), Color(0xFF7B1FA2), "Under Review")
        "resolved"     -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "Resolved")
        "closed"       -> Triple(Color(0xFFECEFF1), Color(0xFF546E7A), "Closed")
        else           -> Triple(SurfaceVariantDark, OnSurfaceMuted, status.statusLabel())
    }
    NpChip(label = text, bg = bg, textColor = textColor, modifier = modifier)
}

@Composable
fun CategoryChip(category: String, modifier: Modifier = Modifier) {
    NpChip(
        label = category,
        bg = SurfaceVariantDark,
        textColor = OnSurfaceMuted,
        modifier = modifier
    )
}

@Composable
fun TagChip(tag: String, modifier: Modifier = Modifier) {
    NpChip(
        label = "#$tag",
        bg = SurfaceVariantDark,
        textColor = ForestGreenLight,
        modifier = modifier
    )
}

@Composable
private fun NpChip(
    label: String,
    bg: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor,
            fontSize = 11.sp
        )
    }
}
