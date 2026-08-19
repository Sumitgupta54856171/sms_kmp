package com.example.schoolmanagement.api.models

import kotlinx.serialization.Serializable

@Serializable
data class PeriodEntry(
    val id: Int? = null,
    val gradeClass: String,
    val subjectName: String,
    val periodNumber: Int,
    val teacher_id: Int? = null,
    val teacher_name: String? = null,
    val session_id: Int? = null
)

@Serializable
data class ClassTeacherAssignment(
    val id: Int? = null,
    val gradeClass: String? = null,
    val class_no: String? = null,
    val section: String? = null,
    val teacher_id: Int? = null,
    val teacher_name: String? = null
)
