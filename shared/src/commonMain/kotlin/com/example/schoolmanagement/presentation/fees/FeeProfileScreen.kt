package com.example.schoolmanagement.presentation.fees

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
fun FeeProfileScreen(
    student: StudentListItem,
    viewModel: FeeProfileViewModel,
    initialShowPayDialog: Boolean = false,
    onBack: () -> Unit
) {
    val detailState by viewModel.detailState.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val selectedEnrollmentId by viewModel.selectedEnrollmentId.collectAsState()
    val payments by viewModel.payments.collectAsState()
    val sessionHistory by viewModel.sessionHistory.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    var showPayDialog by remember { mutableStateOf(initialShowPayDialog) }

    LaunchedEffect(student.id, student.studentId) {
        val id = student.id ?: student.studentId
        id?.let { viewModel.loadStudentFeeProfile(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Toolbar
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(FontAwesomeIcons.Solid.ArrowLeft, null, modifier = Modifier.size(18.dp))
            }
            Text("Fee Profile", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        // Student Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0F2F1)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        (student.studentName ?: "S").take(1).uppercase(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D9488)
                    )
                }
                
                Column {
                    Text(student.studentName ?: student.name ?: "-", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "Grade ${student.class_no ?: student.className ?: "-"} | Roll: ${student.rollNo ?: student.roll_no ?: "-"}",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    Text("Scholar No: ${student.scholarNo ?: student.scholar_no ?: "-"}", fontSize = 13.sp, color = Color.Gray)
                }
            }
        }

        // Summary Cards
        when (val state = detailState) {
            is FeeDetailState.Loading -> {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF0D9488))
                }
            }
            is FeeDetailState.Success -> {
                FeeSummaryGrid(state)
            }
            is FeeDetailState.Error -> {
                Text(state.message, color = Color.Red)
            }
        }

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { showPayDialog = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(FontAwesomeIcons.Solid.Wallet, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Record Payment")
            }
            
            OutlinedButton(
                onClick = { /* Print logic */ },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(FontAwesomeIcons.Solid.Print, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Print Report")
            }
        }

        // Payment History
        PaymentHistorySection(
            sessions = sessions,
            selectedEnrollmentId = selectedEnrollmentId,
            onSessionSelected = { viewModel.setSelectedEnrollmentId(it) },
            payments = payments
        )

        // Session History
        SessionHistorySection(sessionHistory)
        
        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showPayDialog) {
        PayFeeDialog(
            student = student,
            enrollmentId = selectedEnrollmentId ?: 0,
            isSaving = isSaving,
            onDismiss = { showPayDialog = false },
            onConfirm = { payload ->
                viewModel.recordPayment(payload)
                showPayDialog = false
            }
        )
    }
}

@Composable
fun FeeSummaryGrid(details: FeeDetailState.Success) {
    val netDue = (details.totalDue - details.discount).coerceAtLeast(0.0)
    val totalLiability = details.annualFee + netDue
    val paidPercent = if (totalLiability > 0) ((details.totalPaid / totalLiability) * 100).toInt() else 0

    val items = listOf(
        Triple("Annual Fee", details.annualFee, Color(0xFF1E293B)),
        Triple("Total Amount (Fee + Balanced)", totalLiability, Color(0xFF6366F1)),
        Triple("Total Paid", details.totalPaid, Color(0xFF10B981)),
        Triple("Discount", details.discount, Color(0xFF3B82F6)),
        Triple("Balanced Amount", netDue, Color(0xFFEF4444)),
        Triple("Paid %", paidPercent.toDouble(), Color(0xFFF59E0B))
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        val rows = items.chunked(3) // 3 columns for better fit of 6 items
        rows.forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                rowItems.forEach { (label, value, color) ->
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray, maxLines = 1)
                            Spacer(modifier = Modifier.height(4.dp))
                            if (label == "Paid %") {
                                Column {
                                    Text("${value.toInt()}%", fontSize = 16.sp, fontWeight = FontWeight.Black, color = color)
                                    LinearProgressIndicator(
                                        progress = { (value / 100.0).toFloat() },
                                        modifier = Modifier.fillMaxWidth().height(3.dp).clip(CircleShape),
                                        color = color,
                                        trackColor = color.copy(alpha = 0.1f)
                                    )
                                }
                            } else {
                                Text(
                                    "₹${value.toInt()}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = color
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentHistorySection(
    sessions: List<StudentSession>,
    selectedEnrollmentId: Int?,
    onSessionSelected: (Int) -> Unit,
    payments: List<InvoiceData>
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Payment History", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                
                Box {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { expanded = true },
                        color = Color(0xFFF1F5F9)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = sessions.find { it.enrollementNo == selectedEnrollmentId }?.sessionName ?: "Select Session",
                                fontSize = 12.sp,
                                color = Color(0xFF0D9488),
                                fontWeight = FontWeight.Bold
                            )
                            Icon(FontAwesomeIcons.Solid.ChevronDown, null, modifier = Modifier.size(10.dp), tint = Color(0xFF0D9488))
                        }
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        sessions.forEach { session ->
                            DropdownMenuItem(
                                text = { Text(session.sessionName) },
                                onClick = {
                                    onSessionSelected(session.enrollementNo)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (payments.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No payment history found", color = Color.Gray, fontSize = 14.sp)
                }
            } else {
                payments.forEach { invoice ->
                    PaymentRow(invoice)
                    if (invoice != payments.last()) HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 12.dp))
                }
            }
        }
    }
}

@Composable
fun PaymentRow(invoice: InvoiceData) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier.size(36.dp).background(Color(0xFFF1F5F9), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(FontAwesomeIcons.Solid.Receipt, null, modifier = Modifier.size(16.dp), tint = Color(0xFF64748B))
            }
            Column {
                Text("Invoice #${invoice.invoiceId}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(invoice.invoiceDate?.take(10) ?: "-", fontSize = 11.sp, color = Color.Gray)
            }
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Text("₹${invoice.amount.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
            Text(invoice.paymentMethod ?: "CASH", fontSize = 10.sp, color = Color.Gray)
        }
    }
}

