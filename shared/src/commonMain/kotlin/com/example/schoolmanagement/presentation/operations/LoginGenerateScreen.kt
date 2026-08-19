package com.example.schoolmanagement.presentation.operations

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import compose.icons.fontawesomeicons.solid.*

@Composable
fun LoginGenerateScreen(viewModel: LoginGenerateViewModel) {
    val state by viewModel.state.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()

    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp)
    ) {
        Text("Generate Logins", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
        Text("Bulk generate credentials for students and parents", fontSize = 14.sp, color = Color(0xFF64748B), modifier = Modifier.padding(bottom = 24.dp))

        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // Student List
            Card(
                modifier = Modifier.weight(1.5f).fillMaxHeight(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Select Students", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Badge(containerColor = Color(0xFFE0F2F1), contentColor = Color(0xFF0D9488)) {
                            Text("${selectedIds.size} Selected", fontSize = 10.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Box(modifier = Modifier.weight(1f)) {
                        when (val s = state) {
                            is LoginGenerateState.Loading -> CircularProgressIndicator(color = Color(0xFF0D9488), modifier = Modifier.align(Alignment.Center))
                            is LoginGenerateState.Error -> Text(s.message, color = Color.Red)
                            is LoginGenerateState.Success -> {
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(s.students) { student ->
                                        val id = student.id ?: student.studentId ?: 0
                                        StudentSelectRow(
                                            name = student.studentName ?: student.name ?: "-",
                                            scholarNo = student.scholarNo ?: student.scholar_no ?: "-",
                                            isSelected = selectedIds.contains(id),
                                            onClick = { viewModel.toggleStudentSelection(id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Generation Form
            Card(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Settings", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Common Password") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = { viewModel.generateLogins("STUDENT", password) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                        enabled = !isGenerating && selectedIds.isNotEmpty() && password.isNotBlank(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Generate Student Logins")
                    }

                    Button(
                        onClick = { viewModel.generateLogins("PARENT", password) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                        enabled = !isGenerating && selectedIds.isNotEmpty() && password.isNotBlank(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Generate Parent Logins")
                    }
                }
            }
        }
    }
}

@Composable
fun StudentSelectRow(name: String, scholarNo: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = if (isSelected) Color(0xFFF0FDFA) else Color.Transparent,
        shape = RoundedCornerShape(8.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0D9488).copy(0.3f)) else null
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onClick() },
                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF0D9488))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(name, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("Scholar: $scholarNo", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}
