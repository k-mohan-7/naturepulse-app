package com.simats.naturepulse.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.simats.naturepulse.data.model.Report
import com.simats.naturepulse.data.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ReportDetailUiState(
    val report: Report? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val actionMessage: String? = null,
    val isActionLoading: Boolean = false
)

class ReportDetailViewModel(private val repo: ReportRepository) : ViewModel() {

    private val _state = MutableStateFlow(ReportDetailUiState())
    val state: StateFlow<ReportDetailUiState> = _state

    fun load(id: Int) {
        // Show full loading spinner only if we don't have report data yet
        val showFullLoading = _state.value.report == null || _state.value.report?.id != id
        if (showFullLoading) {
            _state.value = _state.value.copy(isLoading = true, error = null)
        }
        viewModelScope.launch {
            repo.getById(id)
                .onSuccess { _state.value = _state.value.copy(report = it, isLoading = false) }
                .onFailure { _state.value = _state.value.copy(error = it.message, isLoading = false) }
        }
    }

    fun sendFeedback(reportId: Int, type: String, message: String, parentId: String? = null) {
        _state.value = _state.value.copy(isActionLoading = true, actionMessage = null)
        viewModelScope.launch {
            repo.feedback(reportId, type, message, parentId)
                .onSuccess { data ->
                    val msg = when (type) {
                        "reaction" -> if (message == "correct") "Confirmation recorded ✅" else "Marked as not correct"
                        "comment"  -> "Comment posted 💬"
                        "reply"    -> "Reply posted ↩"
                        else       -> "Done"
                    }
                    _state.value = _state.value.copy(
                        isActionLoading = false,
                        actionMessage = msg,
                        report = data.report ?: _state.value.report
                    )
                    // Smoothly refresh latest report details & feedback without flashing loading spinner
                    load(reportId)
                }
                .onFailure {
                    _state.value = _state.value.copy(
                        isActionLoading = false,
                        actionMessage = it.message ?: "Action failed"
                    )
                }
        }
    }

    fun updateStatus(reportId: Int, status: String, note: String) {
        _state.value = _state.value.copy(isActionLoading = true)
        viewModelScope.launch {
            repo.updateStatus(reportId, status, note)
                .onSuccess {
                    _state.value = _state.value.copy(isActionLoading = false, actionMessage = "Status updated ✅")
                    load(reportId)
                }
                .onFailure { _state.value = _state.value.copy(isActionLoading = false, actionMessage = it.message) }
        }
    }

    fun updateReport(reportId: Int, title: String, description: String, severity: String) {
        if (title.trim().length < 3 || description.trim().length < 5) {
            _state.value = _state.value.copy(actionMessage = "Title and description are too short")
            return
        }
        _state.value = _state.value.copy(isActionLoading = true)
        viewModelScope.launch {
            repo.updateReport(reportId, title.trim(), description.trim(), severity)
                .onSuccess {
                    _state.value = _state.value.copy(isActionLoading = false, actionMessage = "Report updated ✅", report = it)
                }
                .onFailure { _state.value = _state.value.copy(actionMessage = it.message, isActionLoading = false) }
        }
    }

    fun clearMessage() { _state.value = _state.value.copy(actionMessage = null) }

    class Factory(private val repo: ReportRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ReportDetailViewModel(repo) as T
    }
}
