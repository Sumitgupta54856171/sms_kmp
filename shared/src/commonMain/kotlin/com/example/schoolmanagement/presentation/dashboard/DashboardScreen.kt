package com.example.schoolmanagement.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.schoolmanagement.api.models.DashboardTimeRange
import com.example.schoolmanagement.presentation.dashboard.components.*
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.*

@Composable
fun DashboardScreen(viewModel: DashboardViewModel, onNavigate: (String) -> Unit) {
    val state by viewModel.state.collectAsState()
    val selectedRange by viewModel.selectedRange.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFF1F5F9), Color(0xFFFFFFFF))
                )
            )
    ) {
        when (val currentState = state) {
            is DashboardState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF0D9488), strokeWidth = 5.dp)
                }
            }
            is DashboardState.Success -> {
                DashboardContent(
                    data = currentState.data,
                    selectedRange = selectedRange,
                    onRangeSelected = { viewModel.setTimeRange(it) },
                    onRetry = { viewModel.loadDashboardData() },
                    onNavigate = onNavigate
                )
            }
            is DashboardState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Something went wrong",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentState.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadDashboardData() }) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    data: com.example.schoolmanagement.api.models.DashboardData,
    selectedRange: com.example.schoolmanagement.api.models.DashboardTimeRange,
    onRangeSelected: (com.example.schoolmanagement.api.models.DashboardTimeRange) -> Unit,
    onRetry: () -> Unit,
    onNavigate: (String) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val columns = when {
            screenWidth > 1200.dp -> 4
            screenWidth > 800.dp -> 2
            else -> 1
        }
        val isMobile = screenWidth < 600.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isMobile) 16.dp else 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(if (isMobile) 24.dp else 32.dp)
        ) {
            // Modern Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Dashboard",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = "Live analytics and metrics Overview",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF64748B)
                    )
                }
                
                if (!isMobile) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        SingleChoiceSegmentedButtonRow {
                            DashboardTimeRange.values().forEachIndexed { index, range ->
                                SegmentedButton(
                                    selected = selectedRange == range,
                                    onClick = { onRangeSelected(range) },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = DashboardTimeRange.values().size),
                                    label = { Text(range.name.lowercase().capitalize()) }
                                )
                            }
                        }

                        IconButton(onClick = onRetry) {
                            Icon(FontAwesomeIcons.Solid.SyncAlt, contentDescription = "Refresh", tint = Color(0xFF0D9488))
                        }
                    }
                }
            }

            if (isMobile) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    DashboardTimeRange.values().forEachIndexed { index, range ->
                        SegmentedButton(
                            selected = selectedRange == range,
                            onClick = { onRangeSelected(range) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = DashboardTimeRange.values().size),
                            label = { Text(range.name.lowercase().capitalize()) }
                        )
                    }
                }
            }

            // High-Impact Stats Grid
            val stats = data.stats
            val statItems = listOf(
                StatItem("Total Students", stats.totalStudents.toString(), Color(0xFF0D9488), FontAwesomeIcons.Solid.UserGraduate, "${stats.activeStudents} Active"),
                StatItem("Faculty Members", stats.totalTeachers.toString(), Color(0xFF3B82F6), FontAwesomeIcons.Solid.ChalkboardTeacher, "Verified Staff"),
                StatItem("Presence", "${stats.attendancePercentage}%", Color(0xFF10B981), FontAwesomeIcons.Solid.UserCheck, "Daily Attendance"),
                StatItem("Collection", "₹${(stats.todayCollection / 1000).toInt()}K", Color(0xFF6366F1), FontAwesomeIcons.Solid.Wallet, "Today's Income")
            )
            
            ChunkedGrid(statItems, columns)

            // Dynamic Analytics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                val isWide = screenWidth > 1000.dp
                if (isWide) {
                    ChartCard("Enrollment Distribution", modifier = Modifier.weight(1.8f)) {
                        BarChart(data.enrollmentByClass)
                    }
                    ChartCard("Gender Breakdown", modifier = Modifier.weight(1.2f)) {
                        DonutChart(stats.maleStudents, stats.femaleStudents)
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp), modifier = Modifier.align(Alignment.CenterHorizontally)) {
                            LegendItem("Boys", Color(0xFF3B82F6), stats.maleStudents.toString())
                            LegendItem("Girls", Color(0xFFF43F5E), stats.femaleStudents.toString())
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        ChartCard("Enrollment Distribution") { BarChart(data.enrollmentByClass) }
                        ChartCard("Gender Breakdown") { 
                            DonutChart(stats.maleStudents, stats.femaleStudents)
                        }
                    }
                }
            }

            // Attendance Trend Full Width
            ChartCard("Attendance Trend") {
                AreaChart(data.attendanceTrend)
            }

            // Academic Options Grid
            Text("ACADEMIC OPTIONS", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF64748B), letterSpacing = 1.sp)
            AcademicOptionsGrid(onNavigate = onNavigate)

            // Academic Options Grid
            Text("ACADEMIC OPTIONS", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF64748B), letterSpacing = 1.sp)
            AcademicOptionsGrid(onNavigate = { /* Navigation logic needed here or passed down */ })

            // Notices and Events Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                val isWide = screenWidth > 900.dp
                if (isWide) {
                    RecentNotices(data.recentNotices, modifier = Modifier.weight(1f))
                    UpcomingEvents(data.upcomingEvents, modifier = Modifier.weight(1f))
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        RecentNotices(data.recentNotices)
                        UpcomingEvents(data.upcomingEvents)
                    }
                }
            }
            
            // Modern Bottom Overview
            QuickOverviewGrid(stats, columns)
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ChartCard(title: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF94A3B8),
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            content()
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.size(12.dp).background(color, RoundedCornerShape(4.dp)))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
            Text(value, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF1E293B), fontWeight = FontWeight.Black)
        }
    }
}

