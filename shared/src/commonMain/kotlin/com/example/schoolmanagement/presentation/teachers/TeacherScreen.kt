package com.example.schoolmanagement.presentation.teachers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.schoolmanagement.api.models.TeacherData
import com.example.schoolmanagement.api.models.TeacherResponse
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.*

@Composable
fun TeacherScreen(viewModel: TeacherViewModel) {
    val state by viewModel.state.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isAddDialogOpen by viewModel.isAddDialogOpen.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp)
    ) {
        // Premium Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Teachers Management", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E293B))
                Text("Manage faculty members, schedules, and assignments", fontSize = 14.sp, color = Color(0xFF64748B))
            }
            
            Button(
                onClick = { viewModel.setAddDialogOpen(true) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Icon(FontAwesomeIcons.Solid.Plus, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Add Teacher", fontWeight = FontWeight.Bold)
            }
        }

        // Search Bar with shadow
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.onSearchQueryChange(it) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp).shadow(2.dp, RoundedCornerShape(12.dp)),
            placeholder = { Text("Search by name, ID, or email...", color = Color.Gray) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color(0xFF0D9488)
            ),
            leadingIcon = { Icon(FontAwesomeIcons.Solid.Search, null, modifier = Modifier.size(18.dp), tint = Color.Gray) }
        )

        // Grid List
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (val s = state) {
                is TeacherListState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF0D9488))
                is TeacherListState.Error -> Text(s.message, color = Color.Red, modifier = Modifier.align(Alignment.Center))
                is TeacherListState.Success -> {
                    if (s.teachers.isEmpty()) {
                        EmptyTeachersView()
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 280.dp),
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(s.teachers) { teacher ->
                                TeacherGridCard(
                                    teacher = teacher,
                                    onDelete = { viewModel.deleteTeacher(teacher.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (isAddDialogOpen) {
        AddTeacherDialog(
            onDismiss = { viewModel.setAddDialogOpen(false) },
            onConfirm = { viewModel.addTeacher(it) },
            isSaving = isSaving
        )
    }
}

@Composable
fun TeacherGridCard(teacher: TeacherResponse, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar with Glow
            Box(contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.size(86.dp).clip(CircleShape).background(Color(0xFFE0F2F1).copy(alpha = 0.5f)))
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEEF2FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        teacher.fullName.take(1).uppercase(),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0D9488)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(teacher.fullName, fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF1E293B))
            Text(teacher.subject_specialization ?: "General Faculty", fontSize = 13.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(teacher.employee_id, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Box(modifier = Modifier.size(4.dp).background(Color.LightGray, CircleShape))
                Badge(
                    containerColor = if (teacher.status == "active") Color(0xFFF0FDFA) else Color(0xFFF1F5F9),
                    contentColor = if (teacher.status == "active") Color(0xFF0D9488) else Color(0xFF64748B)
                ) {
                    Text(teacher.status.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Black)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Tags Row
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                teacher.gender?.let {
                    Surface(color = Color(0xFFEFF6FF), shape = RoundedCornerShape(6.dp)) {
                        Text(it, fontSize = 10.sp, color = Color(0xFF3B82F6), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                    }
                }
                teacher.sssmid?.let {
                    Surface(color = Color(0xFFFAF5FF), shape = RoundedCornerShape(6.dp)) {
                        Text("SSSMID: $it", fontSize = 10.sp, color = Color(0xFF8B5CF6), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            
            // Actions Footer
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ActionIconButton(FontAwesomeIcons.Solid.Envelope, Color(0xFF3B82F6))
                ActionIconButton(FontAwesomeIcons.Solid.Phone, Color(0xFF475569))
                ActionIconButton(FontAwesomeIcons.Solid.Pen, Color(0xFF475569))
                ActionIconButton(FontAwesomeIcons.Solid.ShieldAlt, Color(0xFFF59E0B))
                ActionIconButton(FontAwesomeIcons.Solid.CalendarAlt, Color(0xFF0D9488))
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(FontAwesomeIcons.Solid.Trash, null, modifier = Modifier.size(14.dp), tint = Color(0xFFEF4444))
                }
            }
        }
    }
}

@Composable
fun ActionIconButton(icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    IconButton(onClick = { /* Action */ }, modifier = Modifier.size(36.dp)) {
        Icon(icon, null, modifier = Modifier.size(14.dp), tint = color)
    }
}

@Composable
fun EmptyTeachersView() {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(FontAwesomeIcons.Solid.UserFriends, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
        Spacer(modifier = Modifier.height(16.dp))
        Text("No teachers found", fontWeight = FontWeight.Bold, color = Color.Gray)
        Text("Click 'Add Teacher' to create one", fontSize = 12.sp, color = Color.LightGray)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTeacherDialog(
    onDismiss: () -> Unit,
    onConfirm: (TeacherData) -> Unit,
    isSaving: Boolean
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var employeeId by remember { mutableStateOf("") }
    var specialization by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth(0.9f).wrapContentHeight()
        ) {
            Column(
                modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Add New Teacher", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                
                TeacherFormField("Full Name", fullName, { fullName = it }, "Enter full name")
                TeacherFormField("Email", email, { email = it }, "email@example.com")
                TeacherFormField("Employee ID", employeeId, { employeeId = it }, "e.g. EMP101")
                TeacherFormField("Specialization", specialization, { specialization = it }, "e.g. Mathematics")
                TeacherFormField("Phone", phone, { phone = it }, "10 digit number")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.Gray)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onConfirm(TeacherData(
                                fullName = fullName,
                                email = email,
                                employee_id = employeeId,
                                subject_specialization = specialization,
                                phone = phone
                            ))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                        enabled = !isSaving && fullName.isNotBlank() && email.isNotBlank()
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                        } else {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherFormField(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, fontSize = 14.sp) },
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
    }
}
