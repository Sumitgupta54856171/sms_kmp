package com.example.schoolmanagement.presentation.assessment

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.schoolmanagement.api.models.ExamTimetableEntry
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamScreen(viewModel: ExamViewModel) {
    val state by viewModel.state.collectAsState()
    val timetableState by viewModel.timetableState.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()
    val selectedExamName by viewModel.selectedExamName.collectAsState()
    val selectedGrade by viewModel.selectedGrade.collectAsState()

    val grades = listOf("Nursery", "LKG", "UKG") + (1..12).map { it.toString() }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        val isCompact = maxWidth < 800.dp
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isCompact) 16.dp else 32.dp)
        ) {
            // Header
            HeaderSection(isCompact)

            Spacer(modifier = Modifier.height(24.dp))

            // Tabs
            TabSection(activeTab) { viewModel.setActiveTab(it) }

            Spacer(modifier = Modifier.height(24.dp))

            // Filters
            FilterSection(
                selectedGrade = selectedGrade,
                selectedExamName = selectedExamName,
                examNames = if (state is ExamState.Success) (state as ExamState.Success).exams else emptyList(),
                activeTab = activeTab,
                onGradeChange = { viewModel.setSelectedGrade(it) },
                onExamNameChange = { viewModel.setSelectedExam(it) },
                onClear = {
                    viewModel.setSelectedGrade(null)
                    viewModel.setSelectedExam("") // Just trigger a reset if needed
                },
                grades = grades,
                isCompact = isCompact
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Timetable Content
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (val t = timetableState) {
                    is ExamTimetableState.Idle -> {
                        EmptyState("Select a ${activeTab} to view the schedule")
                    }
                    is ExamTimetableState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF0D9488))
                    }
                    is ExamTimetableState.Error -> {
                        Text(t.message, color = Color.Red, modifier = Modifier.align(Alignment.Center))
                    }
                    is ExamTimetableState.Success -> {
                        val filteredEntries = if (selectedGrade != null) {
                            t.entries.filter { it.classNO == selectedGrade }
                        } else {
                            t.entries
                        }

                        if (filteredEntries.isEmpty()) {
                            EmptyState("No entries found for the selected filters")
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(bottom = 24.dp)
                            ) {
                                // Group by date
                                val grouped = filteredEntries.sortedBy { it.date }.groupBy { it.date }
                                grouped.forEach { (date, entries) ->
                                    item {
                                        DateHeader(date)
                                    }
                                    items(entries) { entry ->
                                        ExamTimetableRow(entry, isCompact)
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
private fun HeaderSection(isCompact: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(
            modifier = Modifier.size(if (isCompact) 44.dp else 52.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFEEF2FF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(FontAwesomeIcons.Solid.FileAlt, null, modifier = Modifier.size(if (isCompact) 20.dp else 24.dp), tint = Color(0xFF6366F1))
        }
        Column {
            Text(
                "Exam & Test Timetable",
                fontSize = if (isCompact) 22.sp else 28.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF1E293B),
                letterSpacing = (-0.5).sp
            )
            Text("View and manage schedules for assessments", fontSize = 14.sp, color = Color(0xFF64748B))
        }
    }
}

@Composable
private fun TabSection(activeTab: String, onTabChange: (String) -> Unit) {
    TabRow(
        selectedTabIndex = if (activeTab == "test") 0 else 1,
        containerColor = Color.Transparent,
        contentColor = Color(0xFF0D9488),
        divider = {},
        indicator = { tabPositions ->
            if (activeTab == "test" || activeTab == "exam") {
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[if (activeTab == "test") 0 else 1]),
                    color = Color(0xFF0D9488)
                )
            }
        }
    ) {
        Tab(
            selected = activeTab == "test",
            onClick = { onTabChange("test") },
            text = { 
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(FontAwesomeIcons.Solid.Clock, null, modifier = Modifier.size(14.dp))
                    Text("Tests", fontWeight = FontWeight.Bold)
                }
            }
        )
        Tab(
            selected = activeTab == "exam",
            onClick = { onTabChange("exam") },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(FontAwesomeIcons.Solid.GraduationCap, null, modifier = Modifier.size(16.dp))
                    Text("Exams", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun FilterSection(
    selectedGrade: String?,
    selectedExamName: String?,
    examNames: List<String>,
    activeTab: String,
    onGradeChange: (String?) -> Unit,
    onExamNameChange: (String) -> Unit,
    onClear: () -> Unit,
    grades: List<String>,
    isCompact: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        val content = @Composable {
            // Grade Selector
            Box(modifier = if (isCompact) Modifier.fillMaxWidth() else Modifier.weight(1f)) {
                DropdownFilter(
                    label = if (selectedGrade != null) "Grade $selectedGrade" else "All Grades",
                    items = listOf("All Grades") + grades,
                    onSelect = { if (it == "All Grades") onGradeChange(null) else onGradeChange(it) },
                    icon = FontAwesomeIcons.Solid.School
                )
            }

            // Exam Name Selector
            Box(modifier = if (isCompact) Modifier.fillMaxWidth() else Modifier.weight(1.5f)) {
                DropdownFilter(
                    label = selectedExamName ?: "Select ${activeTab.replaceFirstChar { it.uppercase() }}",
                    items = examNames,
                    onSelect = onExamNameChange,
                    icon = if (activeTab == "test") FontAwesomeIcons.Solid.Clock else FontAwesomeIcons.Solid.GraduationCap
                )
            }

            // Clear Button
            Button(
                onClick = onClear,
                modifier = if (isCompact) Modifier.fillMaxWidth() else Modifier.width(100.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = Color(0xFF475569)),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Text("Clear", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        if (isCompact) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                content()
            }
        } else {
            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                content()
            }
        }
    }
}

@Composable
private fun DropdownFilter(label: String, items: List<String>, onSelect: (String) -> Unit, icon: ImageVector) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Surface(
            modifier = Modifier.fillMaxWidth().clickable { expanded = true },
            color = Color(0xFFF8FAFC),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(icon, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E293B))
                }
                Icon(FontAwesomeIcons.Solid.ChevronDown, null, modifier = Modifier.size(10.dp), tint = Color.Gray)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(Color.White)) {
            items.forEach { item ->
                DropdownMenuItem(text = { Text(item) }, onClick = { onSelect(item); expanded = false })
            }
        }
    }
}

