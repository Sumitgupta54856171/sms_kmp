package com.example.schoolmanagement.presentation.attendance

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.schoolmanagement.presentation.components.ExpressiveDropdown
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.*
import kotlinx.datetime.*

// Design tokens for consistent spacing
private object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp
}

// Design tokens for colors
private object AppColors {
    val Background = Color(0xFFF8FAFC)
    val Surface = Color.White
    val PrimaryText = Color(0xFF0F172A)
    val SecondaryText = Color(0xFF64748B)
    val TertiaryText = Color(0xFF94A3B8)
    val Border = Color(0xFFE2E8F0)
    val Accent = Color(0xFF0D9488)
    val AccentLight = Color(0xFF99F6E4)
    val Present = Color(0xFF10B981)
    val PresentBg = Color(0xFFD1FAE5)
    val Absent = Color(0xFFEF4444)
    val AbsentBg = Color(0xFFFEE2E2)
    val Holiday = Color(0xFF8B5CF6)
    val HolidayBg = Color(0xFFEDE9FE)
    val Total = Color(0xFF3B82F6)
    val TotalBg = Color(0xFFDBEAFE)
    val Indigo = Color(0xFF6366F1)
    val IndigoDark = Color(0xFF4F46E5)
    val Pink = Color(0xFFEC4899)
}

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

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(AppColors.Background)) {
        val screenWidth = maxWidth
        val isCompact = screenWidth < 600.dp
        val horizontalPadding = if (isCompact) Spacing.lg else Spacing.xxl

        Scaffold(
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = horizontalPadding)
            ) {
                Spacer(Modifier.height(if (isCompact) Spacing.md else Spacing.xl))

                // Premium Header - Responsive
                AttendanceHeader(
                    isEditing = isEditing,
                    isSaving = isSaving,
                    canSave = state is AttendanceState.Success,
                    isCompact = isCompact,
                    onToggleEdit = { isEditing = !isEditing },
                    onSave = { viewModel.saveAttendance() }
                )

                Spacer(Modifier.height(if (isCompact) Spacing.xl else Spacing.xxxl))

                // Stats Cards - Responsive
                if (state is AttendanceState.Success) {
                    val students = (state as AttendanceState.Success).students
                    AttendanceStatsRow(
                        present = students.count { it.status == "present" },
                        absent = students.count { it.status == "absent" },
                        holiday = students.count { it.status == "holiday" },
                        total = students.size,
                        isCompact = isCompact
                    )
                    Spacer(Modifier.height(if (isCompact) Spacing.lg else Spacing.xxl))
                }

                // Interactive Filters - Responsive
                if (isCompact) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        ClassSelector(
                            selectedClass = selectedClass,
                            expanded = showClassDropdown,
                            onExpandChange = { showClassDropdown = it },
                            onSelect = {
                                viewModel.setSelectedClass(it)
                                showClassDropdown = false
                            },
                            allClasses = allClasses
                        )
                        DateSelector(
                            selectedDate = selectedDate,
                            onDateSelected = { viewModel.setSelectedDate(it) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.lg)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            ClassSelector(
                                selectedClass = selectedClass,
                                expanded = showClassDropdown,
                                onExpandChange = { showClassDropdown = it },
                                onSelect = {
                                    viewModel.setSelectedClass(it)
                                    showClassDropdown = false
                                },
                                allClasses = allClasses
                            )
                        }
                        DateSelector(
                            selectedDate = selectedDate,
                            onDateSelected = { viewModel.setSelectedDate(it) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(Modifier.height(if (isCompact) Spacing.lg else Spacing.xxl))

                // List Header
                if (state is AttendanceState.Success) {
                    ListHeader(isCompact)
                    Spacer(Modifier.height(Spacing.sm))
                }

                // List
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    when (val s = state) {
                        is AttendanceState.Loading -> LoadingState()
                        is AttendanceState.Error -> ErrorState(s.message)
                        is AttendanceState.Success -> {
                            if (s.students.isEmpty()) {
                                EmptyState(selectedClass)
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                                    contentPadding = PaddingValues(bottom = Spacing.xxl)
                                ) {
                                    items(s.students) { row ->
                                        AttendanceRow(
                                            row = row,
                                            isEditing = isEditing,
                                            isCompact = isCompact,
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
    }
}

@Composable
private fun AttendanceHeader(
    isEditing: Boolean,
    isSaving: Boolean,
    canSave: Boolean,
    isCompact: Boolean,
    onToggleEdit: () -> Unit,
    onSave: () -> Unit
) {
    if (isCompact) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                "Daily Attendance",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = AppColors.PrimaryText,
                letterSpacing = (-0.5).sp
            )
            Text(
                "Manage classroom records",
                fontSize = 13.sp,
                color = AppColors.SecondaryText,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(Spacing.lg))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                // Edit Toggle Button
                FilledTonalButton(
                    onClick = onToggleEdit,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isEditing) AppColors.PrimaryText else AppColors.Surface,
                        contentColor = if (isEditing) AppColors.Surface else AppColors.SecondaryText
                    ),
                    shape = MaterialTheme.shapes.small,
                    contentPadding = PaddingValues(vertical = Spacing.sm),
                ) {
                    Icon(
                        if (isEditing) FontAwesomeIcons.Solid.CheckCircle else FontAwesomeIcons.Solid.Edit,
                        null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(Spacing.xs))
                    Text(if (isEditing) "Done" else "Edit", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                // Save All Button
                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1.2f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Accent,
                        disabledContainerColor = AppColors.AccentLight,
                    ),
                    enabled = !isSaving && canSave,
                    shape = MaterialTheme.shapes.small,
                    contentPadding = PaddingValues(vertical = Spacing.sm)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(FontAwesomeIcons.Solid.CloudUploadAlt, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(Spacing.sm))
                        Text("Sync", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                    }
                }
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Daily Attendance",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black,
                    color = AppColors.PrimaryText,
                    letterSpacing = (-0.8).sp,
                    lineHeight = 36.sp
                )
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    "Manage your classroom records",
                    fontSize = 14.sp,
                    color = AppColors.SecondaryText,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.1.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                // Edit Toggle Button
                FilledTonalButton(
                    onClick = onToggleEdit,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isEditing) AppColors.PrimaryText else AppColors.Surface,
                        contentColor = if (isEditing) AppColors.Surface else AppColors.SecondaryText
                    ),
                    shape = MaterialTheme.shapes.small,
                    contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.md),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 2.dp)
                ) {
                    Icon(
                        if (isEditing) FontAwesomeIcons.Solid.CheckCircle else FontAwesomeIcons.Solid.Edit,
                        null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(Spacing.sm))
                    Text(
                        if (isEditing) "Finishing" else "Edit Mode",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Save All Button
                Button(
                    onClick = onSave,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Accent,
                        contentColor = Color.White,
                        disabledContainerColor = AppColors.AccentLight,
                        disabledContentColor = Color.White.copy(alpha = 0.7f)
                    ),
                    enabled = !isSaving && canSave,
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 6.dp),
                    shape = MaterialTheme.shapes.small,
                    contentPadding = PaddingValues(horizontal = Spacing.xl, vertical = Spacing.md)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Icon(FontAwesomeIcons.Solid.CloudUploadAlt, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(Spacing.md))
                        Text("Sync Records", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ClassSelector(
    selectedClass: String,
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    onSelect: (String) -> Unit,
    allClasses: List<String>
) {
    ExpressiveDropdown(
        label = "Grade $selectedClass",
        items = allClasses.map { "Grade $it" },
        onSelect = { onSelect(it.replace("Grade ", "")) },
        icon = FontAwesomeIcons.Solid.GraduationCap
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateSelector(
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
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
                        onDateSelected(date.toString())
                    }
                    showDatePicker = false
                }) {
                    Text("OK", fontWeight = FontWeight.Bold, color = AppColors.Accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", fontWeight = FontWeight.Medium)
                }
            },
            shape = RoundedCornerShape(20.dp)
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Surface(
        onClick = { showDatePicker = true },
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = AppColors.Surface,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, AppColors.Border)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md + 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AppColors.Pink.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        FontAwesomeIcons.Solid.CalendarCheck,
                        null,
                        modifier = Modifier.size(16.dp),
                        tint = AppColors.Pink
                    )
                }
                Spacer(Modifier.width(Spacing.md))
                Text(
                    selectedDate,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PrimaryText
                )
            }
            Icon(
                FontAwesomeIcons.Solid.ChevronDown,
                null,
                modifier = Modifier.size(12.dp),
                tint = AppColors.TertiaryText
            )
        }
    }
}

