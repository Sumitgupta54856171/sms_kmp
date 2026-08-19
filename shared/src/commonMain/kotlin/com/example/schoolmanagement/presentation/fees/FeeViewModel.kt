package com.example.schoolmanagement.presentation.fees

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schoolmanagement.api.FeeRepository
import com.example.schoolmanagement.api.models.StudentListItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FeeViewModel(private val repository: FeeRepository) : ViewModel() {
    private val _state = MutableStateFlow<FeeListState>(FeeListState.Loading)
    val state: StateFlow<FeeListState> = _state.asStateFlow()

    private val _selectedClass = MutableStateFlow("All Classes")
    val selectedClass: StateFlow<String> = _selectedClass.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOrder = MutableStateFlow("asc")
    val sortOrder: StateFlow<String> = _sortOrder.asStateFlow()

    private var allStudents: List<StudentListItem> = emptyList()

    init {
        loadStudents()
    }

    fun setSelectedClass(className: String) {
        _selectedClass.value = className
        loadStudents()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        applyFilter()
    }

    fun setSortOrder(order: String) {
        _sortOrder.value = order
        applyFilter()
    }

    fun loadStudents() {
        viewModelScope.launch {
            _state.value = FeeListState.Loading
            val className = _selectedClass.value
            if (className == "All Classes") {
                // For now, we use a generic fetch or handle "All Classes" logic
                // Assuming we might need an "all students" fetcher in FeeRepository or reuse StudentRepository
                // For simplicity in this module, we'll fetch a default class if "All" is not supported by API
                repository.fetchStudentsByClass("1")
                    .onSuccess { students ->
                        allStudents = students
                        applyFilter()
                    }
                    .onFailure { error ->
                        _state.value = FeeListState.Error(error.message ?: "Unknown error")
                    }
            } else {
                repository.fetchStudentsByClass(className)
                    .onSuccess { students ->
                        allStudents = students
                        applyFilter()
                    }
                    .onFailure { error ->
                        _state.value = FeeListState.Error(error.message ?: "Unknown error")
                    }
            }
        }
    }

    private fun applyFilter() {
        val query = _searchQuery.value.lowercase()
        var filtered = if (query.isBlank()) {
            allStudents
        } else {
            allStudents.filter {
                it.studentName?.lowercase()?.contains(query) == true ||
                it.scholarNo?.lowercase()?.contains(query) == true ||
                it.rollNo?.lowercase()?.contains(query) == true
            }
        }

        filtered = if (_sortOrder.value == "asc") {
            filtered.sortedBy { it.rollNo?.toIntOrNull() ?: 0 }
        } else {
            filtered.sortedByDescending { it.rollNo?.toIntOrNull() ?: 0 }
        }

        _state.value = FeeListState.Success(filtered)
    }
}
