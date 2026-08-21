package com.example.schoolmanagement.presentation.academics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schoolmanagement.api.StudentRepository
import com.example.schoolmanagement.api.models.BulkRollNoPayload
import com.example.schoolmanagement.api.models.StudentListItem
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ClassStudentState {
    object Loading : ClassStudentState()
    data class Success(val students: List<StudentListItem>, val classTeacher: String?) : ClassStudentState()
    data class Error(val message: String) : ClassStudentState()
}

class ClassStudentViewModel(
    private val repository: StudentRepository,
    private val classNo: String
) : ViewModel() {
    private val _state = MutableStateFlow<ClassStudentState>(ClassStudentState.Loading)
    val state: StateFlow<ClassStudentState> = _state.asStateFlow()

    private val _rollNos = MutableStateFlow<Map<Int, String>>(emptyMap())
    val rollNos: StateFlow<Map<Int, String>> = _rollNos.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    init {
        loadStudents()
    }

    fun loadStudents() {
        viewModelScope.launch {
            _state.value = ClassStudentState.Loading
            repository.fetchStudentsByClass(classNo)
                .onSuccess { students ->
                    _state.value = ClassStudentState.Success(students, null) // Repository currently doesn't return teacher name in parseList
                    val initialRolls = students.associate { 
                        (it.studentId ?: it.id ?: 0) to (it.rolleNo ?: it.roll_no ?: it.rollNo ?: "")
                    }
                    _rollNos.value = initialRolls
                }
                .onFailure { error ->
                    _state.value = ClassStudentState.Error(error.message ?: "Failed to load students")
                }
        }
    }

    fun onRollNoChange(studentId: Int, value: String) {
        _rollNos.value = _rollNos.value + (studentId to value)
    }

    fun saveRollNumbers() {
        viewModelScope.launch {
            _isSaving.value = true
            val payload = _rollNos.value.map { (id, roll) ->
                BulkRollNoPayload(studentId = id, rollno = roll)
            }
            repository.updateBulkRollNo(payload)
                .onSuccess {
                    // Success toast or state
                }
            _isSaving.value = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassStudentListScreen(viewModel: ClassStudentViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsState()
    val rollNos by viewModel.rollNos.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(FontAwesomeIcons.Solid.ArrowLeft, null, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("Class Students", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E293B))
                    if (state is ClassStudentState.Success) {
                        Text("${(state as ClassStudentState.Success).students.size} Students enrolled", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            Button(
                onClick = { viewModel.saveRollNumbers() },
                enabled = !isSaving && state is ClassStudentState.Success,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (isSaving) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                else {
                    Icon(FontAwesomeIcons.Solid.Save, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Save Roll Nos", fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Table Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFFF1F5F9),
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Roll No", modifier = Modifier.width(80.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF64748B))
                Text("Student Name", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF64748B))
                Text("Scholar No", modifier = Modifier.width(100.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF64748B))
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (val s = state) {
                is ClassStudentState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF0D9488))
                is ClassStudentState.Error -> Text(s.message, color = Color.Red, modifier = Modifier.align(Alignment.Center))
                is ClassStudentState.Success -> {
                    if (s.students.isEmpty()) {
                        Text("No students found in this class", modifier = Modifier.align(Alignment.Center), color = Color.Gray)
                    } else {
                        LazyColumn {
                            items(s.students) { student ->
                                StudentRow(
                                    student = student,
                                    rollNo = rollNos[student.studentId ?: student.id ?: 0] ?: "",
                                    onRollNoChange = { viewModel.onRollNoChange(student.studentId ?: student.id ?: 0, it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentRow(student: StudentListItem, rollNo: String, onRollNoChange: (String) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFF1F5F9))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = rollNo,
                onValueChange = onRollNoChange,
                modifier = Modifier.width(70.dp).height(48.dp),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6366F1),
                    unfocusedBorderColor = Color(0xFFE2E8F0)
                )
            )
            
            Spacer(Modifier.width(16.dp))

            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFFEEF2FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Text((student.studentName ?: student.name ?: "S").take(1).uppercase(), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6366F1))
                }
                Spacer(Modifier.width(12.dp))
                Text(student.studentName ?: student.name ?: "-", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            }

            Text(student.scholarNo ?: student.scholar_no ?: "-", modifier = Modifier.width(100.dp), fontSize = 13.sp, color = Color(0xFF64748B))
        }
    }
}
