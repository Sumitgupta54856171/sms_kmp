package com.example.schoolmanagement.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.schoolmanagement.presentation.components.Sidebar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    userName: String,
    userRole: String,
    onLogout: () -> Unit,
    isSidebarVisible: Boolean,
    onToggleSidebar: () -> Unit,
    sessionViewModel: com.example.schoolmanagement.presentation.session.SessionViewModel,
    content: @Composable () -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = isSidebarVisible,
            enter = expandHorizontally(),
            exit = shrinkHorizontally()
        ) {
            Sidebar(
                currentRoute = currentRoute,
                onNavigate = onNavigate,
                userName = userName,
                userRole = userRole,
                onLogout = onLogout,
                sessionViewModel = sessionViewModel
            )
        }
        
        Scaffold(
            modifier = Modifier.weight(1f),
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            text = if (currentRoute == "dashboard") "Dashboard" else currentRoute.capitalize(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = onToggleSidebar) {
                            Text(if (isSidebarVisible) "←" else "☰", style = MaterialTheme.typography.titleLarge)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                    )
                )
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                content()
            }
        }
    }
}

// Extension to capitalize route names for the title
private fun String.capitalize() = this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
