package com.example.schoolmanagement.presentation.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.*
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(viewModel: AttendanceViewModel) {
    val state by viewModel.state.collectAsState()
    val selectedClass by viewModel.selectedClass.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    var isEditing by remember { mutableStateOf(false) }
    var showClassDropdown by remember { mutableStateOf(false) }

    val allClasses = listOf("Nursery", "LKG", "UKG") + (1..12).map { it.toString() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(24.dp)
    ) {
        // Premium Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Daily Attendance",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0F172A),
                    letterSpacing = (-0.5).sp
                )
                Text(
                    "Manage your classroom records",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Edit Toggle Button
                Button(
                    onClick = { isEditing = !isEditing },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isEditing) Color(0xFF0F172A) else Color.White,
                        contentColor = if (isEditing) Color.White else Color(0xFF475569)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    border = if (isEditing) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Icon(
                        if (isEditing) FontAwesomeIcons.Solid.CheckCircle else FontAwesomeIcons.Solid.Edit,
                        null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(if (isEditing) "Finishing" else "Edit Mode", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                // Save All Button
                Button(
                    onClick = { viewModel.saveAttendance() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0D9488),
                        disabledContainerColor = Color(0xFF99F6E4)
                    ),
                    enabled = !isSaving && state is AttendanceState.Success,
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(FontAwesomeIcons.Solid.CloudUploadAlt, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Sync Records", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }

        // Stats Cards with Premium look
        if (state is AttendanceState.Success) {
            val students = (state as AttendanceState.Success).students
            AttendanceStatsRow(
                present = students.count { it.status == "present" },
                absent = students.count { it.status == "absent" },
                holiday = students.count { it.status == "holiday" },
                total = students.size
            )
            Spacer(modifier = Modifier.height(28.dp))
        }

        // Interactive Filters
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Class selector
            Box(modifier = Modifier.weight(1f)) {
                Surface(
                    onClick = { showClassDropdown = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    shadowElevation = 2.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(FontAwesomeIcons.Solid.GraduationCap, null, modifier = Modifier.size(16.dp), tint = Color(0xFF6366F1))
                            Spacer(Modifier.width(12.dp))
                            Text("Grade $selectedClass", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        }
                        Icon(FontAwesomeIcons.Solid.ChevronDown, null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                    }
                }

                DropdownMenu(
                    expanded = showClassDropdown,
                    onDismissRequest = { showClassDropdown = false },
                    modifier = Modifier.background(Color.White).width(200.dp)
                ) {
                    allClasses.forEach { cls ->
                        DropdownMenuItem(
                            text = { Text("Grade $cls", fontWeight = FontWeight.Medium) },
                            onClick = {
                                viewModel.setSelectedClass(cls)
                                showClassDropdown = false
                            }
                        )
                    }
                }
            }
            
            // Date selector
            var showDatePicker by remember { mutableStateOf(false) }
            val datePickerState = rememberDatePickerState()

            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val date = Instant.fromEpochMilliseconds(millis)
                                    .toLocalDateTime(TimeZone.UTC).date
                                viewModel.setSelectedDate(date.toString())
                            }
                            showDatePicker = false
                        }) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            Surface(
                onClick = { showDatePicker = true },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(FontAwesomeIcons.Solid.CalendarCheck, null, modifier = Modifier.size(16.dp), tint = Color(0xFFEC4899))
                        Spacer(Modifier.width(12.dp))
                        Text(selectedDate, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    }
                    Icon(FontAwesomeIcons.Solid.ChevronDown, null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                }
            }
        }

        // List Header
        if (state is AttendanceState.Success) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("STUDENT NAME", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), letterSpacing = 1.sp)
                Text("STATUS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), letterSpacing = 1.sp, modifier = Modifier.padding(end = 50.dp))
            }
        }

        // List
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (val s = state) {
                is AttendanceState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF0D9488))
                is AttendanceState.Error -> Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(FontAwesomeIcons.Solid.ExclamationTriangle, null, tint = Color.Red, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(12.dp))
                    Text(s.message, color = Color.Red, fontWeight = FontWeight.Bold)
                }
                is AttendanceState.Success -> {
                    if (s.students.isEmpty()) {
                        Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(FontAwesomeIcons.Solid.UserFriends, null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(64.dp))
                            Spacer(Modifier.height(16.dp))
                            Text("No students in Grade $selectedClass", color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(s.students) { row ->
                                AttendanceRow(
                                    row = row,
                                    isEditing = isEditing,
                                    onStatusChange = { viewModel.updateStatus(row.id, it) },
                                    onSync = { viewModel.updateIndividualAttendance(row.id, row.status) }
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
fun AttendanceStatsRow(present: Int, absent: Int, holiday: Int, total: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
        StatMiniCard("PRESENT", present.toString(), Color(0xFF10B981), FontAwesomeIcons.Solid.Check, Color(0xFFD1FAE5), Modifier.weight(1f))
        StatMiniCard("ABSENT", absent.toString(), Color(0xFFEF4444), FontAwesomeIcons.Solid.Times, Color(0xFFFEE2E2), Modifier.weight(1f))
        StatMiniCard("HOLIDAY", holiday.toString(), Color(0xFF8B5CF6), FontAwesomeIcons.Solid.UmbrellaBeach, Color(0xFFEDE9FE), Modifier.weight(1f))
        StatMiniCard("TOTAL", total.toString(), Color(0xFF3B82F6), FontAwesomeIcons.Solid.Users, Color(0xFFDBEAFE), Modifier.weight(1f))
    }
}

@Composable
fun StatMiniCard(label: String, value: String, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector, bgColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, modifier = Modifier.size(14.dp), tint = color)
                }
                Text(label, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF94A3B8))
            }
            Spacer(Modifier.height(12.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E293B))
        }
    }
}

@Composable
fun AttendanceRow(row: AttendanceStudentRow, isEditing: Boolean, onStatusChange: (String) -> Unit, onSync: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Premium Initials Avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(Color(0xFF6366F1), Color(0xFF4F46E5))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    row.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.joinToString("").take(2).uppercase(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(row.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F172A))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Roll: ${row.rollNumber}", fontSize = 12.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                    Spacer(Modifier.width(8.dp))
                    Box(Modifier.size(3.dp).clip(CircleShape).background(Color(0xFFCBD5E1)))
                    Spacer(Modifier.width(8.dp))
                    Text("Scholar: ${row.scholarNo}", fontSize = 12.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                }
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatusButton("P", "present", row.status == "present", Color(0xFF10B981), onStatusChange)
                StatusButton("A", "absent", row.status == "absent", Color(0xFFEF4444), onStatusChange)
                StatusButton("H", "holiday", row.status == "holiday", Color(0xFF8B5CF6), onStatusChange)
            }

            if (isEditing) {
                IconButton(
                    onClick = onSync,
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFF1F5F9))
                ) {
                    Icon(FontAwesomeIcons.Solid.SyncAlt, null, modifier = Modifier.size(14.dp), tint = Color(0xFF0D9488))
                }
            }
        }
    }
}

@Composable
fun StatusButton(label: String, status: String, isSelected: Boolean, color: Color, onClick: (String) -> Unit) {
    Surface(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick(status) },
        color = if (isSelected) color else Color(0xFFF8FAFC),
        contentColor = if (isSelected) Color.White else Color(0xFF64748B),
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, fontWeight = FontWeight.Black, fontSize = 13.sp)
        }
    }
}
