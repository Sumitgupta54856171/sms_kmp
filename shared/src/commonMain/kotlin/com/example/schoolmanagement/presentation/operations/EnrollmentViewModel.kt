package com.example.schoolmanagement.presentation.operations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schoolmanagement.api.OperationsRepository
import com.example.schoolmanagement.api.StudentRepository
import com.example.schoolmanagement.api.models.EnrollmentRequest
import com.example.schoolmanagement.api.models.StudentListItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class EnrollmentState {
    object Loading : EnrollmentState()
    data class Success(val students: List<StudentListItem>) : EnrollmentState()
    data class Error(val message: String) : EnrollmentState()
}

class EnrollmentViewModel(
    private val operationsRepository: OperationsRepository,
    private val studentRepository: StudentRepository
) : ViewModel() {

    private val _state = MutableStateFlow<EnrollmentState>(EnrollmentState.Loading)
    val state: StateFlow<EnrollmentState> = _state.asStateFlow()

    private val _selectedClass = MutableStateFlow("1")
    val selectedClass: StateFlow<String> = _selectedClass.asStateFlow()

    private val _isPromoting = MutableStateFlow(false)
    val isPromoting: StateFlow<Boolean> = _isPromoting.asStateFlow()

    init {
        loadStudents()
    }

    fun setSelectedClass(className: String) {
        _selectedClass.value = className
        loadStudents()
    }

    fun loadStudents() {
        viewModelScope.launch {
            _state.value = EnrollmentState.Loading
            studentRepository.fetchStudentList()
                .onSuccess { students ->
                    _state.value = EnrollmentState.Success(students)
                }
                .onFailure { error ->
                    _state.value = EnrollmentState.Error(error.message ?: "Failed to load students")
                }
        }
    }

    fun promoteStudents(nextClass: String, fees: Double) {
        val currentState = _state.value
        if (currentState is EnrollmentState.Success) {
            viewModelScope.launch {
                _isPromoting.value = true
                val requests = currentState.students.map {
                    EnrollmentRequest(
                        studentId = it.id ?: it.studentId ?: 0,
                        classNo = nextClass,
                        rolNo = it.rollNo ?: it.roll_no ?: "0",
                        Totalfees = fees
                    )
                }
                operationsRepository.promoteStudents(requests)
                    .onSuccess { loadStudents() }
                _isPromoting.value = false
            }
        }
    }
}
