package com.example.schoolmanagement.presentation.operations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.FileExport
import compose.icons.fontawesomeicons.solid.Search

@Composable
fun TCScreen(viewModel: TCViewModel) {
    val state by viewModel.state.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Transfer Certificate", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
        Text("Search and generate student TCs", fontSize = 14.sp, color = Color(0xFF64748B), modifier = Modifier.padding(bottom = 24.dp))

        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Student Search", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.searchStudent(it) },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Enter Scholar Number") },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        leadingIcon = { Icon(FontAwesomeIcons.Solid.Search, null, modifier = Modifier.size(16.dp)) }
                    )
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            when (val s = state) {
                is TCState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF0D9488))
                is TCState.Error -> Text(s.message, color = Color.Red, modifier = Modifier.align(Alignment.Center))
                is TCState.Success -> {
                    TCForm(s.student)
                }
                is TCState.Idle -> {
                    Text("Search for a student to begin", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

@Composable
fun TCForm(student: com.example.schoolmanagement.api.models.StudentListItem) {
    var schNo by remember { mutableStateOf(student.scholarNo ?: student.scholar_no ?: "") }
    var bookNo by remember { mutableStateOf("") }
    var tcNo by remember { mutableStateOf("") }
    var pupilName by remember { mutableStateOf(student.studentName ?: student.name ?: "") }
    var dob by remember { mutableStateOf("") }
    var dobWords by remember { mutableStateOf("") }
    var fatherName by remember { mutableStateOf(student.faterhName ?: student.father_name ?: "") }
    var motherName by remember { mutableStateOf(student.motherName ?: "") }
    var caste by remember { mutableStateOf("") }
    var placeOfBirth by remember { mutableStateOf("") }
    var tehsil by remember { mutableStateOf("") }
    var periodStay by remember { mutableStateOf("") }
    var motherTongue by remember { mutableStateOf("Hindi") }
    var dateAdmission by remember { mutableStateOf("") }
    var admRegNo by remember { mutableStateOf(student.scholarNo ?: student.scholar_no ?: "") }
    var classAdmitted by remember { mutableStateOf("") }
    var dateLeaving by remember { mutableStateOf("") }
    var classLeft by remember { mutableStateOf("Grade ${student.class_no ?: student.className ?: ""}") }
    var reason by remember { mutableStateOf("Passed") }
    var lastExam by remember { mutableStateOf("") }
    var character by remember { mutableStateOf("Good") }
    var issueDate by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(FontAwesomeIcons.Solid.FileExport, null, tint = Color(0xFF0D9488), modifier = Modifier.size(24.dp))
                Text("Transfer Certificate Details", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E293B))
            }

            HorizontalDivider(color = Color(0xFFF1F5F9))

            // IDs Section
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth().background(Color(0xFFF0FDFA), RoundedCornerShape(12.dp)).padding(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TCInputField("Sch. No.", schNo, { schNo = it }, Modifier.weight(1f))
                    TCInputField("Book No.", bookNo, { bookNo = it }, Modifier.weight(1f))
                    TCInputField("T.C. No.", tcNo, { tcNo = it }, Modifier.weight(1f))
                }
            }

            // Personal Section
            TCSectionTitle("STUDENT & PARENTS")
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                TCInputField("Name of Pupil", pupilName, { pupilName = it })
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TCInputField("Date of Birth (Fig)", dob, { dob = it }, Modifier.weight(1f))
                    TCInputField("DOB (Words)", dobWords, { dobWords = it }, Modifier.weight(2f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TCInputField("Father's Name", fatherName, { fatherName = it }, Modifier.weight(1f))
                    TCInputField("Mother's Name", motherName, { motherName = it }, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TCInputField("Caste", caste, { caste = it }, Modifier.weight(1f))
                    TCInputField("Place of Birth", placeOfBirth, { placeOfBirth = it }, Modifier.weight(1f))
                }
            }

            // Academic Section
            TCSectionTitle("ACADEMIC RECORD")
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TCInputField("Adm. Date", dateAdmission, { dateAdmission = it }, Modifier.weight(1f))
                    TCInputField("Adm. Reg No.", admRegNo, { admRegNo = it }, Modifier.weight(1f))
                }
                TCInputField("Class Admitted In", classAdmitted, { classAdmitted = it })
                TCInputField("Class Left From", classLeft, { classLeft = it })
                TCInputField("Reason for Leaving", reason, { reason = it })
                TCInputField("Last Exam Passed", lastExam, { lastExam = it })
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TCInputField("Character", character, { character = it }, Modifier.weight(1f))
                    TCInputField("Issue Date", issueDate, { issueDate = it }, Modifier.weight(1f))
                }
            }

            Button(
                onClick = { /* Generation logic */ },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(FontAwesomeIcons.Solid.FileExport, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(12.dp))
                Text("Generate & Preview TC", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun TCSectionTitle(title: String) {
    Text(title, fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF64748B), letterSpacing = 1.sp, modifier = Modifier.padding(top = 8.dp))
}

@Composable
fun TCInputField(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
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
fun TCField(label: String, value: String) {
    Column {
        Text(label, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E293B))
    }
}
