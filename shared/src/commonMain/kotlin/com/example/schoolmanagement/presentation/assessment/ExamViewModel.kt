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

    private val _activeTab = MutableStateFlow("test") // "test" or "exam"
    val activeTab: StateFlow<String> = _activeTab.asStateFlow()

    private val _selectedExamName = MutableStateFlow<String?>(null)
    val selectedExamName: StateFlow<String?> = _selectedExamName.asStateFlow()

    private val _selectedGrade = MutableStateFlow<String?>(null)
    val selectedGrade: StateFlow<String?> = _selectedGrade.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    init {
        loadNames()
    }

    fun setActiveTab(tab: String) {
        _activeTab.value = tab
        _selectedExamName.value = null
        _timetableState.value = ExamTimetableState.Idle
        loadNames()
    }

    fun setSelectedExam(name: String) {
        if (name.isBlank()) {
            _selectedExamName.value = null
            _timetableState.value = ExamTimetableState.Idle
            return
        }
        _selectedExamName.value = name
        loadTimetable(name)
    }

    fun setSelectedGrade(grade: String?) {
        _selectedGrade.value = grade
    }

    fun loadNames() {
        viewModelScope.launch {
            _state.value = ExamState.Loading
            val result = if (_activeTab.value == "test") {
                repository.fetchTestNames()
            } else {
                repository.fetchExamNames()
            }
            
            result.onSuccess { exams ->
                _state.value = ExamState.Success(exams)
            }.onFailure { error ->
                _state.value = ExamState.Error(error.message ?: "Failed to load names")
            }
        }
    }

    fun loadTimetable(name: String) {
        viewModelScope.launch {
            _timetableState.value = ExamTimetableState.Loading
            val result = if (_activeTab.value == "test") {
                repository.fetchTestTimetable(name)
            } else {
                repository.fetchExamTimetable(name)
            }

            result.onSuccess { entries ->
                _timetableState.value = ExamTimetableState.Success(entries)
            }.onFailure { error ->
                _timetableState.value = ExamTimetableState.Error(error.message ?: "Failed to load timetable")
            }
        }
    }

    fun createTimetableEntries(entries: List<ExamTimetableEntry>, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isSaving.value = true
            val type = if (_activeTab.value == "test") "test" else "exam"
            val result = if (type == "test") {
                repository.saveTestTimetable(entries)
            } else {
                repository.saveExamTimetable(entries)
            }

            result.onSuccess {
                _isSaving.value = false
                loadNames()
                onComplete()
            }.onFailure {
                _isSaving.value = false
            }
        }
    }
}
