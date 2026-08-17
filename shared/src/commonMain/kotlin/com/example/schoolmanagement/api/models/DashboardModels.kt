package com.example.schoolmanagement.api.models

import kotlinx.serialization.Serializable

@Serializable
data class DashboardStats(
    val totalStudents: Int = 0,
    val activeStudents: Int = 0,
    val totalTeachers: Int = 0,
    val attendancePercentage: Int = 0,
    val totalFees: Double = 0.0,
    val todayCollection: Double = 0.0,
    val dueFees: Double = 0.0,
    val totalEnrollments: Int = 0,
    val maleStudents: Int = 0,
    val femaleStudents: Int = 0
)

@Serializable
data class SchoolEvent(
    val eventid: Int,
    val eventname: String,
    val eventdate: String,
    val venue: String? = null,
    val color: String? = null
)

@Serializable
data class Notice(
    val id: Int,
    val title: String,
    val description: String? = null,
    val data: String, // date
    val tag: String? = null
)

@Serializable
data class EnrollmentChartItem(
    val name: String,
    val count: Int
)

@Serializable
data class AttendanceTrendItem(
    val day: String,
    val present: Int,
    val absent: Int,
    val total: Int
)

@Serializable
data class DashboardData(
    val stats: DashboardStats,
    val upcomingEvents: List<SchoolEvent> = emptyList(),
    val recentNotices: List<Notice> = emptyList(),
    val enrollmentByClass: List<EnrollmentChartItem> = emptyList(),
    val attendanceTrend: List<AttendanceTrendItem> = emptyList()
)
