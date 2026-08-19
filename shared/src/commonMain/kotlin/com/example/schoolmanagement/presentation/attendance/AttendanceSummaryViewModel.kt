package com.example.schoolmanagement.presentation.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schoolmanagement.api.AttendanceRepository
import com.example.schoolmanagement.api.models.AttendanceRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import com.example.schoolmanagement.api.getCurrentEpochMillis

sealed class AttendanceSummaryState {
    object Loading : AttendanceSummaryState()
    data class Success(val records: List<AttendanceRecord>) : AttendanceSummaryState()
    data class Error(val message: String) : AttendanceSummaryState()
}

class AttendanceSummaryViewModel(private val repository: AttendanceRepository) : ViewModel() {
    private val _state = MutableStateFlow<AttendanceSummaryState>(AttendanceSummaryState.Loading)
    val state: StateFlow<AttendanceSummaryState> = _state.asStateFlow()

    private val _startDate = MutableStateFlow(getCurrentDateStr())
    val startDate: StateFlow<String> = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow(getCurrentDateStr())
    val endDate: StateFlow<String> = _endDate.asStateFlow()

    init {
        loadSummary()
    }

    private fun getCurrentDateStr(): String {
        val currentMoment = Instant.fromEpochMilliseconds(getCurrentEpochMillis())
        val now = currentMoment.toLocalDateTime(TimeZone.currentSystemDefault())
        val month = now.monthNumber.toString().padStart(2, '0')
        val day = now.dayOfMonth.toString().padStart(2, '0')
        return "${now.year}-$month-$day"
    }

    fun loadSummary() {
        viewModelScope.launch {
            _state.value = AttendanceSummaryState.Loading
            repository.fetchAttendanceByDateRange(_startDate.value, _endDate.value)
                .onSuccess { records ->
                    _state.value = AttendanceSummaryState.Success(records)
                }
                .onFailure { error ->
                    _state.value = AttendanceSummaryState.Error(error.message ?: "Failed to load summary")
                }
        }
    }
}
