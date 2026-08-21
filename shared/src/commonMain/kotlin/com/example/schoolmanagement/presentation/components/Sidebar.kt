package com.example.schoolmanagement.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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

// Modern design tokens
private object SidebarColors {
    val Background = Color(0xFFFAFBFC)
    val Surface = Color.White
    val PrimaryText = Color(0xFF0F172A)
    val SecondaryText = Color(0xFF64748B)
    val TertiaryText = Color(0xFF94A3B8)
    val Border = Color(0xFFEEF2F7)
    val Divider = Color(0xFFF1F5F9)
    val Accent = Color(0xFF0D9488)
    val AccentLight = Color(0xFFE6F7F5)
    val AccentDark = Color(0xFF0F766E)
    val Indigo = Color(0xFF6366F1)
    val Danger = Color(0xFFEF4444)
    val DangerLight = Color(0xFFFEF2F2)
}

private object SidebarSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
}

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
            .width(272.dp)
            .fillMaxHeight()
            .background(SidebarColors.Background)
    ) {
        // Brand Header
        BrandHeader()

        Spacer(modifier = Modifier.height(SidebarSpacing.lg))

        // Session Switcher
        Box(modifier = Modifier.padding(horizontal = SidebarSpacing.lg)) {
            SessionSwitcher(sessionViewModel)
        }

        Spacer(modifier = Modifier.height(SidebarSpacing.xl))

        // Dashboard Item (Independent)
        DashboardNavItem(
            isActive = currentRoute == "dashboard",
            onClick = { onNavigate("dashboard") }
        )

        Spacer(modifier = Modifier.height(SidebarSpacing.lg))

        // Menu Items
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = SidebarSpacing.md),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            menuGroups.forEach { group ->
                item {
                    Spacer(modifier = Modifier.height(SidebarSpacing.md))
                    SectionLabel(group.label)
                    Spacer(modifier = Modifier.height(SidebarSpacing.sm))
                }
                items(group.items) { item ->
                    NavMenuItem(
                        item = item,
                        isActive = currentRoute == item.url,
                        onClick = { onNavigate(item.url) }
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(SidebarSpacing.lg))
            }
        }

        // Footer
        UserProfileFooter(
            userName = userName,
            userRole = userRole,
            onLogout = onLogout
        )
    }
}

@Composable
private fun BrandHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SidebarSpacing.xl, vertical = SidebarSpacing.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SidebarSpacing.md)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(MaterialTheme.shapes.small)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(SidebarColors.Accent, SidebarColors.AccentDark)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "RC",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                letterSpacing = 0.5.sp
            )
        }
        Column {
            Text(
                "Rose Convent",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = SidebarColors.PrimaryText,
                letterSpacing = (-0.2).sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                "High School",
                fontSize = 11.sp,
                color = SidebarColors.TertiaryText,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.3.sp
            )
        }
    }
}

@Composable
private fun DashboardNavItem(isActive: Boolean, onClick: () -> Unit) {
    val containerColor = if (isActive) SidebarColors.Accent else Color.Transparent
    val contentColor = if (isActive) Color.White else SidebarColors.PrimaryText
    val iconTint = if (isActive) Color.White else SidebarColors.SecondaryText

    Surface(
        modifier = Modifier
            .padding(horizontal = SidebarSpacing.md)
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .clickable { onClick() },
        color = containerColor,
        shadowElevation = if (isActive) 4.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = SidebarSpacing.md, vertical = SidebarSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SidebarSpacing.md)
        ) {
            Icon(
                imageVector = FontAwesomeIcons.Solid.Columns,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = iconTint
            )
            Text(
                text = "Dashboard",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
        }
    }
}

@Composable
private fun SectionLabel(label: String) {
    Text(
        text = label,
        fontSize = 10.sp,
        fontWeight = FontWeight.ExtraBold,
        color = SidebarColors.TertiaryText,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(horizontal = SidebarSpacing.md)
    )
}

@Composable
private fun NavMenuItem(
    item: MenuItem,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isActive) SidebarColors.AccentLight else Color.Transparent
    val contentColor = if (isActive) SidebarColors.AccentDark else SidebarColors.SecondaryText
    val iconTint = if (isActive) SidebarColors.Accent else SidebarColors.TertiaryText

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .clickable { onClick() },
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = SidebarSpacing.md, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SidebarSpacing.md)
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                modifier = Modifier.size(17.dp),
                tint = iconTint
            )
            Text(
                text = item.title,
                fontSize = 13.5.sp,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
                color = contentColor,
                letterSpacing = (-0.1).sp
            )
        }
    }
}

@Composable
private fun UserProfileFooter(
    userName: String,
    userRole: String,
    onLogout: () -> Unit
) {
    Column {
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = SidebarSpacing.lg),
            thickness = 1.dp,
            color = SidebarColors.Divider
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SidebarSpacing.md, vertical = SidebarSpacing.md)
                .clip(MaterialTheme.shapes.medium)
                .clickable { onLogout() }
                .padding(horizontal = SidebarSpacing.sm, vertical = SidebarSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SidebarSpacing.md)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(SidebarColors.Indigo, Color(0xFF4F46E5))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    userName.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    userName,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SidebarColors.PrimaryText,
                    letterSpacing = (-0.1).sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    userRole,
                    fontSize = 11.sp,
                    color = SidebarColors.TertiaryText,
                    fontWeight = FontWeight.Medium
                )
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(SidebarColors.DangerLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = FontAwesomeIcons.Solid.SignOutAlt,
                    contentDescription = "Logout",
                    modifier = Modifier.size(14.dp),
                    tint = SidebarColors.Danger
                )
            }
        }
    }
}
