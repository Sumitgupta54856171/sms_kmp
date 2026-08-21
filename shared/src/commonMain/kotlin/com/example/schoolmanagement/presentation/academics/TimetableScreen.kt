package com.example.schoolmanagement.presentation.academics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.example.schoolmanagement.api.models.ClassTeacherAssignment
import com.example.schoolmanagement.api.models.PeriodEntry
import com.example.schoolmanagement.api.models.TeacherResponse
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(viewModel: TimetableViewModel) {
    val state by viewModel.state.collectAsState()
    val classTeacherState by viewModel.classTeacherState.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()
    val selectedGrade by viewModel.selectedGrade.collectAsState()
    val selectedTeacherId by viewModel.selectedTeacherId.collectAsState()
    val teachers by viewModel.teachers.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    val userRole = viewModel.userRole?.lowercase()?.removePrefix("role_") ?: "staff"
    val isTeacher = userRole == "teacher"

    var showAssignModal by remember { mutableStateOf(false) }

    val tabs = remember(isTeacher) {
        if (isTeacher) {
            listOf("my-timetable" to "My Timetable", "grade-view" to "Grade View")
        } else {
            listOf("teacher-view" to "Teacher Timetable", "grade-view" to "Grade View", "class-teacher" to "Class Teacher")
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        val isCompact = maxWidth < 600.dp
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isCompact) 16.dp else 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(
                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFF0FDFA)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(FontAwesomeIcons.Solid.CalendarAlt, null, modifier = Modifier.size(24.dp), tint = Color(0xFF0D9488))
                    }
                    Column {
                        Text("Timetable Management", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E293B))
                        Text(
                            if (isTeacher) "View your class schedule" else "Manage teacher-wise timetables",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                if (!isTeacher && activeTab == "teacher-view") {
                    Button(
                        onClick = { showAssignModal = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                        shape = RoundedCornerShape(10.dp),
                        enabled = selectedTeacherId != null
                    ) {
                        Icon(FontAwesomeIcons.Solid.Plus, null, modifier = Modifier.size(16.dp))
                        if (!isCompact) {
                            Spacer(Modifier.width(8.dp))
                            Text("Add Period", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            // Tabs
            ScrollableTabRow(
                selectedTabIndex = tabs.indexOfFirst { it.first == activeTab }.coerceAtLeast(0),
                containerColor = Color.Transparent,
                contentColor = Color(0xFF0D9488),
                edgePadding = 0.dp,
                divider = {},
                indicator = { tabPositions ->
                    val index = tabs.indexOfFirst { it.first == activeTab }.coerceAtLeast(0)
                    if (index < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[index]),
                            color = Color(0xFF0D9488)
                        )
                    }
                }
            ) {
                tabs.forEach { (id, title) ->
                    Tab(
                        selected = activeTab == id,
                        onClick = { viewModel.setActiveTab(id) },
                        text = { 
                            Text(
                                title, 
                                fontSize = 13.sp,
                                fontWeight = if (activeTab == id) FontWeight.Bold else FontWeight.Medium
                            ) 
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (activeTab) {
                    "teacher-view" -> TeacherTimetableContent(
                        state = state,
                        teachers = teachers,
                        selectedTeacherId = selectedTeacherId,
                        onTeacherSelect = { viewModel.setSelectedTeacher(it) },
                        onDelete = { viewModel.removePeriod(it) }
                    )
                    "grade-view" -> GradeTimetableContent(
                        state = state,
                        selectedGrade = selectedGrade,
                        onGradeChange = { viewModel.setSelectedGrade(it) },
                        isTeacher = isTeacher
                    )
                    "my-timetable" -> MyTimetableContent(state)
                    "class-teacher" -> ClassTeacherContent(classTeacherState)
                }
            }
        }
    }

    if (showAssignModal) {
        AssignPeriodDialog(
            onDismiss = { showAssignModal = false },
            onConfirm = { 
                viewModel.addPeriod(it)
                showAssignModal = false
            },
            teachers = teachers,
            preselectedTeacherId = selectedTeacherId,
            isSaving = isSaving
        )
    }
}

@Composable
fun TeacherTimetableContent(
    state: TimetableState,
    teachers: List<TeacherResponse>,
    selectedTeacherId: Int?,
    onTeacherSelect: (Int) -> Unit,
    onDelete: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        InfoBanner(
            icon = FontAwesomeIcons.Solid.CalendarAlt,
            text = "Teacher-Wise Timetable — Select a teacher to view and manage their assigned periods.",
            color = Color(0xFF3B82F6)
        )

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DropdownSelector(
                label = teachers.find { it.id == selectedTeacherId }?.fullName ?: "Select Teacher...",
                items = teachers.map { it.fullName },
                onSelect = { name -> teachers.find { it.fullName == name }?.let { onTeacherSelect(it.id) } },
                modifier = Modifier.width(260.dp)
            )
        }

        TimetableList(state, showGrade = true, showTeacher = false, onDelete = onDelete)
    }
}

@Composable
fun GradeTimetableContent(state: TimetableState, selectedGrade: String, onGradeChange: (String) -> Unit, isTeacher: Boolean) {
    val grades = listOf("Nursery", "LKG", "UKG") + (1..12).map { "Grade $it" }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        InfoBanner(
            icon = FontAwesomeIcons.Solid.BookOpen,
            text = "Grade-Wise Timetable — Select a grade to see which teacher teaches each period.",
            color = Color(0xFFF59E0B)
        )

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DropdownSelector(
                label = if (selectedGrade == "__placeholder__") "Select Grade..." else selectedGrade,
                items = grades,
                onSelect = onGradeChange,
                enabled = !isTeacher,
                modifier = Modifier.width(200.dp)
            )
        }

        TimetableList(state, showGrade = false, showTeacher = true)
    }
}

@Composable
fun MyTimetableContent(state: TimetableState) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        InfoBanner(
            icon = FontAwesomeIcons.Solid.User,
            text = "My Timetable — Showing all your assigned periods across grades.",
            color = Color(0xFF0D9488)
        )
        TimetableList(state, showGrade = true, showTeacher = false)
    }
}

