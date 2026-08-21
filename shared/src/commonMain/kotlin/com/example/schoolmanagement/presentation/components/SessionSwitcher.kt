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
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.CalendarAlt
import compose.icons.fontawesomeicons.solid.ChevronDown

@Composable
fun SessionSwitcher(viewModel: SessionViewModel) {
    val state by viewModel.state.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    val currentSessionName = when (val s = state) {
        is SessionState.Success -> s.currentSession?.sessionName ?: "No Session"
        else -> "Loading..."
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .clickable { expanded = true },
            color = Color(0xFFF1F5F9),
            shape = MaterialTheme.shapes.medium,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(Color(0xFF0D9488).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = FontAwesomeIcons.Solid.CalendarAlt,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF0D9488)
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text("Active Session", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(currentSessionName, fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E293B))
                }
                
                Icon(
                    imageVector = FontAwesomeIcons.Solid.ChevronDown,
                    contentDescription = null,
                    modifier = Modifier.size(10.dp),
                    tint = Color.Gray
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White).widthIn(min = 220.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            when (val s = state) {
                is SessionState.Success -> {
                    s.sessions.forEach { session ->
                        val isSelected = session.sessionId == s.currentSession?.sessionId
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    session.sessionName,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color(0xFF0D9488) else Color(0xFF334155)
                                ) 
                            },
                            onClick = {
                                viewModel.switchSession(session.sessionId)
                                expanded = false
                            },
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
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
