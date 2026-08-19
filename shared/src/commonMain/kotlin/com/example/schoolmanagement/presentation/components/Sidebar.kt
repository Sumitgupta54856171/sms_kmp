package com.example.schoolmanagement.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.*

data class MenuItem(
    val title: String,
    val url: String,
    val icon: ImageVector
)

data class MenuGroup(
    val label: String,
    val items: List<MenuItem>
)

val menuGroups = listOf(
    MenuGroup(
        label = "ACADEMICS",
        items = listOf(
            MenuItem("Students", "students", FontAwesomeIcons.Solid.UserGraduate),
            MenuItem("Teachers", "teachers", FontAwesomeIcons.Solid.ChalkboardTeacher),
            MenuItem("Classes & Sections", "class", FontAwesomeIcons.Solid.School),
            MenuItem("Subjects", "subjects", FontAwesomeIcons.Solid.Book),
            MenuItem("Elective Subjects", "elective-subject", FontAwesomeIcons.Solid.BookReader),
            MenuItem("Timetable", "timetable", FontAwesomeIcons.Solid.CalendarAlt),
            MenuItem("Lesson Plans", "lesson-plans", FontAwesomeIcons.Solid.ClipboardList),
        )
    ),
    MenuGroup(
        label = "OPERATIONS",
        items = listOf(
            MenuItem("Attendance", "attendance", FontAwesomeIcons.Solid.UserCheck),
            MenuItem("Attendance Summary", "attendance/summary", FontAwesomeIcons.Solid.ChartBar),
            MenuItem("Examinations", "timetable/exams", FontAwesomeIcons.Solid.FileAlt),
            MenuItem("Grades", "grades", FontAwesomeIcons.Solid.GraduationCap),
            MenuItem("Fee Management", "fees", FontAwesomeIcons.Solid.MoneyBillWave),
            MenuItem("Invoice History", "fees/invoice-history", FontAwesomeIcons.Solid.History),
            MenuItem("Fee Structure", "fees/structure", FontAwesomeIcons.Solid.LayerGroup),
            MenuItem("Transfer Certificate", "tc", FontAwesomeIcons.Solid.FileExport),
            MenuItem("Enrollment", "enrollment", FontAwesomeIcons.Solid.UserPlus),
            MenuItem("Generate Login", "login-generate", FontAwesomeIcons.Solid.Key),
        )
    )
)

@Composable
fun Sidebar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    userName: String,
    userRole: String,
    onLogout: () -> Unit,
    sessionViewModel: com.example.schoolmanagement.presentation.session.SessionViewModel
) {
    Column(
        modifier = Modifier
            .width(260.dp)
            .fillMaxHeight()
            .background(Color.White)
            .padding(vertical = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0D9488)),
                contentAlignment = Alignment.Center
            ) {
                Text("RC", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Column {
                Text("Rose Convent", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("High School", fontSize = 11.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        SessionSwitcher(sessionViewModel)

        Spacer(modifier = Modifier.height(16.dp))

        // Dashboard Item (Independent)
        Surface(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { onNavigate("dashboard") },
            color = if (currentRoute == "dashboard") Color(0xFF0D9488) else Color.Transparent,
            contentColor = if (currentRoute == "dashboard") Color.White else Color(0xFF475569)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = FontAwesomeIcons.Solid.Columns,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (currentRoute == "dashboard") Color.White else Color(0xFF94A3B8)
                )
                Text(text = "Dashboard", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Menu Items
        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            menuGroups.forEach { group ->
                item {
                    Text(
                        text = group.label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                    )
                }
                items(group.items) { item ->
                    val isActive = currentRoute == item.url
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onNavigate(item.url) },
                        color = if (isActive) Color(0xFFF0FDFA) else Color.Transparent,
                        contentColor = if (isActive) Color(0xFF0D9488) else Color(0xFF475569)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = if (isActive) Color(0xFF0D9488) else Color(0xFF94A3B8)
                            )
                            Text(text = item.title, fontSize = 14.sp, fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium)
                        }
                    }
                }
            }
        }

        // Footer
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFF1F5F9))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { onLogout() }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF6366F1)),
                contentAlignment = Alignment.Center
            ) {
                Text(userName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(userName, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(userRole, fontSize = 12.sp, color = Color.Gray)
            }
            Icon(
                imageVector = FontAwesomeIcons.Solid.SignOutAlt,
                contentDescription = "Logout",
                modifier = Modifier.size(18.dp),
                tint = Color(0xFFEF4444)
            )
        }
    }
}
