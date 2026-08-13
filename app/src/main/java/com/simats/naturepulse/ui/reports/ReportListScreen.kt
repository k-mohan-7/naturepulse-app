package com.simats.naturepulse.ui.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.simats.naturepulse.ui.components.*
import com.simats.naturepulse.ui.theme.*

@Composable
fun ReportListScreen(
    viewModel: ReportListViewModel,
    title: String = "Reports",
    defaultType: String = "",
    onReportClick: (Int) -> Unit,
    onAddReport: (() -> Unit)? = null
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var searchText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) { viewModel.load(type = defaultType) }

    Scaffold(
        floatingActionButton = {
            if (onAddReport != null) {
                FloatingActionButton(
                    onClick = onAddReport,
                    containerColor = ForestGreen,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, "Add Report")
                }
            }
        },
        containerColor = BackgroundDark,
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding())) {

            // ── Top Gradient Header (extends behind status bar) ────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(ForestGreenDark, ForestGreen, BackgroundDark)
                        )
                    )
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${state.reports.size} incident reports",
                        style = MaterialTheme.typography.bodySmall,
                        color = LightGold,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // ── Search bar ────────────────────────────────────────────────────
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = { Text("Search by title, location or tag…", color = OnSurfaceFaint) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = ForestGreenLight) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    focusManager.clearFocus()
                    viewModel.load(q = searchText)
                }),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ForestGreen,
                    unfocusedBorderColor = Color(0xFFD4DEC9),
                    focusedTextColor = OnSurface,
                    unfocusedTextColor = OnSurface,
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark
                )
            )

            // ── Filter chips ──────────────────────────────────────────────────
            if (!viewModel.myReportsMode) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    if (defaultType.isBlank()) {
                        item { FilterChip("All", state.filterType.isEmpty()) { viewModel.load(type = "") } }
                        item { FilterChip("🦌 Wildlife", state.filterType == "wildlife") { viewModel.load(type = "wildlife") } }
                        item { FilterChip("🏭 Pollution", state.filterType == "pollution") { viewModel.load(type = "pollution") } }
                    }
                    item { FilterChip("Open", state.filterStatus == "open") { viewModel.load(status = if (state.filterStatus == "open") "" else "open") } }
                    item { FilterChip("Resolved", state.filterStatus == "resolved") { viewModel.load(status = if (state.filterStatus == "resolved") "" else "resolved") } }
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    item { FilterChip("All", state.filterStatus.isEmpty()) { viewModel.load(status = "") } }
                    item { FilterChip("Open", state.filterStatus == "open") { viewModel.load(status = "open") } }
                    item { FilterChip("Under Review", state.filterStatus == "under_review") { viewModel.load(status = "under_review") } }
                    item { FilterChip("Resolved", state.filterStatus == "resolved") { viewModel.load(status = "resolved") } }
                    item { FilterChip("Closed", state.filterStatus == "closed") { viewModel.load(status = "closed") } }
                }
            }

            // ── Report list ───────────────────────────────────────────────────
            when {
                state.isLoading -> LoadingState()
                state.error != null -> ErrorState(state.error!!, onRetry = { viewModel.load() })
                state.reports.isEmpty() -> EmptyState(
                    message = if (viewModel.myReportsMode) "No reports submitted yet" else "No reports found",
                    subtitle = if (viewModel.myReportsMode) "Tap + to submit your first report" else "Try a different filter"
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(bottom = 24.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.reports, key = { it.id }) { report ->
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
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
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
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