@Composable
private fun DateHeader(date: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = date,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF64748B),
            letterSpacing = 1.sp
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
    }
}

@Composable
fun ExamTimetableRow(entry: ExamTimetableEntry, isCompact: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Row(
            modifier = Modifier.padding(if (isCompact) 12.dp else 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier.size(if (isCompact) 40.dp else 48.dp).clip(CircleShape).background(Color(0xFFF0FDFA)),
                contentAlignment = Alignment.Center
            ) {
                Icon(FontAwesomeIcons.Solid.BookOpen, null, modifier = Modifier.size(18.dp), tint = Color(0xFF0D9488))
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.subject, fontWeight = FontWeight.Bold, fontSize = if (isCompact) 15.sp else 17.sp, color = Color(0xFF1E293B))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(color = Color(0xFFF1F5F9), shape = RoundedCornerShape(4.dp)) {
                        Text("Grade ${entry.classNO}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                    if (entry.maxMarks != null) {
                        Text("Max: ${entry.maxMarks}", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text("${entry.startTime} - ${entry.endTime}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                Text(entry.day, fontSize = 12.sp, color = Color(0xFF0D9488), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(FontAwesomeIcons.Solid.CalendarDay, null, modifier = Modifier.size(64.dp), tint = Color(0xFFCBD5E1))
        Spacer(modifier = Modifier.height(16.dp))
        Text(message, color = Color(0xFF94A3B8), fontSize = 15.sp, fontWeight = FontWeight.Medium)
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
