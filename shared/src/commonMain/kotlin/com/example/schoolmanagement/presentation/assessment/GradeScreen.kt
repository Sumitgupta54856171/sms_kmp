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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.*

@Composable
fun GradeScreen(viewModel: GradeViewModel) {
    val state by viewModel.state.collectAsState()
    val teachers by viewModel.teachers.collectAsState()
    val selectedTeacherId by viewModel.selectedTeacherId.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()
    val examNames by viewModel.examNames.collectAsState()
    val selectedExamName by viewModel.selectedExamName.collectAsState()
    val selectedClass by viewModel.selectedClass.collectAsState()
    val selectedSubject by viewModel.selectedSubject.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    val grades = listOf("Nursery", "LKG", "UKG") + (1..12).map { it.toString() }
    val subjects = listOf("Mathematics", "Science", "English", "Hindi", "Social Science")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp)
    ) {
        // Premium Header
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFF0FDFA)),
                contentAlignment = Alignment.Center
            ) {
                Icon(FontAwesomeIcons.Solid.ClipboardCheck, null, modifier = Modifier.size(24.dp), tint = Color(0xFF0D9488))
            }
            Column {
                Text("Grade Entry", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E293B))
                Text("Select filters to fill student marks", fontSize = 13.sp, color = Color(0xFF64748B))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 5-Step Filter Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Step 1: Teacher
                GradeFilterRow("Step 1: Teacher") {
                    GradeDropdown(
                        label = teachers.find { it.id == selectedTeacherId }?.fullName ?: "Select Teacher",
                        items = teachers.map { it.fullName },
                        onSelect = { name -> teachers.find { it.fullName == name }?.let { viewModel.setSelectedTeacher(it.id) } }
                    )
                }

                // Step 2: Type
                GradeFilterRow("Step 2: Type") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = activeTab == "test", onClick = { viewModel.setActiveTab("test") }, label = { Text("Test") })
                        FilterChip(selected = activeTab == "exam", onClick = { viewModel.setActiveTab("exam") }, label = { Text("Exam") })
                    }
                }

                // Step 3: Exam Name
                GradeFilterRow("Step 3: Name") {
                    GradeDropdown(
                        label = selectedExamName ?: "Select ${activeTab.replaceFirstChar { it.uppercase() }}",
                        items = examNames,
                        onSelect = { viewModel.setSelectedExam(it) }
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Step 4: Class
                    Box(modifier = Modifier.weight(1f)) {
                        GradeFilterRow("Step 4: Class") {
                            GradeDropdown(
                                label = if (selectedClass != null) "Grade $selectedClass" else "Select Class",
                                items = grades,
                                onSelect = { viewModel.setSelectedClass(it) }
                            )
                        }
                    }
                    // Step 5: Subject
                    Box(modifier = Modifier.weight(1f)) {
                        GradeFilterRow("Step 5: Subject") {
                            GradeDropdown(
                                label = selectedSubject ?: "Select Subject",
                                items = subjects,
                                onSelect = { viewModel.setSelectedSubject(it) }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Student Table
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (selectedSubject == null) {
                Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(FontAwesomeIcons.Solid.ClipboardList, null, modifier = Modifier.size(48.dp), tint = Color.LightGray)
                    Text("Complete all steps to enter marks", color = Color.Gray, fontSize = 14.sp)
                }
            } else {
                when (val s = state) {
                    is GradeState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF0D9488))
                    is GradeState.Error -> Text(s.message, color = Color.Red, modifier = Modifier.align(Alignment.Center))
                    is GradeState.Success -> {
                        Column {
                            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("STUDENT LIST (${s.students.size})", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF64748B), letterSpacing = 1.sp)
                                Button(
                                    onClick = { viewModel.saveGrades() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                                    enabled = !isSaving,
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    if (isSaving) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                                    else Text("Save All Marks", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(s.students) { row ->
                                    GradeRow(row, onMarksChange = { viewModel.updateMarks(row.studentId, it) })
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
fun GradeFilterRow(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        content()
    }
}

@Composable
fun GradeDropdown(label: String, items: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Surface(
            modifier = Modifier.fillMaxWidth().clickable { expanded = true },
            color = Color(0xFFF8FAFC),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = if (label.startsWith("Select")) Color.Gray else Color(0xFF1E293B))
                Icon(FontAwesomeIcons.Solid.ChevronDown, null, modifier = Modifier.size(10.dp), tint = Color.Gray)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach { item ->
                DropdownMenuItem(text = { Text(item) }, onClick = { onSelect(item); expanded = false })
            }
        }
    }
}

@Composable
fun GradeRow(row: StudentGradeRow, onMarksChange: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(row.name, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            
            OutlinedTextField(
                value = row.marks,
                onValueChange = onMarksChange,
                modifier = Modifier.width(80.dp),
                placeholder = { Text("0.0") },
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF8FAFC),
                    unfocusedContainerColor = Color.White
                )
            )
        }
    }
}
