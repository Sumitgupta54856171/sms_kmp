package com.example.schoolmanagement.api

import com.example.schoolmanagement.api.models.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*

class AssessmentRepository(private val ktorClient: KtorClient) {

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

    suspend fun fetchExamNames(): Result<List<String>> {
        return try {
            val response = ktorClient.client.get("/api/v1/timetable/examName")
            // Parse response which is a list of strings
            val names = response.parseList<String>()
            Result.success(names)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchExamTimetable(name: String): Result<List<ExamTimetableEntry>> {
        return try {
            val response = ktorClient.client.get("/api/v1/timetable/examByName/$name")
            val entries = response.parseList<ExamTimetableEntry>()
            Result.success(entries)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveExamMarks(payloads: List<ExamGradePayload>): Result<Unit> {
        return try {
            val response = ktorClient.client.post("/api/v1/grade/exam/mark/save") {
                setBody(payloads)
                contentType(ContentType.Application.Json)
            }
            if (response.status.isSuccess()) Result.success(Unit)
            else Result.failure(Exception("Failed to save marks"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
