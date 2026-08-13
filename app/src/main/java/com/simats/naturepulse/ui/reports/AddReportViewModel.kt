package com.simats.naturepulse.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.simats.naturepulse.data.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

data class AddReportUiState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val createdReportId: Int? = null,
    val error: String? = null
)

class AddReportViewModel(private val repo: ReportRepository) : ViewModel() {
    private val _state = MutableStateFlow(AddReportUiState())
    val state: StateFlow<AddReportUiState> = _state

    fun submit(
        title: String, description: String, type: String, category: String,
        severity: String, tags: String, lat: Double, lng: Double,
        locationName: String, imageFile: File?
    ) {
        if (title.trim().length < 3) { _state.value = _state.value.copy(error = "Title must be at least 3 characters."); return }
        if (description.trim().length < 5) { _state.value = _state.value.copy(error = "Please describe what you observed."); return }
        if (lat == 0.0 && lng == 0.0) { _state.value = _state.value.copy(error = "Please select a location."); return }

        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            repo.createReport(
                title.trim(), description.trim(), type, category, severity,
                tags.trim(), lat, lng, locationName.trim(), imageFile
            ).onSuccess { report ->
                _state.value = _state.value.copy(isLoading = false, success = true, createdReportId = report.id)
            }.onFailure {
                _state.value = _state.value.copy(isLoading = false, error = it.message)
            }
        }
    }

    fun clearError() { _state.value = _state.value.copy(error = null) }

    class Factory(private val repo: ReportRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = AddReportViewModel(repo) as T
    }
}
