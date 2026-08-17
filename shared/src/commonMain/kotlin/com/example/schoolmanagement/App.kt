package com.example.schoolmanagement

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.schoolmanagement.api.KtorClient
import com.example.schoolmanagement.auth.AuthRepository
import com.example.schoolmanagement.auth.TokenManager
import com.example.schoolmanagement.presentation.MainScaffold
import com.example.schoolmanagement.presentation.auth.LoginScreen
import com.example.schoolmanagement.presentation.auth.LoginViewModel
import com.example.schoolmanagement.presentation.dashboard.DashboardScreen
import com.example.schoolmanagement.presentation.dashboard.DashboardViewModel
import com.example.schoolmanagement.api.DashboardRepository
import com.example.schoolmanagement.api.SessionRepository
import com.example.schoolmanagement.presentation.session.SessionViewModel
import androidx.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    val tokenManager = remember { TokenManager() }
    val ktorClient = remember { KtorClient(tokenManager) }
    val authRepository = remember { AuthRepository(ktorClient, tokenManager) }
    val loginViewModel = remember { LoginViewModel(authRepository) }
    val dashboardRepository = remember { DashboardRepository(ktorClient) }
    val dashboardViewModel = remember { DashboardViewModel(dashboardRepository) }
    val sessionRepository = remember { SessionRepository(ktorClient) }
    val sessionViewModel = remember { 
        SessionViewModel(sessionRepository, tokenManager) {
            dashboardViewModel.loadDashboardData()
        }
    }
    
    val navController = rememberNavController()

    MaterialTheme {
        NavHost(
            navController = navController,
            startDestination = if (authRepository.isLoggedIn()) "dashboard" else "login"
        ) {
            composable("login") {
                LoginScreen(
                    viewModel = loginViewModel,
                    onLoginSuccess = {
                        navController.navigate("dashboard") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }
            
            val authenticatedRoutes = listOf(
                "dashboard", "students", "teachers", "class", "subjects",
                "elective-subject", "timetable", "lesson-plans", "attendance",
                "attendance/summary", "timetable/exams", "grades", "fees",
                "fees/invoice-history", "fees/structure", "tc", "enrollment", "login-generate"
            )

            authenticatedRoutes.forEach { route ->
                composable(route) {
                    MainScaffold(
                        currentRoute = route,
                        onNavigate = { target ->
                            if (target != route) {
                                navController.navigate(target) {
                                    launchSingleTop = true
                                }
                            }
                        },
                        userName = tokenManager.getUserName() ?: "User",
                        userRole = tokenManager.getUserRole() ?: "Staff",
                        onLogout = {
                            authRepository.logout()
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        sessionViewModel = sessionViewModel
                    ) {
                        when (route) {
                            "dashboard" -> DashboardScreen(dashboardViewModel)
                            "students" -> Text("Students Management")
                            "teachers" -> Text("Teachers Management")
                            "class" -> Text("Classes & Sections")
                            "subjects" -> Text("Subjects Management")
                            "elective-subject" -> Text("Elective Subjects")
                            "timetable" -> Text("Timetable")
                            "lesson-plans" -> Text("Lesson Plans")
                            "attendance" -> Text("Attendance")
                            "attendance/summary" -> Text("Attendance Summary")
                            "timetable/exams" -> Text("Examinations")
                            "grades" -> Text("Grades Management")
                            "fees" -> Text("Fee Management")
                            "fees/invoice-history" -> Text("Invoice History")
                            "fees/structure" -> Text("Fee Structure")
                            "tc" -> Text("Transfer Certificate")
                            "enrollment" -> Text("Enrollment")
                            "login-generate" -> Text("Generate Login")
                            else -> Text("Screen: $route")
                        }
                    }
                }
            }
        }
    }
}