@Composable
private fun ListHeader(isCompact: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.md, vertical = Spacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            "STUDENT NAME",
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = AppColors.TertiaryText,
            letterSpacing = 1.2.sp
        )
        Text(
            "STATUS",
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = AppColors.TertiaryText,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(end = if (isCompact) 24.dp else 60.dp)
        )
    }
}

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = AppColors.Accent,
                strokeWidth = 3.dp,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.height(Spacing.lg))
            Text(
                "Loading students...",
                color = AppColors.SecondaryText,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun ErrorState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(AppColors.AbsentBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    FontAwesomeIcons.Solid.ExclamationTriangle,
                    null,
                    tint = AppColors.Absent,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(Modifier.height(Spacing.lg))
            Text(
                message,
                color = AppColors.Absent,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
private fun EmptyState(selectedClass: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(AppColors.Border),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    FontAwesomeIcons.Solid.UserFriends,
                    null,
                    tint = AppColors.TertiaryText,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(Modifier.height(Spacing.lg))
            Text(
                "No students in Grade $selectedClass",
                color = AppColors.SecondaryText,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
            Spacer(Modifier.height(Spacing.xs))
            Text(
                "Try selecting a different grade",
                color = AppColors.TertiaryText,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun AttendanceStatsRow(present: Int, absent: Int, holiday: Int, total: Int, isCompact: Boolean) {
    if (isCompact) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), modifier = Modifier.fillMaxWidth()) {
                StatMiniCard("PRESENT", present.toString(), AppColors.Present, FontAwesomeIcons.Solid.Check, AppColors.PresentBg, Modifier.weight(1f), isCompact)
                StatMiniCard("ABSENT", absent.toString(), AppColors.Absent, FontAwesomeIcons.Solid.Times, AppColors.AbsentBg, Modifier.weight(1f), isCompact)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), modifier = Modifier.fillMaxWidth()) {
                StatMiniCard("HOLIDAY", holiday.toString(), AppColors.Holiday, FontAwesomeIcons.Solid.UmbrellaBeach, AppColors.HolidayBg, Modifier.weight(1f), isCompact)
                StatMiniCard("TOTAL", total.toString(), AppColors.Total, FontAwesomeIcons.Solid.Users, AppColors.TotalBg, Modifier.weight(1f), isCompact)
            }
        }
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md), modifier = Modifier.fillMaxWidth()) {
            StatMiniCard("PRESENT", present.toString(), AppColors.Present, FontAwesomeIcons.Solid.Check, AppColors.PresentBg, Modifier.weight(1f), isCompact)
            StatMiniCard("ABSENT", absent.toString(), AppColors.Absent, FontAwesomeIcons.Solid.Times, AppColors.AbsentBg, Modifier.weight(1f), isCompact)
            StatMiniCard("HOLIDAY", holiday.toString(), AppColors.Holiday, FontAwesomeIcons.Solid.UmbrellaBeach, AppColors.HolidayBg, Modifier.weight(1f), isCompact)
            StatMiniCard("TOTAL", total.toString(), AppColors.Total, FontAwesomeIcons.Solid.Users, AppColors.TotalBg, Modifier.weight(1f), isCompact)
        }
    }
}

