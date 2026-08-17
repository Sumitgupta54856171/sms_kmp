package com.example.schoolmanagement.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.schoolmanagement.presentation.dashboard.components.*

@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val state by viewModel.state.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        when (val currentState = state) {
            is DashboardState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF0D9488))
            }
            is DashboardState.Success -> {
                DashboardContent(currentState.data)
            }
            is DashboardState.Error -> {
                Text(
                    text = currentState.message,
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun DashboardContent(data: com.example.schoolmanagement.api.models.DashboardData) {
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
                .padding(if (isMobile) 16.dp else 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(if (isMobile) 16.dp else 24.dp)
        ) {
            // Header
            Column {
                Text(
                    text = "Dashboard",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = "Live school metrics and activity",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B)
                )
            }

            // Stats Grid
            val stats = data.stats
            val statItems = listOf(
                StatItem("Total Students", stats.totalStudents.toString(), Color(0xFF0D9488), "${stats.activeStudents} active students"),
                StatItem("Today's Presence", "${stats.attendancePercentage}%", Color(0xFF10B981), "Present today"),
                StatItem("Total Collection", "₹${(stats.totalFees / 100000).toInt()}L", Color(0xFF6366F1), "Current session"),
                StatItem("Due Fees", "₹${(stats.dueFees / 100000).toInt()}L", Color(0xFFF43F5E), "Outstanding")
            )
            
            ChunkedGrid(statItems, columns)

            // Analytics Section (Charts)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                val isWide = screenWidth > 900.dp
                if (isWide) {
                    ChartCard("Enrollment by Class", modifier = Modifier.weight(1.5f)) {
                        BarChart(data.enrollmentByClass)
                    }
                    ChartCard("Gender Distribution", modifier = Modifier.weight(1f)) {
                        DonutChart(stats.maleStudents, stats.femaleStudents)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.align(Alignment.CenterHorizontally)) {
                            LegendItem("Boys", Color(0xFF3B82F6))
                            LegendItem("Girls", Color(0xFFF43F5E))
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        ChartCard("Enrollment by Class") { BarChart(data.enrollmentByClass) }
                        ChartCard("Gender Distribution") { 
                            DonutChart(stats.maleStudents, stats.femaleStudents)
                        }
                    }
                }
            }

            // Trend Section
            ChartCard("Attendance Trend") {
                AreaChart(data.attendanceTrend)
            }

            // Activity sections (Notices/Events)
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
            
            // Quick Overview (Section 4 from web)
            QuickOverviewGrid(stats, columns)
        }
    }
}

@Composable
fun ChartCard(title: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title.uppercase(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), modifier = Modifier.padding(bottom = 20.dp))
            content()
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.size(10.dp).background(color, RoundedCornerShape(2.dp)))
        Text(label, fontSize = 12.sp, color = Color(0xFF64748B))
    }
}

data class StatItem(val title: String, val value: String, val color: Color, val subtitle: String)

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
        Pair("Active Students", stats.activeStudents.toString()),
        Pair("Total Faculty", stats.totalTeachers.toString()),
        Pair("Enrollments", stats.totalEnrollments.toString()),
        Pair("Male Ratio", "${if(stats.totalStudents > 0) (stats.maleStudents * 100 / stats.totalStudents) else 0}%"),
        Pair("Female Ratio", "${if(stats.totalStudents > 0) (stats.femaleStudents * 100 / stats.totalStudents) else 0}%"),
        Pair("Session", "2026-27")
    )
    
    val gridColumns = if(columns > 2) 6 else if(columns == 2) 3 else 2
    val rows = items.chunked(gridColumns)
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("QUICK OVERVIEW", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
        rows.forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowItems.forEach { (label, value) ->
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B))
                            Text(label, fontSize = 10.sp, color = Color(0xFF94A3B8), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("RECENT NOTICES", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
            if (notices.isEmpty()) Text("No recent notices", fontSize = 14.sp, color = Color.Gray)
            notices.forEach { notice ->
                Column {
                    Text(notice.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    Text(notice.data, fontSize = 12.sp, color = Color(0xFF94A3B8))
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("UPCOMING EVENTS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
            if (events.isEmpty()) Text("No upcoming events", fontSize = 14.sp, color = Color.Gray)
            events.forEach { event ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.size(8.dp).background(parseColor(event.color ?: "#0D9488"), RoundedCornerShape(4.dp)))
                    Column {
                        Text(event.eventname, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        Text(event.eventdate, fontSize = 12.sp, color = Color(0xFF94A3B8))
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
