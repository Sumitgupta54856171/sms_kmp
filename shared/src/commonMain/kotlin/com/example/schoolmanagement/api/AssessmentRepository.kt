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
        if (!status.isSuccess()) {
            throw Exception("API Error: ${status.value} - $text")
        }
        
        val element = json.parseToJsonElement(text)
        return if (element is JsonArray) {
            json.decodeFromJsonElement<List<T>>(element)
        } else if (element is JsonObject) {
            val obj = element.jsonObject
            val wrapped = obj["body"] ?: obj["data"]
            if (wrapped is JsonArray) {
                json.decodeFromJsonElement<List<T>>(wrapped)
            } else if (wrapped != null) {
                listOf(json.decodeFromJsonElement<T>(wrapped))
            } else {
                // If no "body" or "data", try to decode the whole object as T
                try {
                    listOf(json.decodeFromJsonElement<T>(element))
                } catch (e: Exception) {
                    emptyList()
                }
            }
        } else {
            emptyList()
        }
    }

    suspend fun fetchExamNames(): Result<List<String>> {
        return try {
            val response = ktorClient.client.get("/api/v1/timetable/examName")
            val names = parseNameList(response)
            Result.success(names)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchTestNames(): Result<List<String>> {
        return try {
            val response = ktorClient.client.get("/api/v1/timetable/testName")
            val names = parseNameList(response)
            Result.success(names)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchExamTimetable(name: String): Result<List<ExamTimetableEntry>> {
        return try {
            val encodedName = name.replace(" ", "%20")
            val response = ktorClient.client.get("/api/v1/timetable/examByName/$encodedName")
            val entries = response.parseList<ExamTimetableEntry>()
            Result.success(entries)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchTestTimetable(name: String): Result<List<ExamTimetableEntry>> {
        return try {
            val encodedName = name.replace(" ", "%20")
            val response = ktorClient.client.get("/api/v1/timetable/testByName/$encodedName")
            val entries = response.parseList<ExamTimetableEntry>()
            Result.success(entries)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun parseNameList(response: HttpResponse): List<String> {
        val text = response.bodyAsText()
        if (!response.status.isSuccess()) return emptyList()

        val element = json.parseToJsonElement(text)
        
        fun extractList(el: JsonElement): List<String> {
            return if (el is JsonArray) {
                el.mapNotNull { item ->
                    if (item is JsonPrimitive && item.isString) {
                        item.content
                    } else if (item is JsonObject) {
                        item["examName"]?.jsonPrimitive?.contentOrNull
                            ?: item["testName"]?.jsonPrimitive?.contentOrNull
                            ?: item["name"]?.jsonPrimitive?.contentOrNull
                            ?: item["timetableName"]?.jsonPrimitive?.contentOrNull
                            ?: item["exam_name"]?.jsonPrimitive?.contentOrNull
                            ?: item["test_name"]?.jsonPrimitive?.contentOrNull
                    } else null
                }
            } else if (el is JsonObject) {
                val wrapped = el["body"] ?: el["data"] ?: el["examNames"] ?: el["testNames"] ?: el["names"] ?: el["result"]
                if (wrapped != null) extractList(wrapped) else emptyList()
            } else {
                emptyList()
            }
        }

        return extractList(element)
    }

    suspend fun saveExamTimetable(entries: List<ExamTimetableEntry>): Result<Unit> {
        return try {
            val response = ktorClient.client.post("/api/v1/timetable/saveexamtimetable") {
                setBody(entries)
                contentType(ContentType.Application.Json)
            }
            if (response.status.isSuccess()) Result.success(Unit)
            else Result.failure(Exception("Failed to save exam timetable"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveTestTimetable(entries: List<ExamTimetableEntry>): Result<Unit> {
        return try {
            val response = ktorClient.client.post("/api/v1/timetable/savetesttimetable") {
                setBody(entries)
                contentType(ContentType.Application.Json)
            }
            if (response.status.isSuccess()) Result.success(Unit)
            else Result.failure(Exception("Failed to save test timetable"))
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
