package com.example.schoolmanagement.presentation.academics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schoolmanagement.api.AcademicRepository
import com.example.schoolmanagement.api.TeacherRepository
import com.example.schoolmanagement.api.models.ClassTeacherAssignment
import com.example.schoolmanagement.api.models.PeriodEntry
import com.example.schoolmanagement.api.models.TeacherResponse
import com.example.schoolmanagement.auth.TokenManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class TimetableState {
    object Loading : TimetableState()
    data class Success(val periods: List<PeriodEntry>) : TimetableState()
    data class Error(val message: String) : TimetableState()
}

sealed class ClassTeacherState {
    object Loading : ClassTeacherState()
    data class Success(val assignments: List<ClassTeacherAssignment>) : ClassTeacherState()
    data class Error(val message: String) : ClassTeacherState()
}

class TimetableViewModel(
    private val repository: AcademicRepository,
    private val teacherRepository: TeacherRepository,
    private val tokenManager: TokenManager
) : ViewModel() {
    val userRole: String? get() = tokenManager.getUserRole()

    private val _allPeriods = MutableStateFlow<List<PeriodEntry>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    private val _classTeacherState = MutableStateFlow<ClassTeacherState>(ClassTeacherState.Loading)
    val classTeacherState: StateFlow<ClassTeacherState> = _classTeacherState.asStateFlow()

    private val _teachers = MutableStateFlow<List<TeacherResponse>>(emptyList())
    val teachers: StateFlow<List<TeacherResponse>> = _teachers.asStateFlow()

    private val _selectedGrade = MutableStateFlow("__placeholder__")
    val selectedGrade: StateFlow<String> = _selectedGrade.asStateFlow()

    private val _selectedTeacherId = MutableStateFlow<Int?>(null)
    val selectedTeacherId: StateFlow<Int?> = _selectedTeacherId.asStateFlow()

    private val _activeTab = MutableStateFlow(
        if (tokenManager.getUserRole()?.lowercase()?.removePrefix("role_") == "teacher") "my-timetable" 
        else "teacher-view"
    )
    val activeTab: StateFlow<String> = _activeTab.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _inputs = combine(_activeTab, _selectedGrade, _selectedTeacherId) { tab, grade, teacherId ->
        TimetableInputs(tab, grade, teacherId)
    }

    val state: StateFlow<TimetableState> = combine(
        _allPeriods, _isLoading, _error, _inputs
    ) { all, loading, err, inputs ->
        if (loading) return@combine TimetableState.Loading
        if (err != null) return@combine TimetableState.Error(err)

        val filtered = when (inputs.tab) {
            "grade-view" -> {
                if (inputs.grade == "__placeholder__") emptyList()
                else all.filter { 
                    it.gradeClass.equals(inputs.grade, ignoreCase = true) || 
                    it.gradeClass.equals(inputs.grade.replace("Grade ", ""), ignoreCase = true) ||
                    "Grade ${it.gradeClass}".equals(inputs.grade, ignoreCase = true)
                }
            }
            "teacher-view" -> if (inputs.teacherId == null) emptyList() else all.filter { it.teacher_id == inputs.teacherId }
            "my-timetable" -> {
                val myId = tokenManager.getUserId()
                if (myId == null) emptyList() else all.filter { it.teacher_id == myId }
            }
            else -> emptyList()
        }
        TimetableState.Success(filtered.sortedBy { it.periodNumber })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TimetableState.Loading)

    data class TimetableInputs(val tab: String, val grade: String, val teacherId: Int?)

    init {
        loadInitialData()
    }

    fun loadInitialData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            // Load Teachers
            teacherRepository.fetchAllTeachers().onSuccess {
                _teachers.value = it
            }

            // Load All Periods
            repository.fetchAllTimetables()
                .onSuccess { 
                    _allPeriods.value = it 
                }
                .onFailure { 
                    _error.value = it.message ?: "Failed to load timetable" 
                }
            
            // Load Class Teachers
            loadClassTeachers()
            
            _isLoading.value = false
        }
    }

    fun setActiveTab(tabId: String) {
        _activeTab.value = tabId
    }

    fun setSelectedGrade(grade: String) {
        _selectedGrade.value = grade
    }

    fun setSelectedTeacher(id: Int?) {
        _selectedTeacherId.value = id
    }

    private fun loadClassTeachers() {
        viewModelScope.launch {
            _classTeacherState.value = ClassTeacherState.Loading
            repository.fetchAllClassTeachers()
                .onSuccess { _classTeacherState.value = ClassTeacherState.Success(it) }
                .onFailure { _classTeacherState.value = ClassTeacherState.Error(it.message ?: "Error") }
        }
    }

    fun addPeriod(period: PeriodEntry) {
        viewModelScope.launch {
            _isSaving.value = true
            repository.savePeriod(period).onSuccess { 
                refreshPeriods()
            }
            _isSaving.value = false
        }
    }

    fun removePeriod(id: Int) {
        viewModelScope.launch {
            repository.deletePeriod(id).onSuccess { 
                refreshPeriods()
            }
        }
    }

    private suspend fun refreshPeriods() {
        repository.fetchAllTimetables().onSuccess {
            _allPeriods.value = it
        }
        loadClassTeachers()
    }

    fun assignClassTeacher(assignment: ClassTeacherAssignment) {
        viewModelScope.launch {
            _isSaving.value = true
            repository.assignClassTeacher(assignment).onSuccess { loadClassTeachers() }
            _isSaving.value = false
        }
    }
}