@Composable
fun StatMiniCard(label: String, value: String, color: Color, icon: ImageVector, bgColor: Color, modifier: Modifier = Modifier, isCompact: Boolean = false) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = if (isCompact) MaterialTheme.shapes.small else MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, AppColors.Border)
    ) {
        Column(modifier = Modifier.padding(if (isCompact) Spacing.md else Spacing.lg)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(if (isCompact) 28.dp else 32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, modifier = Modifier.size(if (isCompact) 14.dp else 16.dp), tint = color)
                }
                Text(
                    label,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AppColors.TertiaryText,
                    letterSpacing = 0.8.sp
                )
            }
            Spacer(Modifier.height(if (isCompact) Spacing.sm else Spacing.md))
            Text(
                value,
                fontSize = if (isCompact) 22.sp else 26.sp,
                fontWeight = FontWeight.Black,
                color = AppColors.PrimaryText,
                letterSpacing = (-0.5).sp
            )
        }
    }
}

@Composable
fun AttendanceRow(row: AttendanceStudentRow, isEditing: Boolean, isCompact: Boolean, onStatusChange: (String) -> Unit, onSync: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = if (isCompact) MaterialTheme.shapes.small else MaterialTheme.shapes.medium,
        color = AppColors.Surface,
        shadowElevation = 0.5.dp,
        border = BorderStroke(1.dp, AppColors.Border)
    ) {
        Row(
            modifier = Modifier.padding(if (isCompact) Spacing.sm + 2.dp else Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (isCompact) Spacing.sm else Spacing.lg)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(if (isCompact) 40.dp else 48.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(AppColors.Indigo, AppColors.IndigoDark)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    row.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.joinToString("").take(2).uppercase(),
                    fontSize = if (isCompact) 13.sp else 15.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    row.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isCompact) 14.sp else 16.sp,
                    color = AppColors.PrimaryText,
                    maxLines = 1
                )
                if (!isCompact) {
                    Spacer(Modifier.height(Spacing.xs))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Roll: ${row.rollNumber}",
                            fontSize = 11.sp,
                            color = AppColors.SecondaryText,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.width(Spacing.sm))
                        Box(Modifier.size(2.dp).clip(CircleShape).background(AppColors.Border))
                        Spacer(Modifier.width(Spacing.sm))
                        Text(
                            "Scholar: ${row.scholarNo}",
                            fontSize = 11.sp,
                            color = AppColors.SecondaryText,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Text(
                        "#${row.rollNumber}",
                        fontSize = 11.sp,
                        color = AppColors.SecondaryText,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(if (isCompact) 4.dp else Spacing.sm)) {
                StatusButton("P", "present", row.status == "present", AppColors.Present, isCompact, onStatusChange)
                StatusButton("A", "absent", row.status == "absent", AppColors.Absent, isCompact, onStatusChange)
                StatusButton("H", "holiday", row.status == "holiday", AppColors.Holiday, isCompact, onStatusChange)
            }

            if (isEditing) {
                IconButton(
                    onClick = onSync,
                    modifier = Modifier
                        .size(if (isCompact) 32.dp else 40.dp)
                        .clip(CircleShape)
                        .background(AppColors.Accent.copy(alpha = 0.1f))
                ) {
                    Icon(
                        FontAwesomeIcons.Solid.SyncAlt,
                        null,
                        modifier = Modifier.size(if (isCompact) 13.dp else 15.dp),
                        tint = AppColors.Accent
                    )
                }
            }
        }
    }
}

@Composable
fun StatusButton(label: String, status: String, isSelected: Boolean, color: Color, isCompact: Boolean, onClick: (String) -> Unit) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) color else AppColors.Background,
        animationSpec = tween(200),
        label = "statusBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else AppColors.SecondaryText,
        animationSpec = tween(200),
        label = "statusContent"
    )

    Surface(
        modifier = Modifier
            .size(if (isCompact) 34.dp else 40.dp)
            .clip(MaterialTheme.shapes.small)
            .clickable { onClick(status) },
        color = backgroundColor,
        contentColor = contentColor,
        border = if (isSelected) null else BorderStroke(1.dp, AppColors.Border),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                label,
                fontWeight = FontWeight.Black,
                fontSize = if (isCompact) 12.sp else 14.sp
            )
        }
    }
}
