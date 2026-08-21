package com.example.schoolmanagement.presentation.operations

import androidx.compose.foundation.background
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
import com.example.schoolmanagement.api.models.InvoiceHistoryItem
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.*

@Composable
fun InvoiceHistoryScreen(viewModel: InvoiceHistoryViewModel) {
    val state by viewModel.state.collectAsState()
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        val screenWidth = maxWidth
        val isCompact = screenWidth < 600.dp
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isCompact) 16.dp else 32.dp)
        ) {
            // Responsive Header
            InvoiceHeader(isCompact)

            Spacer(modifier = Modifier.height(24.dp))

            // Stats Row
            if (state is InvoiceHistoryState.Success) {
                val data = (state as InvoiceHistoryState.Success).data
                InvoiceStatsRow(data, isCompact)
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Filters & Search
            FilterSearchSection(
                startDate = startDate,
                endDate = endDate,
                searchQuery = searchQuery,
                onSearchChange = { viewModel.onSearchQueryChange(it) },
                onRefresh = { viewModel.loadInvoices() }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Invoices List
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (val s = state) {
                    is InvoiceHistoryState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF0D9488))
                    }
                    is InvoiceHistoryState.Error -> {
                        ErrorView(s.message) { viewModel.loadInvoices() }
                    }
                    is InvoiceHistoryState.Success -> {
                        val invoices = s.data.invoice.filter { 
                            searchQuery.isEmpty() || 
                            (it.studentName?.contains(searchQuery, ignoreCase = true) == true) ||
                            (it.invoiceId.toString().contains(searchQuery)) == true
                        }

                        if (invoices.isEmpty()) {
                            EmptyInvoicesView()
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(bottom = 24.dp)
                            ) {
                                items(invoices) { invoice ->
                                    InvoiceCard(invoice, isCompact)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InvoiceHeader(isCompact: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(
            modifier = Modifier.size(if (isCompact) 44.dp else 52.dp).clip(MaterialTheme.shapes.small).background(Color(0xFFF0FDFA)),
            contentAlignment = Alignment.Center
        ) {
            Icon(FontAwesomeIcons.Solid.History, null, modifier = Modifier.size(if (isCompact) 20.dp else 24.dp), tint = Color(0xFF0D9488))
        }
        Column {
            Text(
                "Invoice History",
                fontSize = if (isCompact) 22.sp else 28.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF1E293B),
                letterSpacing = (-0.5).sp
            )
            Text("Track and manage all fee collection records", fontSize = 14.sp, color = Color(0xFF64748B))
        }
    }
}

@Composable
private fun InvoiceStatsRow(data: com.example.schoolmanagement.api.models.InvoiceHistoryResponse, isCompact: Boolean) {
    val items = listOf(
        Triple("Total Invoices", data.invoice.size.toString(), Color(0xFF3B82F6)),
        Triple("Filtered Total", "₹${data.totalamount.toInt()}", Color(0xFF10B981)),
        Triple("Session Paid", "₹${data.totalsessionpaidamount.toInt()}", Color(0xFF8B5CF6))
    )

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        items.forEach { (label, value, color) ->
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = MaterialTheme.shapes.medium,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
            ) {
                Column(modifier = Modifier.padding(if (isCompact) 12.dp else 16.dp)) {
                    Text(label.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color.Gray, letterSpacing = 0.5.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(value, fontSize = if (isCompact) 16.sp else 20.sp, fontWeight = FontWeight.Black, color = color)
                }
            }
        }
    }
}

@Composable
private fun FilterSearchSection(
    startDate: String,
    endDate: String,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onRefresh: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search student or invoice ID...", fontSize = 14.sp) },
                shape = MaterialTheme.shapes.small,
                singleLine = true,
                leadingIcon = { Icon(FontAwesomeIcons.Solid.Search, null, modifier = Modifier.size(16.dp), tint = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
            )
            
            Button(
                onClick = onRefresh,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.height(52.dp)
            ) {
                Icon(FontAwesomeIcons.Solid.Sync, null, modifier = Modifier.size(16.dp))
            }
        }

        Surface(
            color = Color.White,
            shape = MaterialTheme.shapes.small,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(FontAwesomeIcons.Solid.CalendarAlt, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                Text("Range:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Text("$startDate — $endDate", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            }
        }
    }
}

@Composable
fun InvoiceCard(invoice: InvoiceHistoryItem, isCompact: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Row(
            modifier = Modifier.padding(if (isCompact) 12.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape).background(Color(0xFFEEF2FF)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    invoice.studentName?.split(" ")?.mapNotNull { it.firstOrNull()?.toString() }?.joinToString("")?.take(2)?.uppercase() ?: "??",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6366F1)
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(invoice.studentName ?: "-", fontWeight = FontWeight.Bold, fontSize = if (isCompact) 14.sp else 16.sp, color = Color(0xFF1E293B))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("#${invoice.invoiceId}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D9488), modifier = Modifier.background(Color(0xFFF0FDFA), MaterialTheme.shapes.extraSmall).padding(horizontal = 4.dp))
                    Text(invoice.invoiceDate?.take(10) ?: "-", fontSize = 11.sp, color = Color.Gray)
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text("₹${invoice.amount.toInt()}", fontWeight = FontWeight.Black, fontSize = if (isCompact) 15.sp else 18.sp, color = Color(0xFF1E293B))
                Surface(
                    color = getPaymentColor(invoice.paymentMethod),
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    Text(
                        invoice.paymentMethod?.uppercase() ?: "CASH",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = Color.White
                    )
                }
            }
        }
    }
}

private fun getPaymentColor(method: String?): Color {
    return when (method?.lowercase()) {
        "cash" -> Color(0xFF10B981)
        "online" -> Color(0xFF6366F1)
        "cheque" -> Color(0xFFF59E0B)
        else -> Color(0xFF64748B)
    }
}

@Composable
private fun EmptyInvoicesView() {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(FontAwesomeIcons.Solid.Receipt, null, modifier = Modifier.size(64.dp), tint = Color(0xFFCBD5E1))
        Spacer(modifier = Modifier.height(16.dp))
        Text("No invoices found", fontWeight = FontWeight.Bold, color = Color.Gray)
    }
}

@Composable
private fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(FontAwesomeIcons.Solid.ExclamationCircle, null, modifier = Modifier.size(48.dp), tint = Color.Red.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(16.dp))
        Text(message, color = Color.Red, fontSize = 14.sp)
        TextButton(onClick = onRetry) { Text("Retry") }
    }
}
