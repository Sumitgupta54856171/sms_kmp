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
    val editingTeacher by viewModel.editingTeacher.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        val isCompact = maxWidth < 600.dp
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isCompact) 12.dp else 24.dp)
        ) {
            // Premium Header - Responsive
            if (isCompact) {
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    Text("Teachers", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E293B))
                    Text("Manage faculty members", fontSize = 13.sp, color = Color(0xFF64748B))
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.setAddDialogOpen(true) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(FontAwesomeIcons.Solid.Plus, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Add Teacher", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Teachers Management", fontSize = 30.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E293B))
                        Text("Manage faculty members, schedules, and assignments", fontSize = 14.sp, color = Color(0xFF64748B))
                    }
                    
                    Button(
                        onClick = { viewModel.setAddDialogOpen(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Icon(FontAwesomeIcons.Solid.Plus, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Add Teacher", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier.fillMaxWidth().padding(bottom = if (isCompact) 16.dp else 24.dp).shadow(2.dp, RoundedCornerShape(12.dp)),
                placeholder = { Text("Search by name, ID, or email...", color = Color.Gray, fontSize = 14.sp) },
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
                                columns = if (isCompact) GridCells.Fixed(1) else GridCells.Adaptive(minSize = 300.dp),
                                horizontalArrangement = Arrangement.spacedBy(20.dp),
                                verticalArrangement = Arrangement.spacedBy(20.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(s.teachers) { teacher ->
                                    TeacherGridCard(
                                        teacher = teacher,
                                        onEdit = { viewModel.setAddDialogOpen(true, teacher) },
                                        onDelete = { viewModel.deleteTeacher(teacher.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (isAddDialogOpen) {
        TeacherFormDialog(
            onDismiss = { viewModel.setAddDialogOpen(false) },
            onConfirm = { viewModel.saveTeacher(it) },
            isSaving = isSaving,
            editingTeacher = editingTeacher
        )
    }
}

@Composable
fun TeacherGridCard(teacher: TeacherResponse, onEdit: () -> Unit, onDelete: () -> Unit) {
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
                ActionIconButton(FontAwesomeIcons.Solid.Envelope, Color(0xFF3B82F6)) { /* Mail */ }
                ActionIconButton(FontAwesomeIcons.Solid.Phone, Color(0xFF475569)) { /* Call */ }
                ActionIconButton(FontAwesomeIcons.Solid.Pen, Color(0xFF475569), onEdit)
                ActionIconButton(FontAwesomeIcons.Solid.ShieldAlt, Color(0xFFF59E0B)) { /* Role */ }
                ActionIconButton(FontAwesomeIcons.Solid.CalendarAlt, Color(0xFF0D9488)) { /* Schedule */ }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(FontAwesomeIcons.Solid.Trash, null, modifier = Modifier.size(14.dp), tint = Color(0xFFEF4444))
                }
            }
        }
    }
}

@Composable
fun ActionIconButton(icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, onClick: () -> Unit = {}) {
    IconButton(onClick = onClick, modifier = Modifier.size(36.dp)) {
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
fun TeacherFormDialog(
    onDismiss: () -> Unit,
    onConfirm: (TeacherData) -> Unit,
    isSaving: Boolean,
    editingTeacher: TeacherResponse? = null
) {
    val isEditing = editingTeacher != null

    var fullName by remember { mutableStateOf(editingTeacher?.fullName ?: "") }
    var email by remember { mutableStateOf(editingTeacher?.email ?: "") }
    var employeeId by remember { mutableStateOf(editingTeacher?.employee_id ?: "") }
    var phone by remember { mutableStateOf(editingTeacher?.phone ?: "") }
    var specialization by remember { mutableStateOf(editingTeacher?.subject_specialization ?: "") }
    var education by remember { mutableStateOf(editingTeacher?.education ?: "") }
    
    var gender by remember { mutableStateOf(editingTeacher?.gender ?: "") }
    var aadhaarId by remember { mutableStateOf(editingTeacher?.aadhaar_id ?: "") }
    var sssmid by remember { mutableStateOf(editingTeacher?.sssmid ?: "") }
    var status by remember { mutableStateOf(editingTeacher?.status ?: "active") }
    var password by remember { mutableStateOf("") }

    val genders = listOf("male", "female", "other")
    val statuses = listOf("active", "inactive")

    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth(0.95f).wrapContentHeight()
        ) {
            Column(
                modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEditing) "Edit Teacher" else "Add New Teacher",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1E293B)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(FontAwesomeIcons.Solid.Times, null, modifier = Modifier.size(20.dp), tint = Color.Gray)
                    }
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    TeacherFormField("Full Name *", fullName, { fullName = it }, "Enter full name")
                    TeacherFormField("Email", email, { email = it }, "email@school.com")
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TeacherFormField("Employee ID *", employeeId, { employeeId = it }, "EMP-2026", Modifier.weight(1f))
                        TeacherFormField("Phone", phone, { phone = it }, "10 digit number", Modifier.weight(1f))
                    }

                    TeacherFormField("Subject Specialization", specialization, { specialization = it }, "e.g. Mathematics")
                    TeacherFormField("Education", education, { education = it }, "e.g. B.Ed, M.A.")
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Gender", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                            TeacherSelectField(gender, genders) { gender = it }
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Status", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                            TeacherSelectField(status, statuses) { status = it }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TeacherFormField("Aadhaar ID", aadhaarId, { aadhaarId = it }, "12 digit ID", Modifier.weight(1f))
                        TeacherFormField("SSSMID", sssmid, { sssmid = it }, "9 digit ID", Modifier.weight(1f))
                    }

                    TeacherFormField("Password ${if (isEditing) "(leave blank to keep)" else "*"}", password, { password = it }, "Set login password")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.Gray)
                    }
                    Spacer(Modifier.width(12.dp))
                    Button(
                        onClick = {
                            onConfirm(TeacherData(
                                fullName = fullName,
                                email = email,
                                employee_id = employeeId,
                                phone = phone,
                                subject_specialization = specialization,
                                gender = gender,
                                aadhaar_id = aadhaarId,
                                sssmid = sssmid,
                                status = status,
                                education = education,
                                password = password.ifBlank { null }
                            ))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                        enabled = !isSaving && fullName.isNotBlank() && employeeId.isNotBlank() && (isEditing || password.isNotBlank()),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                        } else {
                            Text(if (isEditing) "Update Teacher" else "Save Teacher", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherSelectField(selected: String, items: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Surface(
            modifier = Modifier.fillMaxWidth().clickable { expanded = true },
            color = Color.White,
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (selected.isBlank()) "Select..." else selected.replaceFirstChar { it.uppercase() },
                    fontSize = 14.sp,
                    color = if (selected.isBlank()) Color.LightGray else Color(0xFF1E293B)
                )
                Icon(FontAwesomeIcons.Solid.ChevronDown, null, modifier = Modifier.size(10.dp), tint = Color.Gray)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.replaceFirstChar { it.uppercase() }) },
                    onClick = { onSelect(item); expanded = false }
                )
            }
        }
    }
}

@Composable
fun TeacherFormField(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, fontSize = 14.sp, color = Color.LightGray) },
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color(0xFF0D9488),
                unfocusedBorderColor = Color(0xFFE2E8F0)
            )
        )
    }
}
