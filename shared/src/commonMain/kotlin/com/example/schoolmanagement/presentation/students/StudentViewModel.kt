package com.example.schoolmanagement.presentation.students

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schoolmanagement.api.StudentRepository
import com.example.schoolmanagement.api.models.StudentListItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class StudentListState {
    object Loading : StudentListState()
    data class Success(val students: List<StudentListItem>) : StudentListState()
    data class Error(val message: String) : StudentListState()
}

class StudentViewModel(private val repository: StudentRepository) : ViewModel() {
    private val _state = MutableStateFlow<StudentListState>(StudentListState.Loading)
    val state: StateFlow<StudentListState> = _state.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isAddStudentDialogOpen = MutableStateFlow(false)
    val isAddStudentDialogOpen: StateFlow<Boolean> = _isAddStudentDialogOpen.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private var allStudents: List<StudentListItem> = emptyList()

    init {
        loadStudents()
    }

    fun setAddStudentDialogOpen(open: Boolean) {
        _isAddStudentDialogOpen.value = open
    }

    fun addStudent(request: com.example.schoolmanagement.api.models.StudentCreateRequest) {
        viewModelScope.launch {
            _isSaving.value = true
            repository.saveStudent(request)
                .onSuccess {
                    _isAddStudentDialogOpen.value = false
                    loadStudents()
                }
                .onFailure {
                    // Handle error (could add an error state for the dialog)
                }
            _isSaving.value = false
        }
    }

    fun loadStudents() {
        viewModelScope.launch {
            _state.value = StudentListState.Loading
            repository.fetchStudentList()
                .onSuccess { students ->
                    allStudents = students
                    applyFilter()
                }
                .onFailure { error ->
                    _state.value = StudentListState.Error(error.message ?: "Unknown error")
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        applyFilter()
    }

    private fun applyFilter() {
        val query = _searchQuery.value.lowercase()
        val filtered = if (query.isBlank()) {
            allStudents
        } else {
            allStudents.filter {
                it.studentName?.lowercase()?.contains(query) == true ||
                it.scholarNo?.lowercase()?.contains(query) == true
            }
        }
        _state.value = StudentListState.Success(filtered)
    }
}
