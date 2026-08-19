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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp)
    ) {
        Text("Invoice History", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
        Text("Search and view all past fee collection records", fontSize = 14.sp, color = Color(0xFF64748B), modifier = Modifier.padding(bottom = 24.dp))

        // Date Filter
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Date Range", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text("$startDate to $endDate", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                
                IconButton(onClick = { viewModel.loadInvoices() }) {
                    Icon(FontAwesomeIcons.Solid.Sync, null, modifier = Modifier.size(18.dp), tint = Color(0xFF0D9488))
                }
            }
        }

        // List
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (val s = state) {
                is InvoiceHistoryState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF0D9488))
                is InvoiceHistoryState.Error -> Text(s.message, color = Color.Red, modifier = Modifier.align(Alignment.Center))
                is InvoiceHistoryState.Success -> {
                    val invoices = s.data.invoice
                    if (invoices.isEmpty()) {
                        Text("No invoices found for this range", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(invoices) { invoice ->
                                InvoiceRow(invoice)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InvoiceRow(invoice: InvoiceHistoryItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(Color(0xFFF1F5F9), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(FontAwesomeIcons.Solid.Receipt, null, modifier = Modifier.size(18.dp), tint = Color(0xFF64748B))
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(invoice.studentName ?: "-", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("Inv #${invoice.invoiceId} | ${invoice.invoiceDate?.take(10) ?: "-"}", fontSize = 12.sp, color = Color.Gray)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text("₹${invoice.amount.toInt()}", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color(0xFF10B981))
                Text(invoice.paymentMethod ?: "CASH", fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}
