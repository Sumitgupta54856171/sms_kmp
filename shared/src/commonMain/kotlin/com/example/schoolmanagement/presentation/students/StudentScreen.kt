package com.example.schoolmanagement.presentation.students

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.schoolmanagement.api.models.StudentListItem
import com.example.schoolmanagement.api.models.StudentCreateRequest

@Composable
fun StudentScreen(
    viewModel: StudentViewModel,
    onStudentClick: (StudentListItem) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isAddDialogOpen by viewModel.isAddStudentDialogOpen.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp)
    ) {
        // Toolbar
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search by name or scholar no...") },
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
            
            Button(
                onClick = { viewModel.setAddStudentDialogOpen(true) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("+ Add Student")
            }
        }

        // Content
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (val s = state) {
                is StudentListState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF0D9488))
                }
                is StudentListState.Error -> {
                    Text(s.message, color = Color.Red, modifier = Modifier.align(Alignment.Center))
                }
                is StudentListState.Success -> {
                    StudentTable(s.students, onStudentClick = onStudentClick)
                }
            }
        }
    }

    if (isAddDialogOpen) {
        StudentFormDialog(
            onDismiss = { viewModel.setAddStudentDialogOpen(false) },
            onConfirm = { viewModel.addStudent(it) },
            isSaving = isSaving
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentFormDialog(
    onDismiss: () -> Unit,
    onConfirm: (StudentCreateRequest) -> Unit,
    isSaving: Boolean
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var classNo by remember { mutableStateOf("") }
    var rollNo by remember { mutableStateOf("") }
    var scholarNo by remember { mutableStateOf("") }
    var sssmid by remember { mutableStateOf("") }
    var aadhaar by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var fatherName by remember { mutableStateOf("") }
    var motherName by remember { mutableStateOf("") }
    var apaarId by remember { mutableStateOf("") }
    var penId by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var totalFees by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.9f),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Add New Student", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Text("✕", fontSize = 20.sp, color = Color.Gray)
                    }
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                // Scrollable Form
                Column(
                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FormField("Full Name *", name, { name = it }, "Enter student's full name")
                    FormField("Email *", email, { email = it }, "student@example.com")
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            FormField("Grade *", classNo, { classNo = it }, "e.g. 1")
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            FormField("Roll No *", rollNo, { rollNo = it }, "e.g. 101")
                        }
                    }

                    FormField("Scholar Number *", scholarNo, { scholarNo = it }, "Enter Scholar No.")
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            FormField("SSSMID *", sssmid, { sssmid = it }, "9 Digit SSSMID")
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            FormField("Aadhaar *", aadhaar, { aadhaar = it }, "12 Digit Aadhaar")
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            FormField("Gender *", gender, { gender = it }, "male/female")
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            FormField("Category *", category, { category = it }, "General/OBC/SC/ST")
                        }
                    }

                    FormField("Date of Birth *", dob, { dob = it }, "YYYY-MM-DD")
                    FormField("Phone *", phone, { phone = it }, "Enter 10 digit number")
                    FormField("Father Name *", fatherName, { fatherName = it }, "Enter father's name")
                    FormField("Mother Name *", motherName, { motherName = it }, "Enter mother's name")
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            FormField("APAAR ID", apaarId, { apaarId = it }, "Enter APAAR ID")
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            FormField("PEN ID", penId, { penId = it }, "Enter PEN ID")
                        }
                    }

                    FormField("Address", address, { address = it }, "Enter full address")
                    FormField("Total Annual Fees (₹) *", totalFees, { totalFees = it }, "Enter total annual fees")
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                // Footer
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.padding(end = 8.dp)) {
                        Text("Cancel", color = Color.Gray)
                    }
                    Button(
                        onClick = {
                            onConfirm(StudentCreateRequest(
                                name = name,
                                email = email,
                                class_no = classNo,
                                roll_no = rollNo,
                                scholar_no = scholarNo,
                                sssmid = sssmid,
                                aadhaar = aadhaar,
                                gender = gender,
                                category = category,
                                dob = dob,
                                phone = phone,
                                father_name = fatherName,
                                mother_name = motherName,
                                apaarId = apaarId.ifBlank { null },
                                penId = penId.ifBlank { null },
                                address = address.ifBlank { null },
                                total_fees = totalFees
                            ))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                        enabled = !isSaving && name.isNotBlank() && scholarNo.isNotBlank()
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                        }
                        Text("Add Student")
                    }
                }
            }
        }
    }
}

@Composable
fun FormField(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String) {
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

@Composable
fun StudentTable(students: List<StudentListItem>, onStudentClick: (StudentListItem) -> Unit) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWide = maxWidth > 800.dp
        
        if (isWide) {
            // Desktop Table Header
            Column {
                HeaderRow()
                LazyColumn(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    items(students) { student ->
                        StudentRow(student, onClick = { onStudentClick(student) })
                    }
                }
            }
        } else {
            // Mobile Card List
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(students) { student ->
                    StudentCard(student, onClick = { onStudentClick(student) })
                }
            }
        }
    }
}

@Composable
fun HeaderRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Student", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
        Text("Scholar No", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
        Text("Father's Name", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
        Text("Status", modifier = Modifier.weight(0.8f), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun StudentRow(student: StudentListItem, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(student.studentName ?: "-", modifier = Modifier.weight(2f), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(student.scholarNo ?: "-", modifier = Modifier.weight(1f), fontSize = 14.sp)
            Text(student.faterhName ?: "-", modifier = Modifier.weight(1.5f), fontSize = 14.sp)
            StatusBadge(student.status ?: "active", modifier = Modifier.weight(0.8f))
        }
    }
}

@Composable
fun StudentCard(student: StudentListItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(student.studentName ?: "-", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                StatusBadge(student.status ?: "active")
            }
            Text("Scholar No: ${student.scholarNo ?: "-"}", fontSize = 12.sp, color = Color.Gray)
            Text("Father: ${student.faterhName ?: "-"}", fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    val isActive = status.lowercase() == "active"
    Surface(
        modifier = modifier.clip(RoundedCornerShape(4.dp)),
        color = if (isActive) Color(0xFFF0FDFA) else Color(0xFFF1F5F9),
        contentColor = if (isActive) Color(0xFF0D9488) else Color(0xFF64748B)
    ) {
        Text(
            text = status.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