@Composable
fun ClassTeacherContent(state: ClassTeacherState) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        InfoBanner(
            icon = FontAwesomeIcons.Solid.UserCheck,
            text = "Class teachers are automatically assigned by the system when periods are assigned.",
            color = Color(0xFF3B82F6)
        )

        Box(modifier = Modifier.fillMaxSize()) {
            when (state) {
                is ClassTeacherState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF0D9488))
                is ClassTeacherState.Error -> Text(state.message, color = Color.Red, modifier = Modifier.align(Alignment.Center))
                is ClassTeacherState.Success -> {
                    if (state.assignments.isEmpty()) {
                        EmptyTimetableView("No grade teachers assigned yet", FontAwesomeIcons.Solid.UserCheck)
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 250.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.assignments) { assignment ->
                                ClassTeacherCard(assignment)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimetableList(state: TimetableState, showGrade: Boolean, showTeacher: Boolean, onDelete: ((Int) -> Unit)? = null) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (state) {
            is TimetableState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF0D9488))
            is TimetableState.Error -> Text(state.message, color = Color.Red, modifier = Modifier.align(Alignment.Center))
            is TimetableState.Success -> {
                if (state.periods.isEmpty()) {
                    EmptyTimetableView("No periods assigned yet", FontAwesomeIcons.Solid.CalendarAlt)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(state.periods) { period ->
                            PeriodRow(period, showGrade, showTeacher, onDelete)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PeriodRow(period: PeriodEntry, showGrade: Boolean, showTeacher: Boolean, onDelete: ((Int) -> Unit)? = null) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                color = Color(0xFFF0FDFA),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(period.periodNumber.toString(), fontWeight = FontWeight.Black, color = Color(0xFF0D9488))
                }
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(period.subjectName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1E293B))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (showGrade) {
                        Text(period.gradeClass, fontSize = 12.sp, color = Color(0xFF64748B))
                    }
                    if (showTeacher) {
                        Text(period.displayTeacherName ?: "No teacher", fontSize = 12.sp, color = Color(0xFF64748B))
                    }
                }
            }

            if (onDelete != null && period.id != null) {
                IconButton(onClick = { onDelete(period.id) }, modifier = Modifier.size(32.dp)) {
                    Icon(FontAwesomeIcons.Solid.Trash, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                }
            } else {
                Badge(containerColor = Color(0xFFF1F5F9), contentColor = Color(0xFF475569)) {
                    Text("Period ${period.periodNumber}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ClassTeacherCard(assignment: ClassTeacherAssignment) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFEEF2FF)), contentAlignment = Alignment.Center) {
                    Text((assignment.displayTeacherName ?: "T").take(1).uppercase(), color = Color(0xFF6366F1), fontWeight = FontWeight.Bold)
                }
                Column {
                    Text(assignment.gradeClass ?: assignment.class_no ?: "-", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(assignment.displayTeacherName ?: "Teacher", fontSize = 12.sp, color = Color.Gray)
                }
            }
            Badge(containerColor = Color(0xFFF0FDFA), contentColor = Color(0xFF0D9488)) {
                Text("Class Teacher", fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun InfoBanner(icon: ImageVector, text: String, color: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = color.copy(alpha = 0.05f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, null, modifier = Modifier.size(16.dp), tint = color)
            Text(text, fontSize = 13.sp, color = color, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun DropdownSelector(label: String, items: List<String>, onSelect: (String) -> Unit, enabled: Boolean = true, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Surface(
            modifier = Modifier.fillMaxWidth().clickable(enabled = enabled) { expanded = true },
            color = Color.White,
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(label, fontSize = 14.sp, color = if (label.contains("Select")) Color.Gray else Color(0xFF1E293B))
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
fun EmptyTimetableView(message: String, icon: ImageVector) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, modifier = Modifier.size(48.dp), tint = Color.LightGray)
        Spacer(Modifier.height(12.dp))
        Text(message, color = Color.Gray, fontSize = 14.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignPeriodDialog(
    onDismiss: () -> Unit,
    onConfirm: (PeriodEntry) -> Unit,
    teachers: List<TeacherResponse>,
    preselectedTeacherId: Int?,
    isSaving: Boolean
) {
    var gradeClass by remember { mutableStateOf("__placeholder__") }
    var subjectName by remember { mutableStateOf("__placeholder__") }
    var periodNumber by remember { mutableStateOf(1) }
    var teacherId by remember { mutableStateOf(preselectedTeacherId?.toString() ?: "__placeholder__") }

    val grades = listOf("Nursery", "LKG", "UKG") + (1..12).map { "Grade $it" }
    val subjects = listOf("Mathematics", "English", "Science", "Social Studies", "Hindi", "Sanskrit", "Computer Science", "Physics", "Chemistry", "Biology")

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = Color.White, modifier = Modifier.fillMaxWidth(0.9f).wrapContentHeight()) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Assign Period", fontSize = 18.sp, fontWeight = FontWeight.Bold)

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Grade / Class", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    DropdownSelector(
                        label = if (gradeClass == "__placeholder__") "Select Grade..." else gradeClass,
                        items = grades,
                        onSelect = { gradeClass = it }
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Subject", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    DropdownSelector(
                        label = if (subjectName == "__placeholder__") "Select Subject..." else subjectName,
                        items = subjects,
                        onSelect = { subjectName = it }
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Period", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        DropdownSelector(
                            label = "Period $periodNumber",
                            items = (1..8).map { it.toString() },
                            onSelect = { periodNumber = it.toInt() }
                        )
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Teacher", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        DropdownSelector(
                            label = teachers.find { it.id.toString() == teacherId }?.fullName ?: "Select Teacher",
                            items = teachers.map { it.fullName },
                            onSelect = { name -> teachers.find { it.fullName == name }?.let { teacherId = it.id.toString() } },
                            enabled = preselectedTeacherId == null
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onConfirm(PeriodEntry(
                                gradeClass = gradeClass,
                                subjectName = subjectName,
                                periodNumber = periodNumber,
                                teacher_id_direct = teacherId.toIntOrNull()
                            ))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                        enabled = !isSaving && gradeClass != "__placeholder__" && subjectName != "__placeholder__" && teacherId != "__placeholder__"
                    ) {
                        if (isSaving) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                        else Text("Assign Period")
                    }
                }
            }
        }
    }
}
