package com.example.schoolmanagement.api

import com.example.schoolmanagement.api.models.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*

class AcademicRepository(private val ktorClient: KtorClient) {

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

    suspend fun fetchAllTimetables(): Result<List<PeriodEntry>> {
        return try {
            val response = ktorClient.client.get("/api/v1/academic-options/time-table/all")
            val periods = response.parseList<PeriodEntry>()
            Result.success(periods)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchTimetableByGrade(grade: String): Result<List<PeriodEntry>> {
        return try {
            val response = ktorClient.client.get("/api/v1/academic-options/time-table/grade/$grade")
            val periods = response.parseList<PeriodEntry>()
            Result.success(periods)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun savePeriod(period: PeriodEntry): Result<Unit> {
        return try {
            val response = ktorClient.client.post("/api/v1/academic-options/time-table/period") {
                setBody(period)
                contentType(ContentType.Application.Json)
            }
            if (response.status.isSuccess()) Result.success(Unit)
            else Result.failure(Exception("Failed to save period"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePeriod(id: Int): Result<Unit> {
        return try {
            val response = ktorClient.client.delete("/api/v1/academic-options/delete/period/$id")
            if (response.status.isSuccess()) Result.success(Unit)
            else Result.failure(Exception("Failed to delete period"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchAllClassTeachers(): Result<List<ClassTeacherAssignment>> {
        return try {
            val response = ktorClient.client.get("/api/v1/academic-options/timetable/class-teachers/all")
            val assignments = response.parseList<ClassTeacherAssignment>()
            Result.success(assignments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
