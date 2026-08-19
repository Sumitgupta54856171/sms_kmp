package com.example.schoolmanagement.presentation.attendance

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.schoolmanagement.api.models.AttendanceRecord
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.*

@Composable
fun AttendanceSummaryScreen(viewModel: AttendanceSummaryViewModel) {
    val state by viewModel.state.collectAsState()
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()

    // Calculate overall working days (distinct dates that are NOT holiday)
    val workingDaysCount = remember(state) {
        if (state is AttendanceSummaryState.Success) {
            val records = (state as AttendanceSummaryState.Success).records
            val allDates = records.map { it.attendanceDate }.distinct()
            val holidayDates = records.filter { it.status == "holiday" }.map { it.attendanceDate }.distinct()
            (allDates.size - holidayDates.size).coerceAtLeast(1)
        } else 1
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Premium Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xFF1E293B), Color(0xFF334155))))
                .padding(24.dp)
        ) {
            Column {
                Badge(containerColor = Color(0xFF3B82F6).copy(alpha = 0.2f), contentColor = Color(0xFF60A5FA)) {
                    Text("ANALYTICS", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Attendance Summary", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
                Text("Gender-wise breakdown and trend analysis", fontSize = 14.sp, color = Color(0xFF94A3B8))
            }
        }

        // Overall Stats
        if (state is AttendanceSummaryState.Success) {
            val records = (state as AttendanceSummaryState.Success).records
            OverallSummaryRow(records, workingDaysCount)
        }

        // Reporting Period
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).background(Color(0xFFF0FDFA), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(FontAwesomeIcons.Solid.CalendarAlt, null, modifier = Modifier.size(18.dp), tint = Color(0xFF0D9488))
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Reporting Period", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text("$startDate to $endDate", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                }
            }
        }

        // Class-wise Details
        Text("CLASS BREAKDOWN", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF64748B), letterSpacing = 1.sp)

        when (val s = state) {
            is AttendanceSummaryState.Loading -> Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF0D9488))
            }
            is AttendanceSummaryState.Error -> Text(s.message, color = Color.Red)
            is AttendanceSummaryState.Success -> {
                if (s.records.isEmpty()) {
                    Text("No data for this range", color = Color.Gray)
                } else {
                    val grouped = s.records.groupBy { it.grade ?: "Unknown" }
                    grouped.forEach { (className, records) ->
                        ExpandableClassCard(className, records, workingDaysCount)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun OverallSummaryRow(records: List<AttendanceRecord>, workingDays: Int) {
    val studentCount = records.map { it.studentId }.distinct().size
    val totalSlots = studentCount * workingDays
    val present = records.count { it.status == "present" }
    val percentage = if (totalSlots > 0) (present * 100) / totalSlots else 0
    
    val boys = records.filter { it.gender?.lowercase() == "male" }
    val girls = records.filter { it.gender?.lowercase() == "female" }
    
    val boyCount = boys.map { it.studentId }.distinct().size
    val girlCount = girls.map { it.studentId }.distinct().size
    
    val boysPercent = if (boyCount > 0) (boys.count { it.status == "present" } * 100) / (boyCount * workingDays) else 0
    val girlsPercent = if (girlCount > 0) (girls.count { it.status == "present" } * 100) / (girlCount * workingDays) else 0

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        SummaryStatCard("Overall", "$percentage%", Color(0xFF10B981), Modifier.weight(1f))
        SummaryStatCard("Boys", "$boysPercent%", Color(0xFF3B82F6), Modifier.weight(1f))
        SummaryStatCard("Girls", "$girlsPercent%", Color(0xFFF43F5E), Modifier.weight(1f))
    }
}

@Composable
fun SummaryStatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Black, color = color)
        }
    }
}

@Composable
fun ExpandableClassCard(className: String, records: List<AttendanceRecord>, workingDays: Int) {
    var isExpanded by remember { mutableStateOf(false) }
    val studentCount = records.map { it.studentId }.distinct().size
    val totalWorkingSlots = studentCount * workingDays
    val presentCount = records.count { it.status == "present" }
    val percentage = if (totalWorkingSlots > 0) (presentCount * 100) / totalWorkingSlots else 0

    Card(
        modifier = Modifier.fillMaxWidth().clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(
                        if (isExpanded) FontAwesomeIcons.Solid.ChevronDown else FontAwesomeIcons.Solid.ChevronRight,
                        null, modifier = Modifier.size(14.dp), tint = Color.Gray
                    )
                    Text("Grade $className", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                }
                Badge(containerColor = Color(0xFFF0FDFA), contentColor = Color(0xFF0D9488)) {
                    Text("$percentage%", fontWeight = FontWeight.Bold)
                }
            }
            
            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFF1F5F9))
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    GenderStat("Boys", records.filter { it.gender?.lowercase() == "male" }, workingDays, Color(0xFF3B82F6))
                    GenderStat("Girls", records.filter { it.gender?.lowercase() == "female" }, workingDays, Color(0xFFF43F5E))
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("Detailed View", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D9488))
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { percentage / 100f },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                    color = if (percentage > 75) Color(0xFF10B981) else Color(0xFFF59E0B),
                    trackColor = Color(0xFFF1F5F9)
                )
            }
        }
    }
}

@Composable
fun GenderStat(label: String, records: List<AttendanceRecord>, workingDays: Int, color: Color) {
    val studentCount = records.map { it.studentId }.distinct().size
    val totalSlots = studentCount * workingDays
    val presentCount = records.count { it.status == "present" }
    val percentage = if (totalSlots > 0) (presentCount * 100) / totalSlots else 0
    
    Column(modifier = Modifier.width(100.dp)) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        Text("$percentage%", fontSize = 16.sp, fontWeight = FontWeight.Black, color = color)
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { percentage / 100f },
            modifier = Modifier.fillMaxWidth().height(3.dp).clip(CircleShape),
            color = color,
            trackColor = color.copy(alpha = 0.1f)
        )
        Text("$studentCount students", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
    }
}