data class StatItem(val title: String, val value: String, val color: Color, val icon: androidx.compose.ui.graphics.vector.ImageVector, val subtitle: String)

@Composable
fun ChunkedGrid(items: List<StatItem>, columns: Int) {
    val rows = items.chunked(columns)
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        rows.forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                rowItems.forEach { item ->
                    StatCard(
                        title = item.title,
                        value = item.value,
                        color = item.color,
                        icon = item.icon,
                        subtitle = item.subtitle,
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(columns - rowItems.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
fun QuickOverviewGrid(stats: com.example.schoolmanagement.api.models.DashboardStats, columns: Int) {
    val items = listOf(
        Triple("Active Students", stats.activeStudents.toString(), Color(0xFF0D9488)),
        Triple("Total Faculty", stats.totalTeachers.toString(), Color(0xFF3B82F6)),
        Triple("Enrollments", stats.totalEnrollments.toString(), Color(0xFF8B5CF6)),
        Triple("Male Students", stats.maleStudents.toString(), Color(0xFF3B82F6)),
        Triple("Female Students", stats.femaleStudents.toString(), Color(0xFFF43F5E)),
        Triple("Retention", "98%", Color(0xFF10B981))
    )
    
    val gridColumns = if(columns > 2) 6 else if(columns == 2) 3 else 2
    val rows = items.chunked(gridColumns)
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            "QUICK SNAPSHOT",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF94A3B8),
            letterSpacing = 1.sp
        )
        rows.forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                rowItems.forEach { (label, value, color) ->
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = Color.White,
                        shape = RoundedCornerShape(20.dp),
                        shadowElevation = 0.5.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = color)
                            Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                repeat(gridColumns - rowItems.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
fun RecentNotices(notices: List<com.example.schoolmanagement.api.models.Notice>, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text("RECENT NOTICES", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold, color = Color(0xFF94A3B8), letterSpacing = 1.sp)
            if (notices.isEmpty()) {
                Text("No announcements today", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
            notices.forEach { notice ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(modifier = Modifier.size(4.dp, 32.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFF59E0B)))
                    Column {
                        Text(notice.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        Text(notice.data, style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                    }
                }
                if (notice != notices.last()) HorizontalDivider(color = Color(0xFFF1F5F9))
            }
        }
    }
}

@Composable
fun UpcomingEvents(events: List<com.example.schoolmanagement.api.models.SchoolEvent>, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text("UPCOMING EVENTS", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold, color = Color(0xFF94A3B8), letterSpacing = 1.sp)
            if (events.isEmpty()) {
                Text("No upcoming events", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
            events.forEach { event ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = parseColor(event.color ?: "#0D9488").copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = event.eventdate.takeLast(2),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = parseColor(event.color ?: "#0D9488")
                            )
                        }
                    }
                    Column {
                        Text(event.eventname, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        Text(event.eventdate, style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                    }
                }
            }
        }
    }
}

private fun parseColor(hex: String): Color {
    val cleanHex = hex.removePrefix("#")
    return try {
        if (cleanHex.length == 6) {
            Color(cleanHex.toLong(16) or 0xFF000000L)
        } else {
            Color(cleanHex.toLong(16))
        }
    } catch (e: Exception) {
        Color(0xFF0D9488)
    }
}

@Composable
fun AcademicOptionsGrid(onNavigate: (String) -> Unit) {
    val options = listOf(
        Triple("Attendance", "attendance", FontAwesomeIcons.Solid.UserCheck),
        Triple("Summary", "attendance/summary", FontAwesomeIcons.Solid.ChartBar),
        Triple("Teachers", "teachers", FontAwesomeIcons.Solid.ChalkboardTeacher),
        Triple("Timetable", "timetable", FontAwesomeIcons.Solid.CalendarAlt),
        Triple("Classes", "class", FontAwesomeIcons.Solid.School),
        Triple("Exams", "timetable/exams", FontAwesomeIcons.Solid.FileAlt),
        Triple("Grades", "grades", FontAwesomeIcons.Solid.ChartLine),
        Triple("Invoices", "fees/invoice-history", FontAwesomeIcons.Solid.History),
        Triple("TC", "tc", FontAwesomeIcons.Solid.FileExport),
        Triple("Enrollment", "enrollment", FontAwesomeIcons.Solid.ArrowUp),
        Triple("Logins", "login-generate", FontAwesomeIcons.Solid.Key)
    )

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        options.chunked(3).forEach { rowOptions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowOptions.forEach { (title, url, icon) ->
                    OptionCard(title, icon, onClick = { onNavigate(url) }, modifier = Modifier.weight(1f))
                }
                // Fill empty slots in the last row to maintain grid alignment
                repeat(3 - rowOptions.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun OptionCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9)),
        shadowElevation = 0.5.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(Color(0xFFF0FDFA), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, modifier = Modifier.size(20.dp), tint = Color(0xFF0D9488))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

private fun String.capitalize() = this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
