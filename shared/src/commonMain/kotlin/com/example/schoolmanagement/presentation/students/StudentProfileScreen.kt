package com.example.schoolmanagement.presentation.students

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.example.schoolmanagement.api.models.*
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.*

@Composable
fun StudentProfileScreen(
    studentId: Int,
    viewModel: StudentProfileViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var activeTab by remember { mutableStateOf("profile") }

    LaunchedEffect(studentId) {
        viewModel.loadStudentProfile(studentId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(FontAwesomeIcons.Solid.ArrowLeft, contentDescription = "Back")
                }
                
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(Color(0xFFE0F2F1)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(FontAwesomeIcons.Solid.User, contentDescription = null, tint = Color(0xFF0D9488), modifier = Modifier.size(20.dp))
                }
                
                Column {
                    Text("Student Profile", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    if (state is StudentProfileState.Success) {
                        val student = (state as StudentProfileState.Success).data.student
                        Text("${student?.studentName ?: student?.name ?: ""} • Grade ${student?.class_no ?: "-"}", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }

        // Tabs
        ScrollableTabRow(
            selectedTabIndex = when(activeTab) {
                "profile" -> 0
                "bank" -> 1
                "photo" -> 2
                "idcard" -> 3
                "tc" -> 4
                else -> 0
            },
            containerColor = Color.White,
            contentColor = Color(0xFF0D9488),
            edgePadding = 16.dp,
            divider = {}
        ) {
            val tabs = listOf(
                "profile" to "Profile",
                "bank" to "Bank",
                "photo" to "Photo",
                "idcard" to "ID Card",
                "tc" to "TC"
            )
            tabs.forEach { (value, label) ->
                Tab(
                    selected = activeTab == value,
                    onClick = { activeTab = value },
                    text = { Text(label, fontSize = 14.sp) }
                )
            }
        }

        // Content
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (val s = state) {
                is StudentProfileState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF0D9488))
                }
                is StudentProfileState.Error -> {
                    Text(s.message, color = Color.Red, modifier = Modifier.align(Alignment.Center))
                }
                is StudentProfileState.Success -> {
                    val data = s.data
                    when (activeTab) {
                        "profile" -> ProfileDetailTab(data.student)
                        "bank" -> BankDetailTab(data.bank, onSave = { viewModel.saveBankDetails(studentId, it) }, isSaving = viewModel.isSavingBank.collectAsState().value)
                        "photo" -> UploadPhotoTab(data.photo, onRemove = { viewModel.removePhoto(studentId) })
                        "idcard" -> IDCardTab(data.student)
                        "tc" -> TCTab(data.student)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileDetailTab(student: StudentListItem?) {
    if (student == null) return
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                InfoItem("Name", student.studentName ?: student.name ?: "-")
                InfoItem("Scholar No", student.scholarNo ?: student.scholar_no ?: "-")
                InfoItem("Roll No", student.rollNo ?: student.roll_no ?: "-")
                InfoItem("Class", "Grade ${student.class_no ?: "-"}")
                InfoItem("Email", student.email ?: "-")
                InfoItem("Phone", student.phone ?: "-")
                InfoItem("Father's Name", student.father_name ?: student.faterhName ?: "-")
                InfoItem("Mother's Name", student.motherName ?: "-")
                InfoItem("Gender", student.gender ?: "-")
                InfoItem("Date of Birth", student.dob ?: "-")
                InfoItem("SSSMID", student.sssmid ?: "-")
                InfoItem("Aadhaar", student.aadhaar ?: "-")
                InfoItem("Address", student.address ?: "-")
            }
        }
    }
}

@Composable
fun InfoItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(label, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E293B))
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp), color = Color(0xFFF1F5F9))
    }
}

@Composable
fun BankDetailTab(bank: BankDetailData?, onSave: (BankDetailData) -> Unit, isSaving: Boolean) {
    var accountHolder by remember { mutableStateOf(bank?.AccountHolderName ?: "") }
    var bankName by remember { mutableStateOf(bank?.bankName ?: "") }
    var accountNumber by remember { mutableStateOf(bank?.accountNumber ?: "") }
    var ifscCode by remember { mutableStateOf(bank?.ifscCode ?: "") }
    var branchName by remember { mutableStateOf(bank?.branchName ?: "") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Bank Information", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                
                OutlinedTextField(value = accountHolder, onValueChange = { accountHolder = it }, label = { Text("Account Holder Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = bankName, onValueChange = { bankName = it }, label = { Text("Bank Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = accountNumber, onValueChange = { accountNumber = it }, label = { Text("Account Number") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = ifscCode, onValueChange = { ifscCode = it }, label = { Text("IFSC Code") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = branchName, onValueChange = { branchName = it }, label = { Text("Branch Name") }, modifier = Modifier.fillMaxWidth())

                Button(
                    onClick = { onSave(BankDetailData(AccountHolderName = accountHolder, bankName = bankName, accountNumber = accountNumber, ifscCode = ifscCode, branchName = branchName)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                    enabled = !isSaving
                ) {
                    if (isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    else Text("Save Bank Details")
                }
            }
        }
    }
}

@Composable
fun UploadPhotoTab(photo: PhotoData?, onRemove: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            if (photo?.filePath != null) {
                // Image loading logic would go here
                Text("Photo Uploaded")
            } else {
                Icon(FontAwesomeIcons.Solid.User, contentDescription = null, modifier = Modifier.size(100.dp), tint = Color.LightGray)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = { /* Pick photo logic */ }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488))) {
                Text("Select Photo")
            }
            if (photo?.filePath != null) {
                Button(onClick = onRemove, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                    Text("Remove")
                }
            }
        }
    }
}

@Composable
fun IDCardTab(student: StudentListItem?) {
    if (student == null) return
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.size(300.dp, 450.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Simplified ID Card UI mirroring the React version
                Box(modifier = Modifier.fillMaxWidth().height(80.dp).background(Color(0xFF1A2B4C))) {
                    Text("ROSE CONVENT", color = Color.White, modifier = Modifier.align(Alignment.Center), fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.size(120.dp).align(Alignment.CenterHorizontally).background(Color.LightGray))
                Spacer(modifier = Modifier.height(16.dp))
                Text(student.studentName ?: student.name ?: "", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
                Text("Sch No: ${student.scholarNo ?: "-"}", modifier = Modifier.align(Alignment.CenterHorizontally), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(16.dp))
                InfoItemSmall("Father", student.father_name ?: student.faterhName ?: "-")
                InfoItemSmall("Class", "Grade ${student.class_no ?: "-"}")
                InfoItemSmall("Phone", student.phone ?: "-")
            }
        }
    }
}

@Composable
fun InfoItemSmall(label: String, value: String) {
    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, fontSize = 10.sp, color = Color.Gray, modifier = Modifier.width(60.dp))
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun TCTab(student: StudentListItem?) {
    if (student == null) return
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Transfer Certificate", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Student details for TC generation")
                InfoItemSmall("Pupil Name", student.studentName ?: student.name ?: "")
                InfoItemSmall("Scholar No", student.scholarNo ?: student.scholar_no ?: "")
                // ... other fields matching React form ...
                Button(onClick = { /* Print TC logic */ }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488))) {
                    Text("Generate & Print TC")
                }
            }
        }
    }
}