@Composable
fun SessionHistorySection(history: List<SessionWiseHistory>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Session-wise History", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            // Table Header
            Row(
                modifier = Modifier.fillMaxWidth().background(Color(0xFFF8FAFC)).padding(vertical = 8.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Session", modifier = Modifier.weight(1.2f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Text("Annual Fee", modifier = Modifier.weight(1.2f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Text("Total Paid", modifier = Modifier.weight(1.2f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Text("Due", modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Text("Payments", modifier = Modifier.weight(1.2f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.End)
            }

            if (history.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("No session history available", fontSize = 13.sp, color = Color.Gray)
                }
            } else {
                history.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(item.sessionName, modifier = Modifier.weight(1.2f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("₹${item.totalfees.toInt()}", modifier = Modifier.weight(1.2f), fontSize = 13.sp)
                        Text("₹${item.totalpaid.toInt()}", modifier = Modifier.weight(1.2f), fontSize = 13.sp, color = Color(0xFF10B981), fontWeight = FontWeight.SemiBold)
                        Text("₹${item.totaldue.toInt()}", modifier = Modifier.weight(1f), fontSize = 13.sp, color = if (item.totaldue > 0) Color(0xFFEF4444) else Color(0xFF10B981))
                        Text("${item.paymentsNo} payments", modifier = Modifier.weight(1.2f), fontSize = 11.sp, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.End)
                    }
                    if (item != history.last()) HorizontalDivider(color = Color(0xFFF1F5F9))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayFeeDialog(
    student: StudentListItem,
    enrollmentId: Int,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (InvoicePayload) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("cash") }
    var remarks by remember { mutableStateOf("") }
    var feeHead by remember { mutableStateOf("Tuition Fee") }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.9f).wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Record Fee Payment", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                // Simplified dropdown for Fee Head
                Text("Fee Head", fontSize = 12.sp, color = Color.Gray)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Tuition Fee", "Admission", "Misc").forEach { head ->
                        FilterChip(
                            selected = feeHead == head,
                            onClick = { feeHead = head },
                            label = { Text(head) }
                        )
                    }
                }

                Text("Payment Method", fontSize = 12.sp, color = Color.Gray)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("cash", "online", "cheque").forEach { method ->
                        FilterChip(
                            selected = paymentMethod == method,
                            onClick = { paymentMethod = method },
                            label = { Text(method.uppercase()) }
                        )
                    }
                }

                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Remarks") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val studentId = student.id ?: student.studentId ?: 0
                            onConfirm(InvoicePayload(
                                enrollmentId = enrollmentId,
                                paymentMethod = paymentMethod,
                                studentId = studentId,
                                scholarNo = student.scholarNo ?: "",
                                classNo = student.class_no ?: student.className ?: "",
                                rollNo = student.rollNo ?: student.roll_no ?: "",
                                sessionId = 1, // Default or selected session ID
                                amount = amount.toDoubleOrNull() ?: 0.0,
                                paymentType = feeHead,
                                remarks = remarks
                            ))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        enabled = !isSaving && amount.isNotBlank()
                    ) {
                        Text("Record")
                    }
                }
            }
        }
    }
}
