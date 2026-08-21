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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.savedstate.read
import com.example.schoolmanagement.api.KtorClient
import com.example.schoolmanagement.auth.AuthRepository
import com.example.schoolmanagement.auth.TokenManager
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

import com.example.schoolmanagement.util.ToastManager
import com.example.schoolmanagement.util.ToastType
import com.example.schoolmanagement.presentation.components.ToastHost

@Composable
@Preview
fun App() {
    val toastManager = remember { ToastManager() }
    val tokenManager = remember { TokenManager() }
    val ktorClient = remember { KtorClient(tokenManager, toastManager) }
    
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

    // Shared state for temporary student data during navigation
    var selectedFeeStudent by remember { mutableStateOf<StudentListItem?>(null) }
    var shouldOpenPayDialog by remember { mutableStateOf(false) }
    var selectedProfileStudentId by remember { mutableStateOf<Int?>(null) }
    var isSidebarVisible by remember { mutableStateOf(true) }

    val navController = rememberNavController()

    // Global session ViewModel should stay at root
    val sessionViewModel = remember { 
        SessionViewModel(sessionRepository, tokenManager) {
            // Callback for session change
        }
    }

    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            val currentBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = currentBackStackEntry?.destination?.route ?: "login"
            
            val authenticatedRoutes = listOf(
                "dashboard", "students", "teachers", "class", "class/{classNo}", "subjects",
                "elective-subject", "timetable", "lesson-plans", "attendance",
                "attendance/summary", "timetable/exams", "grades", "fees",
                "fees/invoice-history", "fees/structure", "tc", "enrollment", "login-generate",
                "fees-profile", "student-profile"
            )
            
            val isAuthRoute = authenticatedRoutes.contains(currentRoute)

            if (isAuthRoute) {
                com.example.schoolmanagement.presentation.MainScaffold(
                    currentRoute = currentRoute,
                    onNavigate = { target: String ->
                        if (target != currentRoute) {
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
                    AuthenticatedNavHost(
                        navController = navController,
                        dashboardRepository = dashboardRepository,
                        studentRepository = studentRepository,
                        teacherRepository = teacherRepository,
                        academicRepository = academicRepository,
                        attendanceRepository = attendanceRepository,
                        assessmentRepository = assessmentRepository,
                        operationsRepository = operationsRepository,
                        feeRepository = feeRepository,
                        tokenManager = tokenManager,
                        selectedProfileStudentId = selectedProfileStudentId,
                        onStudentSelected = { selectedProfileStudentId = it },
                        selectedFeeStudent = selectedFeeStudent,
                        onFeeStudentSelected = { selectedFeeStudent = it },
                        shouldOpenPayDialog = shouldOpenPayDialog,
                        onPayDialogChange = { shouldOpenPayDialog = it }
                    )
                }
            } else {
                NavHost(
                    navController = navController,
                    startDestination = if (authRepository.isLoggedIn()) "dashboard" else "login"
                ) {
                    composable("login") {
                        val loginViewModel = remember { LoginViewModel(authRepository) }
                        LoginScreen(
                            viewModel = loginViewModel,
                            onLoginSuccess = {
                                navController.navigate("dashboard") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }
                    // Placeholder to allow navigation to dashboard which will then trigger the AuthenticatedNavHost
                    composable("dashboard") { Box(Modifier.fillMaxSize()) }
                }
            }

            // Global Toasts
            ToastHost(toastManager)
        }
    }
}

@Composable
fun AuthenticatedNavHost(
    navController: androidx.navigation.NavHostController,
    dashboardRepository: DashboardRepository,
    studentRepository: StudentRepository,
    teacherRepository: TeacherRepository,
    academicRepository: AcademicRepository,
    attendanceRepository: AttendanceRepository,
    assessmentRepository: AssessmentRepository,
    operationsRepository: OperationsRepository,
    feeRepository: FeeRepository,
    tokenManager: TokenManager,
    selectedProfileStudentId: Int?,
    onStudentSelected: (Int?) -> Unit,
    selectedFeeStudent: StudentListItem?,
    onFeeStudentSelected: (StudentListItem?) -> Unit,
    shouldOpenPayDialog: Boolean,
    onPayDialogChange: (Boolean) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = "dashboard",
        modifier = Modifier.fillMaxSize()
    ) {
        composable("dashboard") {
            val viewModel = remember { DashboardViewModel(dashboardRepository) }
            DashboardScreen(viewModel, onNavigate = { target ->
                navController.navigate(target) { launchSingleTop = true }
            })
        }
        composable("students") {
            val viewModel = remember { StudentViewModel(studentRepository) }
            StudentScreen(
                viewModel = viewModel,
                onStudentClick = { student ->
                    onStudentSelected(student.id ?: student.studentId)
                    navController.navigate("student-profile")
                }
            )
        }
        composable("student-profile") {
            val profileId = selectedProfileStudentId
            if (profileId != null) {
                val viewModel = remember { StudentProfileViewModel(studentRepository) }
                StudentProfileScreen(
                    studentId = profileId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
        composable("teachers") { 
            val viewModel = remember { TeacherViewModel(teacherRepository) }
            TeacherScreen(viewModel) 
        }
        composable("class") { 
            ClasspageScreen(onClassClick = { classNo ->
                navController.navigate("class/$classNo")
            }) 
        }
        composable(
            route = "class/{classNo}",
            arguments = listOf(navArgument("classNo") { type = NavType.StringType })
        ) { entry ->
            val classNo = entry.arguments?.read { getString("classNo") } ?: ""
            val viewModel = remember(classNo) { ClassStudentViewModel(studentRepository, classNo) }
            ClassStudentListScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable("timetable") { 
            val viewModel = remember { TimetableViewModel(academicRepository, teacherRepository, tokenManager) }
            TimetableScreen(viewModel) 
        }
        composable("attendance") { 
            val viewModel = remember { AttendanceViewModel(attendanceRepository, studentRepository) }
            AttendanceScreen(viewModel) 
        }
        composable("attendance/summary") { 
            val viewModel = remember { AttendanceSummaryViewModel(attendanceRepository) }
            AttendanceSummaryScreen(viewModel) 
        }
        composable("timetable/exams") { 
            val viewModel = remember { ExamViewModel(assessmentRepository) }
            ExamScreen(viewModel) 
        }
        composable("grades") { 
            val viewModel = remember { GradeViewModel(assessmentRepository, studentRepository, teacherRepository) }
            GradeScreen(viewModel) 
        }
        composable("tc") { 
            val viewModel = remember { TCViewModel(studentRepository) }
            TCScreen(viewModel) 
        }
        composable("enrollment") { 
            val viewModel = remember { EnrollmentViewModel(operationsRepository, studentRepository) }
            EnrollmentScreen(viewModel) 
        }
        composable("fees/invoice-history") { 
            val viewModel = remember { InvoiceHistoryViewModel(operationsRepository) }
            InvoiceHistoryScreen(viewModel) 
        }
        composable("login-generate") { 
            val viewModel = remember { LoginGenerateViewModel(operationsRepository, studentRepository) }
            LoginGenerateScreen(viewModel) 
        }
        composable("fees") {
            val viewModel = remember { FeeViewModel(feeRepository) }
            FeeScreen(
                viewModel = viewModel,
                onViewFees = { student ->
                    onFeeStudentSelected(student)
                    onPayDialogChange(false)
                    navController.navigate("fees-profile")
                },
                onPayFees = { student ->
                    onFeeStudentSelected(student)
                    onPayDialogChange(true)
                    navController.navigate("fees-profile")
                }
            )
        }
        composable("fees-profile") {
            selectedFeeStudent?.let { student ->
                val viewModel = remember { FeeProfileViewModel(feeRepository) }
                FeeProfileScreen(
                    student = student,
                    viewModel = viewModel,
                    initialShowPayDialog = shouldOpenPayDialog,
                    onBack = { navController.popBackStack() }
                )
            }
        }
        composable("login") { /* No-op */ }
    }
}
