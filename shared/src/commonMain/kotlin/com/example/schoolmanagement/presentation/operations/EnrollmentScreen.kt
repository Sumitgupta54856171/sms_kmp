package com.example.schoolmanagement.presentation.operations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.UserPlus

@Composable
fun EnrollmentScreen(viewModel: EnrollmentViewModel) {
    val state by viewModel.state.collectAsState()
    val selectedClass by viewModel.selectedClass.collectAsState()
    val isPromoting by viewModel.isPromoting.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp)
    ) {
        Text("Enrollment & Promotion", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
        Text("Manage student promotion to next academic session", fontSize = 14.sp, color = Color(0xFF64748B), modifier = Modifier.padding(bottom = 24.dp))

        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Current Class", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text("Grade $selectedClass", fontSize = 18.sp, fontWeight = FontWeight.Black)
                }
                
                Button(
                    onClick = { viewModel.promoteStudents("2", 15000.0) }, // Simplified promotion
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                    enabled = !isPromoting && state is EnrollmentState.Success,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isPromoting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                    } else {
                        Icon(FontAwesomeIcons.Solid.UserPlus, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Promote to Next Grade")
                    }
                }
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (val s = state) {
                is EnrollmentState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF0D9488))
                is EnrollmentState.Error -> Text(s.message, color = Color.Red, modifier = Modifier.align(Alignment.Center))
                is EnrollmentState.Success -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(s.students) { student ->
                            StudentEnrollmentRow(student)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentEnrollmentRow(student: com.example.schoolmanagement.api.models.StudentListItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(student.studentName ?: student.name ?: "-", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("Scholar No: ${student.scholarNo ?: student.scholar_no ?: "-"}", fontSize = 12.sp, color = Color.Gray)
            }
            
            Badge(containerColor = Color(0xFFF0FDFA), contentColor = Color(0xFF0D9488)) {
                Text("Eligible", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
