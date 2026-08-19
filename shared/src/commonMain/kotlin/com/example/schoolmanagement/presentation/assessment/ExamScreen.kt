package com.example.schoolmanagement.presentation.assessment

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.schoolmanagement.api.models.ExamTimetableEntry
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.*

@Composable
fun ExamScreen(viewModel: ExamViewModel) {
    val state by viewModel.state.collectAsState()
    val timetableState by viewModel.timetableState.collectAsState()

    var selectedExam by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp)
    ) {
        Text("Examinations", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
        Text("View schedules and manage assessments", fontSize = 14.sp, color = Color(0xFF64748B), modifier = Modifier.padding(bottom = 24.dp))

        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // Exam List
            Card(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Available Exams", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D9488))
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    when (val s = state) {
                        is ExamState.Loading -> CircularProgressIndicator(color = Color(0xFF0D9488))
                        is ExamState.Error -> Text(s.message, color = Color.Red)
                        is ExamState.Success -> {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(s.exams) { exam ->
                                    ExamItem(
                                        name = exam,
                                        isSelected = selectedExam == exam,
                                        onClick = {
                                            selectedExam = exam
                                            viewModel.loadTimetable(exam)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Timetable View
            Card(
                modifier = Modifier.weight(2f).fillMaxHeight(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = selectedExam ?: "Select an exam to view timetable",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedExam != null) Color(0xFF0D9488) else Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    when (val t = timetableState) {
                        is ExamTimetableState.Idle -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No exam selected", color = Color.LightGray)
                            }
                        }
                        is ExamTimetableState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = Color(0xFF0D9488))
                        is ExamTimetableState.Error -> Text(t.message, color = Color.Red)
                        is ExamTimetableState.Success -> {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(t.entries) { entry ->
                                    ExamTimetableRow(entry)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExamItem(name: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = if (isSelected) Color(0xFFF0FDFA) else Color.Transparent,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = FontAwesomeIcons.Solid.FileAlt,
                contentDescription = null,
                tint = if (isSelected) Color(0xFF0D9488) else Color.Gray,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = name,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color(0xFF0D9488) else Color(0xFF475569)
            )
        }
    }
}

@Composable
fun ExamTimetableRow(entry: ExamTimetableEntry) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFF8FAFC),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.subject, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Grade ${entry.classNO}", fontSize = 12.sp, color = Color.Gray)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(entry.date, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0D9488))
                Text("${entry.startTime} - ${entry.endTime}", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}
