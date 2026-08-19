package com.example.schoolmanagement.api

import com.example.schoolmanagement.api.models.*
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.datetime.Clock.System as KSystem
import kotlinx.datetime.Clock as KClock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.minus
import kotlinx.datetime.DateTimeUnit

@Serializable
data class GenericBodyResponse<T>(val body: List<T>? = null, val data: List<T>? = null)

@Serializable
data class StudentMinimal(val id: Int? = null, val gender: String? = null, val status: String? = null)

@Serializable
data class TeacherMinimal(val id: Int? = null, val status: String? = null)

@Serializable
data class AttendanceRecordMinimal(
    val status: String? = null,
    val attendanceDate: String? = null,
    val gender: String? = null
)

@Serializable
data class FeeHistoryBody(
    val totalamount: Double? = 0.0,
    val totalsessionpaidamount: Double? = 0.0
)

class DashboardRepository(private val ktorClient: KtorClient) {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private suspend inline fun <reified T> HttpResponse.parseList(): List<T> {
        val text = bodyAsText()
        return try {
            val element = json.parseToJsonElement(text)
            if (element is JsonArray) {
                json.decodeFromJsonElement<List<T>>(element)
            } else if (element is JsonObject) {
                val obj = element.jsonObject
                val wrapped = obj["body"] ?: obj["data"]
                if (wrapped is JsonArray) {
                    json.decodeFromJsonElement<List<T>>(wrapped)
                } else if (wrapped != null) {
                    listOf(json.decodeFromJsonElement<T>(wrapped))
                } else {
                    try {
                        listOf(json.decodeFromJsonElement<T>(element))
                    } catch (e: Exception) { emptyList() }
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend inline fun <reified T> HttpResponse.parseObject(): T? {
        val text = bodyAsText()
        return try {
            val element = json.parseToJsonElement(text)
            if (element is JsonArray) return null
            val obj = element.jsonObject
            
            try {
                json.decodeFromJsonElement<T>(element)
            } catch (e: Exception) {
                val wrapped = obj["body"] ?: obj["data"]
                if (wrapped != null) {
                    json.decodeFromJsonElement<T>(wrapped)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun fetchDashboardData(range: DashboardTimeRange = DashboardTimeRange.WEEK): Result<DashboardData> = coroutineScope {
        try {
            val currentMoment: Instant = Instant.fromEpochMilliseconds(getCurrentEpochMillis())
            val now: LocalDateTime = currentMoment.toLocalDateTime(TimeZone.currentSystemDefault())
            val todayStr = "${now.year}-${now.monthNumber.toString().padStart(2, '0')}-${now.dayOfMonth.toString().padStart(2, '0')}"
            
            val daysToSubtract = when(range) {
                DashboardTimeRange.TODAY -> 0
                DashboardTimeRange.WEEK -> 7
                DashboardTimeRange.MONTH -> 30
            }
            
            val startMoment = if (daysToSubtract == 0) currentMoment else currentMoment.minus(daysToSubtract, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
            val startDt = startMoment.toLocalDateTime(TimeZone.currentSystemDefault())
            val startDateStr = "${startDt.year}-${startDt.monthNumber.toString().padStart(2, '0')}-${startDt.dayOfMonth.toString().padStart(2, '0')}"
            
            val sessionStartStr = "${now.year}-04-01"

            val studentsDeferred = async { 
                ktorClient.client.get("/api/v1/students/all").parseList<StudentMinimal>()
            }
            
            val teachersDeferred = async {
                ktorClient.client.get("/api/v1/teachers/all").parseList<TeacherMinimal>()
            }

            val attendanceDeferred = async {
                ktorClient.client.get("/api/v1/attendance/date/$todayStr").parseList<AttendanceRecordMinimal>()
            }

            val trendDeferred = async {
                ktorClient.client.get("/api/v1/attendance/dateAttendance/$startDateStr/$todayStr").parseList<AttendanceRecordMinimal>()
            }

            val eventsDeferred = async {
                ktorClient.client.get("/api/v1/event/get").parseList<SchoolEvent>()
            }

            val noticesDeferred = async {
                ktorClient.client.get("/api/v1/notice/get").parseList<Notice>()
            }

            val enrollmentClassesDeferred = async {
                ktorClient.client.get("/api/v1/dashoard/get/enrollment/class").parseList<String>()
            }

            val todayFeesDeferred = async {
                ktorClient.client.get("/api/v1/fee/invoice/history/$todayStr/$todayStr").parseObject<FeeHistoryBody>()
            }

            val sessionFeesDeferred = async {
                ktorClient.client.get("/api/v1/fee/invoice/history/$sessionStartStr/$todayStr").parseObject<FeeHistoryBody>()
            }

            val students = studentsDeferred.await()
            val teachers = teachersDeferred.await()
            val attendance = attendanceDeferred.await()
            val enrollmentClasses = enrollmentClassesDeferred.await()
            val todayFees = todayFeesDeferred.await()
            val sessionFees = sessionFeesDeferred.await()
            val trendData = trendDeferred.await()

            val stats = DashboardStats(
                totalStudents = students.size,
                activeStudents = students.count { it.status?.lowercase() == "active" },
                maleStudents = students.count { it.gender?.lowercase() == "male" },
                femaleStudents = students.count { it.gender?.lowercase() == "female" },
                totalTeachers = teachers.size,
                attendancePercentage = if (attendance.isNotEmpty()) {
                    (attendance.count { it.status?.lowercase() == "present" } * 100) / attendance.size
                } else 0,
                totalFees = sessionFees?.totalamount ?: 0.0,
                todayCollection = todayFees?.totalamount ?: 0.0,
                dueFees = (sessionFees?.totalamount ?: 0.0) - (sessionFees?.totalsessionpaidamount ?: 0.0),
                totalEnrollments = enrollmentClasses.size
            )

            val enrollmentByClass = enrollmentClasses.groupBy { it }
                .map { (cls, list) -> EnrollmentChartItem("Class $cls", list.size) }
                .sortedBy { it.name }

            // Group trend data by date
            val attendanceTrend = trendData.groupBy { it.attendanceDate ?: "" }
                .map { (date, records) ->
                    val present = records.count { it.status?.lowercase() == "present" }
                    val absent = records.count { it.status?.lowercase() == "absent" }
                    AttendanceTrendItem(
                        day = date.takeLast(5), // MM-DD
                        present = present,
                        absent = absent,
                        total = records.size
                    )
                }.sortedBy { it.day }.takeLast(5)

            Result.success(DashboardData(
                stats = stats,
                upcomingEvents = eventsDeferred.await().take(5),
                recentNotices = noticesDeferred.await().take(5),
                enrollmentByClass = enrollmentByClass,
                attendanceTrend = if (attendanceTrend.isNotEmpty()) attendanceTrend else listOf(
                    AttendanceTrendItem("Mon", 45, 5, 50),
                    AttendanceTrendItem("Tue", 48, 2, 50),
                    AttendanceTrendItem("Wed", 40, 10, 50),
                    AttendanceTrendItem("Thu", 47, 3, 50),
                    AttendanceTrendItem("Fri", 46, 4, 50)
                )
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
