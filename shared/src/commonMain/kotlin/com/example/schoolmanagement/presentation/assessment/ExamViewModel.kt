package com.example.schoolmanagement.presentation.assessment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schoolmanagement.api.AssessmentRepository
import com.example.schoolmanagement.api.models.ExamTimetableEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ExamState {
    object Loading : ExamState()
    data class Success(val exams: List<String>) : ExamState()
    data class Error(val message: String) : ExamState()
}

sealed class ExamTimetableState {
    object Idle : ExamTimetableState()
    object Loading : ExamTimetableState()
    data class Success(val entries: List<ExamTimetableEntry>) : ExamTimetableState()
    data class Error(val message: String) : ExamTimetableState()
}

class ExamViewModel(private val repository: AssessmentRepository) : ViewModel() {
    private val _state = MutableStateFlow<ExamState>(ExamState.Loading)
    val state: StateFlow<ExamState> = _state.asStateFlow()

    private val _timetableState = MutableStateFlow<ExamTimetableState>(ExamTimetableState.Idle)
    val timetableState: StateFlow<ExamTimetableState> = _timetableState.asStateFlow()

    init {
        loadExams()
    }

    fun loadExams() {
        viewModelScope.launch {
            _state.value = ExamState.Loading
            repository.fetchExamNames()
                .onSuccess { exams ->
                    _state.value = ExamState.Success(exams)
                }
                .onFailure { error ->
                    _state.value = ExamState.Error(error.message ?: "Failed to load exams")
                }
        }
    }

    fun loadTimetable(examName: String) {
        viewModelScope.launch {
            _timetableState.value = ExamTimetableState.Loading
            repository.fetchExamTimetable(examName)
                .onSuccess { entries ->
                    _timetableState.value = ExamTimetableState.Success(entries)
                }
                .onFailure { error ->
                    _timetableState.value = ExamTimetableState.Error(error.message ?: "Failed to load timetable")
                }
        }
    }
}
