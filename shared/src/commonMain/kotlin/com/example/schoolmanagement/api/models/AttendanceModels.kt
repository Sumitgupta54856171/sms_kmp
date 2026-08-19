package com.example.schoolmanagement.api.models

import kotlinx.serialization.Serializable

@Serializable
data class AttendanceRecord(
    val attendanceId: Int? = null,
    val attendanceDate: String,
    val status: String, // "present", "absent", "holiday"
    val studentId: Int,
    val studentName: String? = null,
    val grade: String? = null,
    val rollNumber: String? = null,
    val scholarNo: String? = null,
    val gender: String? = null
)

@Serializable
data class AttendancePayload(
    val attendanceDate: String,
    val studentId: Int,
    val status: String,
    val grade: String? = null
)
