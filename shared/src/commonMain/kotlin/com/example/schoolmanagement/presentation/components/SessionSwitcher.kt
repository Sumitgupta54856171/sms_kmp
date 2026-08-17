package com.example.schoolmanagement.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.schoolmanagement.presentation.session.SessionViewModel
import com.example.schoolmanagement.presentation.session.SessionState

@Composable
fun SessionSwitcher(viewModel: SessionViewModel) {
    val state by viewModel.state.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF1F5F9))
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Calendar Icon Placeholder
                Box(modifier = Modifier.size(16.dp).background(Color(0xFF0D9488), RoundedCornerShape(4.dp)))
                
                Column {
                    Text("Active Session", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                    val sessionName = when (val s = state) {
                        is SessionState.Success -> s.currentSession?.sessionName ?: "No Session"
                        else -> "Loading..."
                    }
                    Text(sessionName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                }
                
                Spacer(modifier = Modifier.weight(1f))
                Text("▼", fontSize = 10.sp, color = Color.Gray)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White)
            ) {
                when (val s = state) {
                    is SessionState.Success -> {
                        s.sessions.forEach { session ->
                            DropdownMenuItem(
                                text = { Text(session.sessionName) },
                                onClick = {
                                    viewModel.switchSession(session.sessionId)
                                    expanded = false
                                }
                            )
                        }
                    }
                    is SessionState.Loading -> {
                        DropdownMenuItem(text = { Text("Loading...") }, onClick = {})
                    }
                    is SessionState.Error -> {
                        DropdownMenuItem(text = { Text("Error loading sessions") }, onClick = {})
                    }
                }
            }
        }
    }
}
