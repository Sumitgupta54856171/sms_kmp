package com.example.schoolmanagement.presentation.operations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schoolmanagement.api.OperationsRepository
import com.example.schoolmanagement.api.StudentRepository
import com.example.schoolmanagement.api.models.RegisterRolePayload
import com.example.schoolmanagement.api.models.StudentListItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LoginGenerateState {
    object Loading : LoginGenerateState()
    data class Success(val students: List<StudentListItem>) : LoginGenerateState()
    data class Error(val message: String) : LoginGenerateState()
}

class LoginGenerateViewModel(
    private val operationsRepository: OperationsRepository,
    private val studentRepository: StudentRepository
) : ViewModel() {

    private val _state = MutableStateFlow<LoginGenerateState>(LoginGenerateState.Loading)
    val state: StateFlow<LoginGenerateState> = _state.asStateFlow()

    private val _selectedIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedIds: StateFlow<Set<Int>> = _selectedIds.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    init {
        loadStudents()
    }

    fun loadStudents() {
        viewModelScope.launch {
            _state.value = LoginGenerateState.Loading
            studentRepository.fetchStudentList()
                .onSuccess { students ->
                    _state.value = LoginGenerateState.Success(students)
                }
                .onFailure { error ->
                    _state.value = LoginGenerateState.Error(error.message ?: "Failed to load students")
                }
        }
    }

    fun toggleStudentSelection(id: Int) {
        val current = _selectedIds.value.toMutableSet()
        if (current.contains(id)) current.remove(id)
        else current.add(id)
        _selectedIds.value = current
    }

    fun generateLogins(role: String, password: String) {
        val currentState = _state.value
        if (currentState is LoginGenerateState.Success && _selectedIds.value.isNotEmpty()) {
            viewModelScope.launch {
                _isGenerating.value = true
                val selectedStudents = currentState.students.filter { _selectedIds.value.contains(it.id ?: it.studentId) }
                val payloads = selectedStudents.map { student ->
                    RegisterRolePayload(
                        username = if (role == "STUDENT") student.scholarNo ?: student.scholar_no else (student.faterhName ?: student.father_name),
                        email = (student.id ?: student.studentId ?: 0).toString(),
                        password = password,
                        role = role
                    )
                }
                operationsRepository.generateLogins(payloads)
                    .onSuccess {
                        _selectedIds.value = emptySet()
                        // Maybe show success toast via event channel
                    }
                _isGenerating.value = false
            }
        }
    }
}
