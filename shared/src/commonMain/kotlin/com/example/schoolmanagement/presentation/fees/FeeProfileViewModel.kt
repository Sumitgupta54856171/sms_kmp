package com.example.schoolmanagement.presentation.fees

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schoolmanagement.api.FeeRepository
import com.example.schoolmanagement.api.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FeeProfileViewModel(private val repository: FeeRepository) : ViewModel() {
    private val _detailState = MutableStateFlow<FeeDetailState>(FeeDetailState.Loading)
    val detailState: StateFlow<FeeDetailState> = _detailState.asStateFlow()

    private val _sessions = MutableStateFlow<List<StudentSession>>(emptyList())
    val sessions: StateFlow<List<StudentSession>> = _sessions.asStateFlow()

    private val _selectedEnrollmentId = MutableStateFlow<Int?>(null)
    val selectedEnrollmentId: StateFlow<Int?> = _selectedEnrollmentId.asStateFlow()

    private val _payments = MutableStateFlow<List<InvoiceData>>(emptyList())
    val payments: StateFlow<List<InvoiceData>> = _payments.asStateFlow()

    private val _sessionHistory = MutableStateFlow<List<SessionWiseHistory>>(emptyList())
    val sessionHistory: StateFlow<List<SessionWiseHistory>> = _sessionHistory.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    fun loadStudentFeeProfile(studentId: Int) {
        viewModelScope.launch {
            // Reset state for new student to ensure data isolation
            _detailState.value = FeeDetailState.Loading
            _sessions.value = emptyList()
            _selectedEnrollmentId.value = null
            _payments.value = emptyList()
            _sessionHistory.value = emptyList()

            repository.fetchStudentFeeDetails(studentId)
                .onSuccess { details ->
                    _detailState.value = FeeDetailState.Success(
                        annualFee = details.totalAnnualFee,
                        totalPaid = details.totalPaid,
                        totalDue = details.totaldue,
                        discount = details.discount
                    )
                }
                .onFailure { error ->
                    _detailState.value = FeeDetailState.Error(error.message ?: "Failed to load fee details")
                }

            repository.fetchStudentSessions(studentId)
                .onSuccess { sessions ->
                    _sessions.value = sessions
                    if (sessions.isNotEmpty() && _selectedEnrollmentId.value == null) {
                        setSelectedEnrollmentId(sessions[0].enrollementNo)
                    }
                }

            repository.fetchSessionWiseHistory(studentId)
                .onSuccess { history ->
                    _sessionHistory.value = history
                }
        }
    }

    fun setSelectedEnrollmentId(enrollmentId: Int) {
        _selectedEnrollmentId.value = enrollmentId
        loadPaymentHistory(enrollmentId)
    }

    private fun loadPaymentHistory(enrollmentId: Int) {
        viewModelScope.launch {
            repository.fetchInvoicesByEnrollmentId(enrollmentId)
                .onSuccess { invoices ->
                    _payments.value = invoices
                }
        }
    }

    fun recordPayment(payload: InvoicePayload) {
        viewModelScope.launch {
            _isSaving.value = true
            repository.createInvoice(payload)
                .onSuccess {
                    loadStudentFeeProfile(payload.studentId)
                    _selectedEnrollmentId.value?.let { loadPaymentHistory(it) }
                }
                .onFailure {
                    // Handle error
                }
            _isSaving.value = false
        }
    }

    fun applyStudentDiscount(studentId: Int, amount: Double) {
        viewModelScope.launch {
            repository.applyDiscount(studentId, amount)
                .onSuccess {
                    loadStudentFeeProfile(studentId)
                }
        }
    }
}
