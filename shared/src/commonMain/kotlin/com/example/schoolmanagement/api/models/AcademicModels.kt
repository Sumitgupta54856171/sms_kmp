package com.example.schoolmanagement.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PeriodEntry(
    val id: Int? = null,
    val gradeClass: String,
    val subjectName: String,
    val periodNumber: Int,
    @SerialName("teacher_id") val teacher_id_direct: Int? = null,
    @SerialName("teacherId") val teacherIdAlt: Int? = null,
    val teacher: TeacherMini? = null,
    val teacher_name: String? = null,
    val session_id: Int? = null
) {
    val teacher_id: Int? get() = teacher_id_direct ?: teacherIdAlt ?: teacher?.id
    val displayTeacherName: String? get() = teacher?.fullName ?: teacher?.name ?: teacher_name
}

@Serializable
data class TeacherMini(
    val id: Int,
    val fullName: String? = null,
    val name: String? = null
)

@Serializable
data class ClassTeacherAssignment(
    val id: Int? = null,
    val gradeClass: String? = null,
    val class_no: String? = null,
    val section: String? = null,
    @SerialName("teacher_id") val teacher_id_direct: Int? = null,
    @SerialName("teacherId") val teacherIdAlt: Int? = null,
    val teacher: TeacherMini? = null,
    val teacher_name: String? = null
) {
    val teacher_id: Int? get() = teacher_id_direct ?: teacherIdAlt ?: teacher?.id
    val displayTeacherName: String? get() = teacher?.fullName ?: teacher?.name ?: teacher_name
}
