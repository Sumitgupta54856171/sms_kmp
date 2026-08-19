package com.example.schoolmanagement.presentation.fees

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.*

@Composable
fun FeeScreen(
    viewModel: FeeViewModel,
    onViewFees: (StudentListItem) -> Unit,
    onPayFees: (StudentListItem) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val selectedClass by viewModel.selectedClass.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()

    val allClasses = listOf(
        "All Classes", "Nursery", "LKG", "UKG",
        "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Fee Management", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                Text("Manage student fees and collection", fontSize = 14.sp, color = Color(0xFF64748B))
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ClassDropdown(
                    selectedClass = selectedClass,
                    classes = allClasses,
                    onClassSelected = { viewModel.setSelectedClass(it) }
                )
            }
        }

        // Search and Sort
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search by name, scholar no...") },
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                leadingIcon = { Icon(FontAwesomeIcons.Solid.Search, null, modifier = Modifier.size(16.dp)) }
            )
            
            IconButton(
                onClick = { viewModel.setSortOrder(if (sortOrder == "asc") "desc" else "asc") },
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .size(48.dp)
            ) {
                Icon(
                    if (sortOrder == "asc") FontAwesomeIcons.Solid.SortAmountUp else FontAwesomeIcons.Solid.SortAmountDown,
                    contentDescription = "Sort",
                    tint = Color(0xFF0D9488)
                )
            }
        }

        // Student List
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (val s = state) {
                is FeeListState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF0D9488))
                }
                is FeeListState.Error -> {
                    Text(s.message, color = Color.Red, modifier = Modifier.align(Alignment.Center))
                }
                is FeeListState.Success -> {
                    if (s.students.isEmpty()) {
                        Text("No students found", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(s.students) { student ->
                                FeeStudentCard(
                                    student = student,
                                    onViewFees = { onViewFees(student) },
                                    onPayFees = { onPayFees(student) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassDropdown(
    selectedClass: String,
    classes: List<String>,
    onClassSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Surface(
            modifier = Modifier
                .width(160.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable { expanded = true },
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (selectedClass == "All Classes") selectedClass else "Grade $selectedClass",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Icon(FontAwesomeIcons.Solid.ChevronDown, null, modifier = Modifier.size(12.dp), tint = Color.Gray)
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            classes.forEach { className ->
                DropdownMenuItem(
                    text = { Text(if (className == "All Classes") className else "Grade $className") },
                    onClick = {
                        onClassSelected(className)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun FeeStudentCard(
    student: StudentListItem,
    onViewFees: () -> Unit,
    onPayFees: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F5F9)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        (student.studentName ?: "S").take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D9488)
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(student.studentName ?: student.name ?: "-", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Scholar No: ${student.scholarNo ?: student.scholar_no ?: "-"}", fontSize = 12.sp, color = Color.Gray)
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Badge(
                        containerColor = if (student.status?.lowercase() == "active") Color(0xFFF0FDFA) else Color(0xFFF1F5F9),
                        contentColor = if (student.status?.lowercase() == "active") Color(0xFF0D9488) else Color(0xFF64748B)
                    ) {
                        Text(student.status?.uppercase() ?: "ACTIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("Roll: ${student.rollNo ?: student.roll_no ?: "-"}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onViewFees,
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Icon(FontAwesomeIcons.Solid.Eye, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("View Fees", fontSize = 12.sp)
                }
                
                Button(
                    onClick = onPayFees,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Icon(FontAwesomeIcons.Solid.Wallet, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Pay Fees", fontSize = 12.sp)
                }
            }
        }
    }
}
