package com.example.schoolmanagement.presentation.operations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schoolmanagement.api.OperationsRepository
import com.example.schoolmanagement.api.models.InvoiceHistoryResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.Instant
import com.example.schoolmanagement.api.getCurrentEpochMillis

sealed class InvoiceHistoryState {
    object Loading : InvoiceHistoryState()
    data class Success(val data: InvoiceHistoryResponse) : InvoiceHistoryState()
    data class Error(val message: String) : InvoiceHistoryState()
}

class InvoiceHistoryViewModel(private val repository: OperationsRepository) : ViewModel() {
    private val _state = MutableStateFlow<InvoiceHistoryState>(InvoiceHistoryState.Loading)
    val state: StateFlow<InvoiceHistoryState> = _state.asStateFlow()

    private val _startDate = MutableStateFlow(getCurrentDateStr())
    val startDate: StateFlow<String> = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow(getCurrentDateStr())
    val endDate: StateFlow<String> = _endDate.asStateFlow()

    init {
        loadInvoices()
    }

    private fun getCurrentDateStr(): String {
        val currentMoment = Instant.fromEpochMilliseconds(getCurrentEpochMillis())
        val now = currentMoment.toLocalDateTime(TimeZone.currentSystemDefault())
        val month = now.monthNumber.toString().padStart(2, '0')
        val day = now.dayOfMonth.toString().padStart(2, '0')
        return "${now.year}-$month-$day"
    }

    fun setDateRange(start: String, end: String) {
        _startDate.value = start
        _endDate.value = end
        loadInvoices()
    }

    fun loadInvoices() {
        viewModelScope.launch {
            _state.value = InvoiceHistoryState.Loading
            repository.fetchInvoiceHistory(_startDate.value, _endDate.value)
                .onSuccess { data ->
                    _state.value = InvoiceHistoryState.Success(data)
                }
                .onFailure { error ->
                    _state.value = InvoiceHistoryState.Error(error.message ?: "Failed to load invoices")
                }
        }
    }
}
