package com.example.schoolmanagement.presentation.assessment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schoolmanagement.api.AssessmentRepository
import com.example.schoolmanagement.api.StudentRepository
import com.example.schoolmanagement.api.TeacherRepository
import com.example.schoolmanagement.api.models.ExamGradePayload
import com.example.schoolmanagement.api.models.TeacherResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class GradeState {
    object Loading : GradeState()
    data class Success(val students: List<StudentGradeRow>) : GradeState()
    data class Error(val message: String) : GradeState()
}

data class StudentGradeRow(
    val studentId: Int,
    val name: String,
    var marks: String = ""
)

class GradeViewModel(
    private val assessmentRepository: AssessmentRepository,
    private val studentRepository: StudentRepository,
    private val teacherRepository: TeacherRepository
) : ViewModel() {

    private val _state = MutableStateFlow<GradeState>(GradeState.Loading)
    val state: StateFlow<GradeState> = _state.asStateFlow()

    private val _teachers = MutableStateFlow<List<com.example.schoolmanagement.api.models.TeacherResponse>>(emptyList())
    val teachers: StateFlow<List<com.example.schoolmanagement.api.models.TeacherResponse>> = _teachers.asStateFlow()

    private val _selectedTeacherId = MutableStateFlow<Int?>(null)
    val selectedTeacherId: StateFlow<Int?> = _selectedTeacherId.asStateFlow()

    private val _activeTab = MutableStateFlow("test") // "test" or "exam"
    val activeTab: StateFlow<String> = _activeTab.asStateFlow()

    private val _examNames = MutableStateFlow<List<String>>(emptyList())
    val examNames: StateFlow<List<String>> = _examNames.asStateFlow()

    private val _selectedExamName = MutableStateFlow<String?>(null)
    val selectedExamName: StateFlow<String?> = _selectedExamName.asStateFlow()

    private val _selectedClass = MutableStateFlow<String?>(null)
    val selectedClass: StateFlow<String?> = _selectedClass.asStateFlow()

    private val _selectedSubject = MutableStateFlow<String?>(null)
    val selectedSubject: StateFlow<String?> = _selectedSubject.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            teacherRepository.fetchAllTeachers()
                .onSuccess { _teachers.value = it }
            
            assessmentRepository.fetchExamNames()
                .onSuccess { _examNames.value = it }
        }
    }

    fun setSelectedTeacher(id: Int) {
        _selectedTeacherId.value = id
        resetSelection(fromStep = 2)
    }

    fun setActiveTab(tab: String) {
        _activeTab.value = tab
        resetSelection(fromStep = 3)
    }

    fun setSelectedExam(name: String) {
        _selectedExamName.value = name
        resetSelection(fromStep = 4)
    }

    fun setSelectedClass(className: String) {
        _selectedClass.value = className
        resetSelection(fromStep = 5)
        loadStudents()
    }

    fun setSelectedSubject(subject: String) {
        _selectedSubject.value = subject
    }

    private fun resetSelection(fromStep: Int) {
        if (fromStep <= 2) _selectedTeacherId.value = _selectedTeacherId.value
        if (fromStep <= 3) _selectedExamName.value = null
        if (fromStep <= 4) _selectedClass.value = null
        if (fromStep <= 5) {
            _selectedSubject.value = null
            _state.value = GradeState.Loading
        }
    }

    fun loadStudents() {
        val className = _selectedClass.value ?: return
        viewModelScope.launch {
            _state.value = GradeState.Loading
            studentRepository.fetchStudentList() // Ideally by class
                .onSuccess { students ->
                    val rows = students.map {
                        StudentGradeRow(it.id ?: it.studentId ?: 0, it.studentName ?: it.name ?: "")
                    }
                    _state.value = GradeState.Success(rows)
                }
        }
    }

    fun updateMarks(studentId: Int, marks: String) {
        val currentState = _state.value
        if (currentState is GradeState.Success) {
            val updated = currentState.students.map {
                if (it.studentId == studentId) it.copy(marks = marks) else it
            }
            _state.value = GradeState.Success(updated)
        }
    }

    fun saveGrades() {
        val currentState = _state.value
        if (currentState is GradeState.Success) {
            viewModelScope.launch {
                _isSaving.value = true
                val payloads = currentState.students.filter { it.marks.isNotBlank() }.map {
                    ExamGradePayload(
                        studentId = it.studentId,
                        teacherId = _selectedTeacherId.value ?: 1,
                        subject = _selectedSubject.value ?: "",
                        classNo = _selectedClass.value ?: "",
                        mark = it.marks.toDoubleOrNull() ?: 0.0
                    )
                }
                assessmentRepository.saveExamMarks(payloads)
                    .onSuccess { /* Show success */ }
                _isSaving.value = false
            }
        }
    }
}
