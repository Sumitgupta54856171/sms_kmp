package com.example.schoolmanagement.presentation.academics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.schoolmanagement.api.models.PeriodEntry
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.*

@Composable
fun TimetableScreen(viewModel: TimetableViewModel) {
    val state by viewModel.state.collectAsState()
    val selectedGrade by viewModel.selectedGrade.collectAsState()

    var activeTab by remember { mutableStateOf(0) }
    val tabs = listOf("Grade View", "Teacher View", "My Timetable", "Class Teacher")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFE0F2F1)),
                contentAlignment = Alignment.Center
            ) {
                Icon(FontAwesomeIcons.Solid.CalendarAlt, null, modifier = Modifier.size(24.dp), tint = Color(0xFF0D9488))
            }
            Column {
                Text("Timetable Management", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E293B))
                Text("Organize schedules across faculty and grades", fontSize = 13.sp, color = Color(0xFF64748B))
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        // Premium Tabs
        ScrollableTabRow(
            selectedTabIndex = activeTab,
            containerColor = Color.Transparent,
            contentColor = Color(0xFF0D9488),
            edgePadding = 0.dp,
            divider = {},
            indicator = { tabPositions ->
                if (activeTab < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                        color = Color(0xFF0D9488)
                    )
                }
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = activeTab == index,
                    onClick = { activeTab = index },
                    text = { 
                        Text(
                            title, 
                            fontSize = 13.sp,
                            fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Medium,
                            color = if (activeTab == index) Color(0xFF0D9488) else Color(0xFF64748B)
                        ) 
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        when (activeTab) {
            0 -> GradeTimetableContent(state, selectedGrade, onGradeChange = { viewModel.setSelectedGrade(it) })
            else -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Section coming soon", color = Color.Gray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            }
        }
    }
}

@Composable
fun GradeTimetableContent(state: TimetableState, selectedGrade: String, onGradeChange: (String) -> Unit) {
    val grades = listOf("Nursery", "LKG", "UKG") + (1..12).map { it.toString() }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Select Grade:", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            // Simplified Dropdown for now
            ScrollableTabRow(
                selectedTabIndex = grades.indexOf(selectedGrade).coerceAtLeast(0),
                edgePadding = 0.dp,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                grades.forEach { grade ->
                    Tab(
                        selected = selectedGrade == grade,
                        onClick = { onGradeChange(grade) },
                        text = { Text(grade) }
                    )
                }
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (state) {
                is TimetableState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF0D9488))
                is TimetableState.Error -> Text(state.message, color = Color.Red, modifier = Modifier.align(Alignment.Center))
                is TimetableState.Success -> {
                    if (state.periods.isEmpty()) {
                        Text("No periods assigned for this grade", modifier = Modifier.align(Alignment.Center), color = Color.Gray)
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(state.periods) { period ->
                                PeriodCard(period)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PeriodCard(period: PeriodEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF0FDFA)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    period.periodNumber.toString(),
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0D9488)
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(period.subjectName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(period.teacher_name ?: "No teacher assigned", fontSize = 12.sp, color = Color.Gray)
            }
            
            Badge(containerColor = Color(0xFFF1F5F9), contentColor = Color(0xFF475569)) {
                Text("Period ${period.periodNumber}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
