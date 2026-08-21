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
import com.example.schoolmanagement.presentation.components.ExpressiveDropdown
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

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        val isCompact = maxWidth < 600.dp
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isCompact) 12.dp else 24.dp)
        ) {
            // Premium Header - Responsive
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(if (isCompact) 12.dp else 16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier.size(if (isCompact) 40.dp else 56.dp).clip(MaterialTheme.shapes.medium).background(Color(0xFFF0FDFA)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(FontAwesomeIcons.Solid.ClipboardCheck, null, modifier = Modifier.size(if (isCompact) 20.dp else 28.dp), tint = Color(0xFF0D9488))
                }
                Column {
                    Text("Grade Entry", fontSize = if (isCompact) 22.sp else 28.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E293B))
                    Text("Fill student marks", fontSize = 13.sp, color = Color(0xFF64748B))
                }
            }

            Spacer(modifier = Modifier.height(if (isCompact) 16.dp else 24.dp))

            // 5-Step Filter Card - Responsive
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = MaterialTheme.shapes.large,
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(if (isCompact) 16.dp else 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
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

                    if (isCompact) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            GradeFilterRow("Step 4: Class") {
                                GradeDropdown(
                                    label = if (selectedClass != null) "Grade $selectedClass" else "Select Class",
                                    items = grades,
                                    onSelect = { viewModel.setSelectedClass(it) }
                                )
                            }
                            GradeFilterRow("Step 5: Subject") {
                                GradeDropdown(
                                    label = selectedSubject ?: "Select Subject",
                                    items = subjects,
                                    onSelect = { viewModel.setSelectedSubject(it) }
                                )
                            }
                        }
                    } else {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                GradeFilterRow("Step 4: Class") {
                                    GradeDropdown(
                                        label = if (selectedClass != null) "Grade $selectedClass" else "Select Class",
                                        items = grades,
                                        onSelect = { viewModel.setSelectedClass(it) }
                                    )
                                }
                            }
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
            }

            Spacer(modifier = Modifier.height(if (isCompact) 16.dp else 24.dp))

            // Student Table
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (selectedSubject == null) {
                    Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(FontAwesomeIcons.Solid.ClipboardList, null, modifier = Modifier.size(48.dp), tint = Color.LightGray)
                        Text("Complete steps to enter marks", color = Color.Gray, fontSize = 14.sp)
                    }
                } else {
                    when (val s = state) {
                        is GradeState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF0D9488))
                        is GradeState.Error -> Text(s.message, color = Color.Red, modifier = Modifier.align(Alignment.Center))
                        is GradeState.Success -> {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("STUDENT LIST (${s.students.size})", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF64748B), letterSpacing = 1.sp)
                                    Button(
                                        onClick = { viewModel.saveGrades() },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                                        enabled = !isSaving,
                                        shape = MaterialTheme.shapes.small,
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        if (isSaving) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                        else Text("Save Marks", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
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
    ExpressiveDropdown(
        label = label,
        items = items,
        onSelect = onSelect
    )
}

@Composable
fun GradeRow(row: StudentGradeRow, onMarksChange: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = MaterialTheme.shapes.small,
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
                shape = MaterialTheme.shapes.extraSmall,
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF8FAFC),
                    unfocusedContainerColor = Color.White
                )
            )
        }
    }
}
