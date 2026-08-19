package com.example.schoolmanagement.api.models

import kotlinx.serialization.Serializable

@Serializable
data class StudentCreateRequest(
    val name: String,
    val email: String,
    val class_no: String,
    val roll_no: String,
    val scholar_no: String,
    val sssmid: String,
    val aadhaar: String,
    val gender: String,
    val category: String,
    val dob: String,
    val phone: String,
    val father_name: String,
    val mother_name: String,
    val apaarId: String? = null,
    val penId: String? = null,
    val address: String? = null,
    val status: String = "active",
    val total_fees: String
)
