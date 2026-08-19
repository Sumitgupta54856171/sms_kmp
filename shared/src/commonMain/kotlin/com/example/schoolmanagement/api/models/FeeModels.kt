package com.example.schoolmanagement.api.models

import kotlinx.serialization.Serializable

@Serializable
data class FeeHead(
    val name: String,
    val code: String,
    val amount: Double,
    val isOptional: Boolean = false
)

@Serializable
data class FeeStructure(
    val classRange: String,
    val classes: List<String>,
    val annualTotal: Double,
    val heads: List<FeeHead>
)

@Serializable
data class StudentFeeDetails(
    val totalAnnualFee: Double = 0.0,
    val totalPaid: Double = 0.0,
    val totaldue: Double = 0.0,
    val discount: Double = 0.0
)

@Serializable
data class InvoiceData(
    val invoiceId: Int,
    val studentName: String? = null,
    val invoiceDate: String? = null,
    val paymentMethod: String? = null,
    val amount: Double = 0.0
)

@Serializable
data class StudentSession(
    val enrollementNo: Int,
    val sessionName: String
)

@Serializable
data class SessionWiseHistory(
    val sessionName: String,
    val totalfees: Double = 0.0,
    val totalpaid: Double = 0.0,
    val totaldue: Double = 0.0,
    val paymentsNo: Int = 0
)

@Serializable
data class InvoicePayload(
    val enrollmentId: Int,
    val paymentMethod: String,
    val studentId: Int,
    val scholarNo: String,
    val classNo: String,
    val rollNo: String,
    val sessionId: Int,
    val amount: Double,
    val paymentType: String,
    val remarks: String? = null
)

@Serializable
data class InvoiceResponse(
    val invoiceId: Int,
    val invoiceNo: String? = null,
    val amount: Double = 0.0,
    val paymentMethod: String? = null,
    val paymentType: String? = null,
    val studentId: Int? = null,
    val scholarNo: String? = null,
    val classNo: String? = null,
    val rollNo: String? = null,
    val sessionId: Int? = null,
    val remarks: String? = null,
    val createdAt: String? = null
)

@Serializable
data class InvoiceHistoryItem(
    val invoiceId: Int,
    val studentName: String? = null,
    val invoiceDate: String? = null,
    val paymentMethod: String? = null,
    val amount: Double = 0.0
)

@Serializable
data class InvoiceHistoryResponse(
    val invoice: List<InvoiceHistoryItem> = emptyList(),
    val totalamount: Double = 0.0,
    val totalsessionpaidamount: Double = 0.0,
    val totalInvoicesAmount: Double = 0.0
)

@Serializable
data class FeeStudentResponse(
    val id: Int? = null,
    val studentId: Int? = null,
    val roll_no: String? = null,
    val total_fees: Double? = null,
    val enrollmentId: Int? = null,
    val student: StudentListItem? = null
)
