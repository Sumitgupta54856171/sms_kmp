package com.example.schoolmanagement.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.schoolmanagement.presentation.components.Sidebar
import kotlinx.coroutines.launch

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
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isCompact = maxWidth < 600.dp
        
        if (isCompact) {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        drawerContainerColor = Color(0xFFFAFBFC),
                        drawerShape = MaterialTheme.shapes.large,
                        modifier = Modifier.width(280.dp)
                    ) {
                        Sidebar(
                            currentRoute = currentRoute,
                            onNavigate = {
                                onNavigate(it)
                                scope.launch { drawerState.close() }
                            },
                            userName = userName,
                            userRole = userRole,
                            onLogout = onLogout,
                            sessionViewModel = sessionViewModel
                        )
                    }
                }
            ) {
                ScaffoldContent(
                    currentRoute = currentRoute,
                    onToggleSidebar = { scope.launch { drawerState.open() } },
                    isSidebarVisible = false,
                    isCompact = true,
                    content = content
                )
            }
        } else {
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

                ScaffoldContent(
                    currentRoute = currentRoute,
                    onToggleSidebar = onToggleSidebar,
                    isSidebarVisible = isSidebarVisible,
                    isCompact = false,
                    modifier = Modifier.weight(1f),
                    content = content
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScaffoldContent(
    currentRoute: String,
    onToggleSidebar: () -> Unit,
    isSidebarVisible: Boolean,
    isCompact: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (currentRoute == "dashboard") "Dashboard" else currentRoute.split("/").last().capitalize(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onToggleSidebar) {
                        val iconText = if (isCompact) "☰" else if (isSidebarVisible) "←" else "☰"
                        Text(iconText, style = MaterialTheme.typography.titleLarge)
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

// Extension to capitalize route names for the title
private fun String.capitalize() = this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
