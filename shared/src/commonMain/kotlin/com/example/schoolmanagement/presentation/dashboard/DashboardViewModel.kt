package com.example.schoolmanagement.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schoolmanagement.api.DashboardRepository
import com.example.schoolmanagement.api.models.DashboardData
import com.example.schoolmanagement.api.models.DashboardTimeRange
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class DashboardState {
    object Loading : DashboardState()
    data class Success(val data: DashboardData) : DashboardState()
    data class Error(val message: String) : DashboardState()
}

class DashboardViewModel(private val repository: DashboardRepository) : ViewModel() {
    private val _state = MutableStateFlow<DashboardState>(DashboardState.Loading)
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    private val _selectedRange = MutableStateFlow(DashboardTimeRange.WEEK)
    val selectedRange: StateFlow<DashboardTimeRange> = _selectedRange.asStateFlow()

    init {
        loadDashboardData()
    }

    fun setTimeRange(range: DashboardTimeRange) {
        if (_selectedRange.value != range) {
            _selectedRange.value = range
            loadDashboardData()
        }
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _state.value = DashboardState.Loading
            repository.fetchDashboardData(_selectedRange.value)
                .onSuccess { data ->
                    _state.value = DashboardState.Success(data)
                }
                .onFailure { error ->
                    _state.value = DashboardState.Error(error.message ?: "Unknown error")
                }

        }
    }
}
