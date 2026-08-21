package com.example.schoolmanagement.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExamTimetableEntry(
    val testtimetableId: Int? = null,
    val id: Int? = null,
    val timetableName: String? = null,
    val examName: String? = null,
    @SerialName("exam_name") val examNameUnderscore: String? = null,
    val testName: String? = null,
    @SerialName("test_name") val testNameUnderscore: String? = null,
    val examType: String? = null, // "test" or "exam"
    @SerialName("exam_type") val examTypeUnderscore: String? = null,
    @SerialName("classNO") val classNO: String? = null,
    @SerialName("classNo") val classNoAlt: String? = null,
    @SerialName("gradeClass") val gradeClass: String? = null,
    @SerialName("grade_class") val gradeClassUnderscore: String? = null,
    val subject: String? = null,
    val date: String? = null,
    val day: String? = null,
    val startTime: String? = null,
    @SerialName("start_time") val startTimeUnderscore: String? = null,
    val endTime: String? = null,
    @SerialName("end_time") val endTimeUnderscore: String? = null,
    val maxMarks: Int? = null,
    @SerialName("totalMarks") val totalMarks: Int? = null,
    @SerialName("total_marks") val totalMarksUnderscore: Int? = null
) {
    val displayClass: String get() = classNO ?: classNoAlt ?: gradeClass ?: gradeClassUnderscore ?: ""
    val displayStartTime: String get() = startTime ?: startTimeUnderscore ?: ""
    val displayEndTime: String get() = endTime ?: endTimeUnderscore ?: ""
    val displayMaxMarks: Int? get() = maxMarks ?: totalMarks ?: totalMarksUnderscore
    val displayName: String get() = timetableName ?: examName ?: examNameUnderscore ?: testName ?: testNameUnderscore ?: ""
}

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
