package com.example.schoolmanagement.api

import com.example.schoolmanagement.api.models.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*

class AttendanceRepository(private val ktorClient: KtorClient) {

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

    suspend fun fetchAttendanceByDate(date: String): Result<List<AttendanceRecord>> {
        return try {
            val response = ktorClient.client.get("/api/v1/attendance/date/$date")
            val records = response.parseList<AttendanceRecord>()
            Result.success(records)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchAttendanceByDateRange(startDate: String, endDate: String): Result<List<AttendanceRecord>> {
        return try {
            val response = ktorClient.client.get("/api/v1/attendance/dateAttendance/$startDate/$endDate")
            val records = response.parseList<AttendanceRecord>()
            Result.success(records)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveAttendance(records: List<AttendancePayload>): Result<Unit> {
        return try {
            val response = ktorClient.client.post("/api/v1/attendance/save") {
                setBody(records)
                contentType(ContentType.Application.Json)
            }
            if (response.status.isSuccess()) Result.success(Unit)
            else Result.failure(Exception("Failed to save attendance"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAttendance(studentId: Int, status: String, date: String): Result<Unit> {
        return try {
            val response = ktorClient.client.put("/api/v1/attendance/$studentId/${status.uppercase()}/$date")
            if (response.status.isSuccess()) Result.success(Unit)
            else Result.failure(Exception("Failed to update attendance"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
