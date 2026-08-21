package com.example.schoolmanagement.api.models

import kotlinx.serialization.Serializable

@Serializable
data class TeacherData(
    val fullName: String,
    val email: String,
    val employee_id: String,
    val phone: String? = null,
    val subject_specialization: String? = null,
    val gender: String? = null,
    val aadhaar_id: String? = null,
    val sssmid: String? = null,
    val status: String? = "active",
    val education: String? = null,
    val password: String? = null
)

@Serializable
data class TeacherResponse(
    val id: Int,
    val fullName: String,
    val email: String,
    val employee_id: String,
    val phone: String? = null,
    val subject_specialization: String? = null,
    val gender: String? = null,
    val aadhaar_id: String? = null,
    val sssmid: String? = null,
    val status: String,
    val education: String? = null,
    val created_at: String? = null
)

@Serializable
data class ChangeRoleRequest(
    val email: String,
    val role: String // "ADMIN", "ACCOUNTANT", "TEACHER"
)
