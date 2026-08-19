package com.example.schoolmanagement.presentation.academics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schoolmanagement.api.AcademicRepository
import com.example.schoolmanagement.api.models.PeriodEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class TimetableState {
    object Loading : TimetableState()
    data class Success(val periods: List<PeriodEntry>) : TimetableState()
    data class Error(val message: String) : TimetableState()
}

class TimetableViewModel(private val repository: AcademicRepository) : ViewModel() {
    private val _state = MutableStateFlow<TimetableState>(TimetableState.Loading)
    val state: StateFlow<TimetableState> = _state.asStateFlow()

    private val _selectedGrade = MutableStateFlow("1")
    val selectedGrade: StateFlow<String> = _selectedGrade.asStateFlow()

    init {
        loadTimetable()
    }

    fun setSelectedGrade(grade: String) {
        _selectedGrade.value = grade
        loadTimetable()
    }

    fun loadTimetable() {
        viewModelScope.launch {
            _state.value = TimetableState.Loading
            repository.fetchTimetableByGrade(_selectedGrade.value)
                .onSuccess { periods ->
                    _state.value = TimetableState.Success(periods)
                }
                .onFailure { error ->
                    _state.value = TimetableState.Error(error.message ?: "Failed to load timetable")
                }
        }
    }

    fun addPeriod(period: PeriodEntry) {
        viewModelScope.launch {
            repository.savePeriod(period)
                .onSuccess { loadTimetable() }
        }
    }

    fun removePeriod(id: Int) {
        viewModelScope.launch {
            repository.deletePeriod(id)
                .onSuccess { loadTimetable() }
        }
    }
}
