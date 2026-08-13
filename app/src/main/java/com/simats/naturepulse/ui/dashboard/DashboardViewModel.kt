package com.simats.naturepulse.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.simats.naturepulse.data.model.Report
import com.simats.naturepulse.data.model.ReportStats
import com.simats.naturepulse.data.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val stats: ReportStats? = null,
    val reports: List<Report> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val lat: Double? = null,
    val lng: Double? = null
)

class DashboardViewModel(private val repo: ReportRepository) : ViewModel() {

    private val _state = MutableStateFlow(DashboardUiState())
    val state: StateFlow<DashboardUiState> = _state

    fun load(lat: Double? = null, lng: Double? = null, type: String? = null) {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            // Load stats
            repo.stats().onSuccess { stats ->
                _state.value = _state.value.copy(stats = stats)
            }
            // Load nearby or all reports
            val reportsResult = if (lat != null && lng != null) {
                repo.nearby(lat, lng, 25.0, type).onSuccess { list ->
                    _state.value = _state.value.copy(reports = list, lat = lat, lng = lng)
                }
            } else {
                repo.reports(type = type, limit = 50).onSuccess { list ->
                    _state.value = _state.value.copy(reports = list)
                }
            }
            reportsResult.onFailure { e ->
                _state.value = _state.value.copy(error = e.message)
            }
            _state.value = _state.value.copy(isLoading = false)
        }
    }

    fun confirmReport(reportId: Int, onDone: (String) -> Unit) {
        viewModelScope.launch {
            repo.feedback(reportId, "reaction", "correct")
                .onSuccess { onDone("Confirmation recorded ✅") }
                .onFailure { onDone(it.message ?: "Failed") }
        }
    }

    class Factory(private val repo: ReportRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = DashboardViewModel(repo) as T
    }
}
