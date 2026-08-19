package com.example.schoolmanagement.presentation.students

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schoolmanagement.api.StudentRepository
import com.example.schoolmanagement.api.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class StudentProfileState {
    object Loading : StudentProfileState()
    data class Success(val data: StudentDetailResponse) : StudentProfileState()
    data class Error(val message: String) : StudentProfileState()
}

class StudentProfileViewModel(private val repository: StudentRepository) : ViewModel() {
    private val _state = MutableStateFlow<StudentProfileState>(StudentProfileState.Loading)
    val state: StateFlow<StudentProfileState> = _state.asStateFlow()

    private val _isSavingBank = MutableStateFlow(false)
    val isSavingBank: StateFlow<Boolean> = _isSavingBank.asStateFlow()

    fun loadStudentProfile(studentId: Int) {
        viewModelScope.launch {
            _state.value = StudentProfileState.Loading
            repository.fetchStudentDetail(studentId)
                .onSuccess { _state.value = StudentProfileState.Success(it) }
                .onFailure { _state.value = StudentProfileState.Error(it.message ?: "Unknown error") }
        }
    }

    fun saveBankDetails(studentId: Int, bankData: BankDetailData) {
        viewModelScope.launch {
            _isSavingBank.value = true
            repository.saveBankDetails(bankData.copy(studentId = studentId))
                .onSuccess { loadStudentProfile(studentId) }
                .onFailure { /* handle error */ }
            _isSavingBank.value = false
        }
    }

    fun removePhoto(studentId: Int) {
        viewModelScope.launch {
            repository.deleteStudentPhoto(studentId)
                .onSuccess { loadStudentProfile(studentId) }
        }
    }
}
