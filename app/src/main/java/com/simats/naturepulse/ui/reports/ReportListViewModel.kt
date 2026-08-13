package com.simats.naturepulse.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.simats.naturepulse.data.model.Report
import com.simats.naturepulse.data.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ReportListUiState(
    val reports: List<Report> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val filterType: String = "",
    val filterStatus: String = "",
    val filterSeverity: String = "",
    val query: String = ""
)

class ReportListViewModel(private val repo: ReportRepository) : ViewModel() {

    private val _state = MutableStateFlow(ReportListUiState())
    val state: StateFlow<ReportListUiState> = _state

    // For "My Reports" mode
    var myReportsMode = false

    fun load(
        type: String = _state.value.filterType,
        status: String = _state.value.filterStatus,
        severity: String = _state.value.filterSeverity,
        q: String = _state.value.query
    ) {
        _state.value = _state.value.copy(isLoading = true, error = null,
            filterType = type, filterStatus = status, filterSeverity = severity, query = q)
        viewModelScope.launch {
            val result = if (myReportsMode) {
                repo.myReports(status = status.ifBlank { null }, q = q.ifBlank { null })
            } else {
                repo.reports(
                    type = type.ifBlank { null },
                    status = status.ifBlank { null },
                    severity = severity.ifBlank { null },
                    q = q.ifBlank { null }
                )
            }
            result
                .onSuccess { _state.value = _state.value.copy(reports = it, isLoading = false) }
                .onFailure { _state.value = _state.value.copy(error = it.message, isLoading = false) }
        }
    }

    class Factory(private val repo: ReportRepository, private val myReports: Boolean = false) :
        ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ReportListViewModel(repo).also { it.myReportsMode = myReports } as T
    }
}
