package com.example.schoolmanagement.presentation.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schoolmanagement.api.AttendanceRepository
import com.example.schoolmanagement.api.StudentRepository
import com.example.schoolmanagement.api.models.AttendancePayload
import com.example.schoolmanagement.api.models.AttendanceRecord
import com.example.schoolmanagement.api.models.StudentListItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import com.example.schoolmanagement.api.getCurrentEpochMillis

sealed class AttendanceState {
    object Loading : AttendanceState()
    data class Success(val students: List<AttendanceStudentRow>) : AttendanceState()
    data class Error(val message: String) : AttendanceState()
}

data class AttendanceStudentRow(
    val id: Int,
    val name: String,
    val rollNumber: String,
    val scholarNo: String,
    val status: String? = null // "present", "absent", "holiday"
)

class AttendanceViewModel(
    private val attendanceRepository: AttendanceRepository,
    private val studentRepository: StudentRepository
) : ViewModel() {

    private val _state = MutableStateFlow<AttendanceState>(AttendanceState.Loading)
    val state: StateFlow<AttendanceState> = _state.asStateFlow()

    private val _selectedClass = MutableStateFlow("1")
    val selectedClass: StateFlow<String> = _selectedClass.asStateFlow()

    private val _selectedDate = MutableStateFlow(getCurrentDateStr())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    init {
        loadAttendanceData()
    }

    private fun getCurrentDateStr(): String {
        val currentMoment = Instant.fromEpochMilliseconds(getCurrentEpochMillis())
        val now = currentMoment.toLocalDateTime(TimeZone.currentSystemDefault())
        val month = now.monthNumber.toString().padStart(2, '0')
        val day = now.dayOfMonth.toString().padStart(2, '0')
        return "${now.year}-$month-$day"
    }

    fun setSelectedClass(className: String) {
        _selectedClass.value = className
        loadAttendanceData()
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
        loadAttendanceData()
    }

    fun loadAttendanceData() {
        viewModelScope.launch {
            _state.value = AttendanceState.Loading
            
            // Use class-specific fetch as it's more reliable for attendance marking
            val normalizedSelectedClass = _selectedClass.value.replace("Grade ", "", ignoreCase = true).trim()
            val studentsResult = studentRepository.fetchStudentsByClass(normalizedSelectedClass)
            val attendanceResult = attendanceRepository.fetchAttendanceByDate(_selectedDate.value)

            if (studentsResult.isSuccess && attendanceResult.isSuccess) {
                val students = studentsResult.getOrNull() ?: emptyList()
                val attendance = attendanceResult.getOrNull() ?: emptyList()
                
                val rows = students.map { student ->
                    val record = attendance.find { it.studentId == student.id || it.studentId == student.studentId }
                    AttendanceStudentRow(
                        id = student.id ?: student.studentId ?: 0,
                        name = student.studentName ?: student.name ?: "Unknown",
                        rollNumber = student.rollNo ?: student.roll_no ?: "-",
                        scholarNo = student.scholarNo ?: student.scholar_no ?: "-",
                        status = record?.status?.lowercase() // Normalize for UI
                    )
                }
                _state.value = AttendanceState.Success(rows.sortedBy { it.rollNumber.toIntOrNull() ?: Int.MAX_VALUE })
            } else {
                _state.value = AttendanceState.Error("Failed to load data")
            }
        }
    }

    fun updateStatus(studentId: Int, status: String) {
        val currentState = _state.value
        if (currentState is AttendanceState.Success) {
            val updatedList = currentState.students.map {
                if (it.id == studentId) it.copy(status = status) else it
            }
            _state.value = AttendanceState.Success(updatedList)
        }
    }

    fun saveAttendance() {
        val currentState = _state.value
        if (currentState is AttendanceState.Success) {
            viewModelScope.launch {
                _isSaving.value = true
                val payload = currentState.students.filter { it.status != null }.map {
                    AttendancePayload(
                        attendanceDate = _selectedDate.value,
                        studentId = it.id,
                        status = it.status!!.uppercase(), // Send uppercase to backend
                        grade = _selectedClass.value
                    )
                }
                attendanceRepository.saveAttendance(payload)
                    .onSuccess { loadAttendanceData() }
                _isSaving.value = false
            }
        }
    }

    fun updateIndividualAttendance(studentId: Int, status: String?) {
        if (status == null) return
        viewModelScope.launch {
            attendanceRepository.updateAttendance(studentId, status, _selectedDate.value)
                .onSuccess { loadAttendanceData() }
        }
    }
}
