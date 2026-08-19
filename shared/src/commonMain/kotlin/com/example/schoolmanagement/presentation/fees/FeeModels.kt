package com.example.schoolmanagement.presentation.fees

import com.example.schoolmanagement.api.models.StudentListItem

sealed class FeeListState {
    object Loading : FeeListState()
    data class Success(val students: List<StudentListItem>) : FeeListState()
    data class Error(val message: String) : FeeListState()
}

sealed class FeeDetailState {
    object Loading : FeeDetailState()
    data class Success(
        val annualFee: Double,
        val totalPaid: Double,
        val totalDue: Double,
        val discount: Double
    ) : FeeDetailState()
    data class Error(val message: String) : FeeDetailState()
}
