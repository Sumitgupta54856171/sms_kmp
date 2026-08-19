package com.example.schoolmanagement

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.schoolmanagement.api.*
import com.example.schoolmanagement.presentation.session.SessionViewModel
import com.example.schoolmanagement.presentation.students.*
import com.example.schoolmanagement.presentation.fees.*
import com.example.schoolmanagement.presentation.teachers.*
import com.example.schoolmanagement.presentation.academics.*
import com.example.schoolmanagement.presentation.attendance.*
import com.example.schoolmanagement.presentation.assessment.*
import com.example.schoolmanagement.presentation.operations.*
import androidx.compose.ui.tooling.preview.Preview
import com.example.schoolmanagement.api.models.StudentListItem
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

@Composable
@Preview
fun App() {
    val tokenManager = remember { TokenManager() }
    val ktorClient = remember { KtorClient(tokenManager) }
    
    // Repositories
    val authRepository = remember { AuthRepository(ktorClient, tokenManager) }
    val dashboardRepository = remember { DashboardRepository(ktorClient) }
    val studentRepository = remember { StudentRepository(ktorClient) }
    val feeRepository = remember { FeeRepository(ktorClient) }
    val teacherRepository = remember { TeacherRepository(ktorClient) }
    val academicRepository = remember { AcademicRepository(ktorClient) }
    val attendanceRepository = remember { AttendanceRepository(ktorClient) }
    val assessmentRepository = remember { AssessmentRepository(ktorClient) }
    val operationsRepository = remember { OperationsRepository(ktorClient) }
    val sessionRepository = remember { SessionRepository(ktorClient) }

    // ViewModels
    val loginViewModel = remember { LoginViewModel(authRepository) }
    val dashboardViewModel = remember { DashboardViewModel(dashboardRepository) }
    val studentViewModel = remember { StudentViewModel(studentRepository) }
    val studentProfileViewModel = remember { StudentProfileViewModel(studentRepository) }
    val feeViewModel = remember { FeeViewModel(feeRepository) }
    val feeProfileViewModel = remember { FeeProfileViewModel(feeRepository) }
    val teacherViewModel = remember { TeacherViewModel(teacherRepository) }
    val timetableViewModel = remember { TimetableViewModel(academicRepository) }
    val attendanceViewModel = remember { AttendanceViewModel(attendanceRepository, studentRepository) }
    val attendanceSummaryViewModel = remember { AttendanceSummaryViewModel(attendanceRepository) }
    val examViewModel = remember { ExamViewModel(assessmentRepository) }
    val gradeViewModel = remember { GradeViewModel(assessmentRepository, studentRepository, teacherRepository) }
    val tcViewModel = remember { TCViewModel(studentRepository) }
    val enrollmentViewModel = remember { EnrollmentViewModel(operationsRepository, studentRepository) }
    val invoiceHistoryViewModel = remember { InvoiceHistoryViewModel(operationsRepository) }
    val loginGenerateViewModel = remember { LoginGenerateViewModel(operationsRepository, studentRepository) }
    
    val sessionViewModel = remember { 
        SessionViewModel(sessionRepository, tokenManager) {
            dashboardViewModel.loadDashboardData()
        }
    }
    
    val navController = rememberNavController()
    var isSidebarVisible by remember { mutableStateOf(true) }

    // State for temporary student data during navigation
    var selectedFeeStudent by remember { mutableStateOf<StudentListItem?>(null) }
    var shouldOpenPayDialog by remember { mutableStateOf(false) }
    var selectedProfileStudentId by remember { mutableStateOf<Int?>(null) }

    MaterialTheme {
        NavHost(
            navController = navController,
            startDestination = if (authRepository.isLoggedIn()) "authenticated" else "login"
        ) {
            composable("login") {
                LoginScreen(
                    viewModel = loginViewModel,
                    onLoginSuccess = {
                        navController.navigate("authenticated") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }
            
            composable("authenticated") {
                LaunchedEffect(Unit) {
                    navController.navigate("dashboard") {
                        popUpTo("authenticated") { inclusive = true }
                    }
                }
            }

            val authenticatedRoutes = listOf(
                "dashboard", "students", "teachers", "class", "subjects",
                "elective-subject", "timetable", "lesson-plans", "attendance",
                "attendance/summary", "timetable/exams", "grades", "fees",
                "fees/invoice-history", "fees/structure", "tc", "enrollment", "login-generate",
                "fees-profile", "student-profile"
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
                        isSidebarVisible = isSidebarVisible,
                        onToggleSidebar = { isSidebarVisible = !isSidebarVisible },
                        sessionViewModel = sessionViewModel
                    ) {
                        when (route) {
                            "dashboard" -> DashboardScreen(dashboardViewModel, onNavigate = { target ->
                                if (target != route) {
                                    navController.navigate(target) {
                                        launchSingleTop = true
                                    }
                                }
                            })
                            "students" -> StudentScreen(
                                viewModel = studentViewModel,
                                onStudentClick = { student ->
                                    selectedProfileStudentId = student.id ?: student.studentId
                                    studentProfileViewModel.loadStudentProfile(selectedProfileStudentId!!)
                                    navController.navigate("student-profile")
                                }
                            )
                            "student-profile" -> {
                                val profileId = selectedProfileStudentId
                                if (profileId != null) {
                                    StudentProfileScreen(
                                        studentId = profileId,
                                        viewModel = studentProfileViewModel,
                                        onBack = { navController.popBackStack() }
                                    )
                                } else {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("No student selected")
                                    }
                                }
                            }
                            "teachers" -> TeacherScreen(teacherViewModel)
                            "class" -> ClasspageScreen(onClassClick = { /* Handle class click */ })
                            "timetable" -> TimetableScreen(timetableViewModel)
                            "attendance" -> AttendanceScreen(attendanceViewModel)
                            "attendance/summary" -> AttendanceSummaryScreen(attendanceSummaryViewModel)
                            "timetable/exams" -> ExamScreen(examViewModel)
                            "grades" -> GradeScreen(gradeViewModel)
                            "tc" -> TCScreen(tcViewModel)
                            "enrollment" -> EnrollmentScreen(enrollmentViewModel)
                            "fees/invoice-history" -> InvoiceHistoryScreen(invoiceHistoryViewModel)
                            "login-generate" -> LoginGenerateScreen(loginGenerateViewModel)
                            "fees" -> FeeScreen(
                                viewModel = feeViewModel,
                                onViewFees = { student ->
                                    selectedFeeStudent = student
                                    shouldOpenPayDialog = false
                                    navController.navigate("fees-profile")
                                },
                                onPayFees = { student ->
                                    selectedFeeStudent = student
                                    shouldOpenPayDialog = true
                                    navController.navigate("fees-profile")
                                }
                            )
                            "fees-profile" -> {
                                selectedFeeStudent?.let { student ->
                                    FeeProfileScreen(
                                        student = student,
                                        viewModel = feeProfileViewModel,
                                        initialShowPayDialog = shouldOpenPayDialog,
                                        onBack = { navController.popBackStack() }
                                    )
                                } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("No student selected")
                                }
                            }
                            else -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Screen: $route", style = MaterialTheme.typography.headlineMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
