package com.example.schoolmanagement.presentation.teachers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schoolmanagement.api.TeacherRepository
import com.example.schoolmanagement.api.models.TeacherData
import com.example.schoolmanagement.api.models.TeacherResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class TeacherListState {
    object Loading : TeacherListState()
    data class Success(val teachers: List<TeacherResponse>) : TeacherListState()
    data class Error(val message: String) : TeacherListState()
}

class TeacherViewModel(private val repository: TeacherRepository) : ViewModel() {
    private val _state = MutableStateFlow<TeacherListState>(TeacherListState.Loading)
    val state: StateFlow<TeacherListState> = _state.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isAddDialogOpen = MutableStateFlow(false)
    val isAddDialogOpen: StateFlow<Boolean> = _isAddDialogOpen.asStateFlow()

    private val _editingTeacher = MutableStateFlow<TeacherResponse?>(null)
    val editingTeacher: StateFlow<TeacherResponse?> = _editingTeacher.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private var allTeachers: List<TeacherResponse> = emptyList()

    init {
        loadTeachers()
    }

    fun loadTeachers() {
        viewModelScope.launch {
            _state.value = TeacherListState.Loading
            repository.fetchAllTeachers()
                .onSuccess { teachers ->
                    allTeachers = teachers
                    applyFilter()
                }
                .onFailure { error ->
                    _state.value = TeacherListState.Error(error.message ?: "Failed to load teachers")
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
            allTeachers
        } else {
            allTeachers.filter {
                it.fullName.lowercase().contains(query) ||
                it.email.lowercase().contains(query) ||
                it.employee_id.lowercase().contains(query)
            }
        }
        _state.value = TeacherListState.Success(filtered)
    }

    fun setAddDialogOpen(open: Boolean, teacher: TeacherResponse? = null) {
        _editingTeacher.value = teacher
        _isAddDialogOpen.value = open
    }

    fun saveTeacher(teacher: TeacherData) {
        viewModelScope.launch {
            _isSaving.value = true
            val result = if (_editingTeacher.value != null) {
                repository.updateTeacher(_editingTeacher.value!!.id, teacher)
            } else {
                repository.saveTeacher(teacher)
            }
            
            result.onSuccess {
                _isAddDialogOpen.value = false
                _editingTeacher.value = null
                loadTeachers()
            }.onFailure {
                // Handle error
            }
            _isSaving.value = false
        }
    }

    fun deleteTeacher(id: Int) {
        viewModelScope.launch {
            repository.deleteTeacher(id)
                .onSuccess { loadTeachers() }
        }
    }
}
