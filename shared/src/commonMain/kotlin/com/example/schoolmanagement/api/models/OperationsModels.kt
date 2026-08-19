package com.example.schoolmanagement.api.models

import kotlinx.serialization.Serializable

@Serializable
data class EnrollmentRequest(
    val studentId: Int,
    val classNo: String,
    val rolNo: String,
    val Totalfees: Double
)

@Serializable
data class TCStudent(
    val id: Int,
    val name: String,
    val father_name: String? = null,
    val mother_name: String? = null,
    val sssmid: String? = null,
    val aadhaar: String? = null,
    val dob: String? = null,
    val scholar_no: String? = null
)

@Serializable
data class RegisterRolePayload(
    val username: String? = null,
    val email: String,
    val password: String,
    val role: String // "STUDENT" or "PARENT"
)
