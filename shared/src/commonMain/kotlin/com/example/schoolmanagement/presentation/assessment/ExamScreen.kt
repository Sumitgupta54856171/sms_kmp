package com.example.schoolmanagement.presentation.assessment

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.schoolmanagement.presentation.components.ExpressiveDropdown
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.*
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamScreen(viewModel: ExamViewModel) {
    val state by viewModel.state.collectAsState()
    val timetableState by viewModel.timetableState.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()
    val selectedExamName by viewModel.selectedExamName.collectAsState()
    val selectedGrade by viewModel.selectedGrade.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var formType by remember { mutableStateOf("test") }

    val grades = listOf("Nursery", "LKG", "UKG") + (1..12).map { it.toString() }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        val isCompact = maxWidth < 800.dp
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isCompact) 16.dp else 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeaderSection(isCompact, Modifier.weight(1f))
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { 
                            formType = "test"
                            showAddDialog = true 
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF475569)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        shape = MaterialTheme.shapes.small,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(FontAwesomeIcons.Solid.Plus, null, modifier = Modifier.size(14.dp))
                        if (!isCompact) {
                            Spacer(Modifier.width(6.dp))
                            Text("Add Test", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = { 
                            formType = "exam"
                            showAddDialog = true 
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                        shape = MaterialTheme.shapes.small,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(FontAwesomeIcons.Solid.Plus, null, modifier = Modifier.size(14.dp))
                        if (!isCompact) {
                            Spacer(Modifier.width(6.dp))
                            Text("Add Exam", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (showAddDialog) {
                TimetableFormDialog(
                    type = formType,
                    onDismiss = { showAddDialog = false },
                    onConfirm = { entries ->
                        viewModel.createTimetableEntries(entries) {
                            showAddDialog = false
                        }
                    },
                    isSaving = isSaving,
                    grades = grades
                )
            }

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
                            t.entries.filter { it.displayClass == selectedGrade }
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
                                val grouped = filteredEntries.sortedBy { it.date ?: "" }.groupBy { it.date ?: "No Date" }
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
private fun HeaderSection(isCompact: Boolean, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = modifier) {
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
        shape = MaterialTheme.shapes.medium,
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
                shape = MaterialTheme.shapes.small,
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
    ExpressiveDropdown(
        label = label,
        items = items,
        onSelect = onSelect,
        icon = icon
    )
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
                Text(entry.subject ?: "Unknown Subject", fontWeight = FontWeight.Bold, fontSize = if (isCompact) 15.sp else 17.sp, color = Color(0xFF1E293B))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(color = Color(0xFFF1F5F9), shape = RoundedCornerShape(4.dp)) {
                        Text("Grade ${entry.displayClass}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                    if (entry.displayMaxMarks != null) {
                        Text("Max: ${entry.displayMaxMarks}", fontSize = 12.sp, color = Color.Gray)
                    }
                }
                if (entry.displayName.isNotBlank()) {
                    Text(entry.displayName, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 2.dp))
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                val timeStr = if (entry.displayStartTime.isNotBlank()) {
                    "${entry.displayStartTime} - ${entry.displayEndTime}"
                } else null
                
                if (timeStr != null) {
                    Text(timeStr, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                }
                Text(entry.day ?: "", fontSize = 12.sp, color = Color(0xFF0D9488), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TimetableFormDialog(
    type: String,
    onDismiss: () -> Unit,
    onConfirm: (List<ExamTimetableEntry>) -> Unit,
    isSaving: Boolean,
    grades: List<String>
) {
    var examName by remember { mutableStateOf("") }
    var examCode by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf("") }
    var selectedGrades by remember { mutableStateOf(setOf<String>()) }
    var selectedDay by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var totalMarks by remember { mutableStateOf("") }

    // Picker States
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()
    val startTimePickerState = rememberTimePickerState()
    val endTimePickerState = rememberTimePickerState()

    val subjects = listOf("Mathematics", "English", "Science", "Social Studies", "Hindi", "Sanskrit", "Computer Science")

    // Date Picker Logic
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val localDate = kotlinx.datetime.Instant.fromEpochMilliseconds(millis)
                            .toLocalDateTime(TimeZone.UTC).date
                        date = localDate.toString() // YYYY-MM-DD
                        selectedDay = localDate.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
                    }
                    showDatePicker = false
                }) { Text("OK", color = Color(0xFF0D9488)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Time Picker Logic (Start)
    if (showStartTimePicker) {
        TimePickerDialog(
            onDismiss = { showStartTimePicker = false },
            onConfirm = {
                val h = startTimePickerState.hour
                val m = startTimePickerState.minute
                val ampm = if (h >= 12) "PM" else "AM"
                val h12 = if (h % 12 == 0) 12 else h % 12
                startTime = "${h12.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')} $ampm"
                showStartTimePicker = false
            }
        ) {
            TimePicker(state = startTimePickerState)
        }
    }

    // Time Picker Logic (End)
    if (showEndTimePicker) {
        TimePickerDialog(
            onDismiss = { showEndTimePicker = false },
            onConfirm = {
                val h = endTimePickerState.hour
                val m = endTimePickerState.minute
                val ampm = if (h >= 12) "PM" else "AM"
                val h12 = if (h % 12 == 0) 12 else h % 12
                endTime = "${h12.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')} $ampm"
                showEndTimePicker = false
            }
        ) {
            TimePicker(state = endTimePickerState)
        }
    }

    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = Color.White,
            modifier = Modifier.fillMaxWidth(0.95f).wrapContentHeight()
        ) {
            Column(
                modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (type == "test") "Create Test Schedule" else "Create Exam Schedule",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1E293B)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(FontAwesomeIcons.Solid.Times, null, modifier = Modifier.size(20.dp), tint = Color.Gray)
                    }
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                // Basic Info
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    FormInput(if (type == "test") "Test Name" else "Exam Name", examName, { examName = it }, "e.g. Unit Test 1")
                    FormInput("Test/Exam Code", examCode, { examCode = it }, "e.g. T-101")
                    
                    Text("Subject", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    SimpleDropdown(selectedSubject, subjects) { selectedSubject = it }
                }

                // Grade Selection (Multi-select)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select Classes (Bulk Create)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        grades.forEach { grade ->
                            val isSelected = selectedGrades.contains(grade)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedGrades = if (isSelected) selectedGrades - grade else selectedGrades + grade
                                },
                                label = { Text(grade, fontSize = 12.sp) }
                            )
                        }
                    }
                }

                // Timing
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Day & Date", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            ReadOnlyInput("Day", selectedDay, { showDatePicker = true }, "Select Date", FontAwesomeIcons.Solid.CalendarDay)
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            ReadOnlyInput("Date", date, { showDatePicker = true }, "YYYY-MM-DD", FontAwesomeIcons.Solid.CalendarAlt)
                        }
                    }

                    if (type == "exam") {
                        Text("Timing", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                ReadOnlyInput("Start", startTime, { showStartTimePicker = true }, "09:00 AM", FontAwesomeIcons.Solid.Clock)
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                ReadOnlyInput("End", endTime, { showEndTimePicker = true }, "12:00 PM", FontAwesomeIcons.Solid.Clock)
                            }
                        }
                    }
                    
                    FormInput("Total Marks", totalMarks, { totalMarks = it }, "100", modifier = Modifier.fillMaxWidth())
                }

                // Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.Gray)
                    }
                    Spacer(Modifier.width(12.dp))
                    Button(
                        onClick = {
                            val entries = selectedGrades.map { grade ->
                                ExamTimetableEntry(
                                    timetableName = examName,
                                    examType = type,
                                    classNO = grade,
                                    subject = selectedSubject,
                                    date = date,
                                    day = selectedDay,
                                    startTime = startTime,
                                    endTime = endTime,
                                    maxMarks = totalMarks.toIntOrNull()
                                )
                            }
                            onConfirm(entries)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                        enabled = !isSaving && examName.isNotBlank() && selectedGrades.isNotEmpty() && selectedSubject.isNotBlank(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isSaving) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                        else Text("Create ${selectedGrades.size} Entries", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun FormInput(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (label.isNotBlank()) {
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, fontSize = 13.sp, color = Color.LightGray) },
            shape = MaterialTheme.shapes.small,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF0D9488),
                unfocusedBorderColor = Color(0xFFE2E8F0)
            )
        )
    }
}

@Composable
private fun SimpleDropdown(selected: String, items: List<String>, onSelect: (String) -> Unit) {
    ExpressiveDropdown(
        label = selected.ifBlank { "Select..." },
        items = items,
        onSelect = onSelect
    )
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
fun ReadOnlyInput(
    label: String,
    value: String,
    onClick: () -> Unit,
    placeholder: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        Surface(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
            color = Color.White
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(icon, null, modifier = Modifier.size(14.dp), tint = Color(0xFF0D9488))
                Text(
                    text = value.ifBlank { placeholder },
                    fontSize = 13.sp,
                    color = if (value.isBlank()) Color.LightGray else Color(0xFF1E293B),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("OK", color = Color(0xFF0D9488)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        text = {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    )
}

@Composable
fun ExamItem(name: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = if (isSelected) Color(0xFFF0FDFA) else Color.Transparent,
        shape = MaterialTheme.shapes.extraSmall
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

