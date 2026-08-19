package com.example.schoolmanagement.api.models

import kotlinx.serialization.Serializable

@Serializable
data class ExamTimetableEntry(
    val testtimetableId: Int? = null,
    val timetableName: String,
    val examType: String, // "test" or "exam"
    val classNO: String,
    val subject: String,
    val date: String,
    val day: String,
    val startTime: String,
    val endTime: String,
    val maxMarks: Int? = null
)

@Serializable
data class ExamGradePayload(
    val studentId: Int,
    val teacherId: Int,
    val subject: String,
    val classNo: String,
    val mark: Double,
    val examtimetableId: Int? = null
)

@Serializable
data class GradeMarkResponse(
    val studentId: Int,
    val studentName: String? = null,
    val mark: Double,
    val maxMarks: Int? = null
)
