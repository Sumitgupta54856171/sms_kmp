package com.example.schoolmanagement.presentation.operations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schoolmanagement.api.StudentRepository
import com.example.schoolmanagement.api.models.StudentListItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class TCState {
    object Idle : TCState()
    object Loading : TCState()
    data class Success(val student: StudentListItem) : TCState()
    data class Error(val message: String) : TCState()
}

class TCViewModel(private val studentRepository: StudentRepository) : ViewModel() {
    private val _state = MutableStateFlow<TCState>(TCState.Idle)
    val state: StateFlow<TCState> = _state.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun searchStudent(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _state.value = TCState.Idle
            return
        }
        
        viewModelScope.launch {
            _state.value = TCState.Loading
            studentRepository.fetchStudentList()
                .onSuccess { students ->
                    val student = students.find { it.scholarNo == query || it.scholar_no == query }
                    if (student != null) {
                        _state.value = TCState.Success(student)
                    } else {
                        _state.value = TCState.Error("Student not found")
                    }
                }
                .onFailure {
                    _state.value = TCState.Error("Failed to fetch students")
                }
        }
    }
}
