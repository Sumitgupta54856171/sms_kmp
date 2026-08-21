package com.example.schoolmanagement.api

import com.example.schoolmanagement.api.models.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*

class TeacherRepository(private val ktorClient: KtorClient) {

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

    suspend fun fetchAllTeachers(): Result<List<TeacherResponse>> {
        return try {
            val response = ktorClient.client.get("/api/v1/teachers/all")
            val teachers = response.parseList<TeacherResponse>()
            Result.success(teachers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveTeacher(teacher: TeacherData): Result<Unit> {
        return try {
            val response = ktorClient.client.post("/api/v1/teachers/save") {
                setBody(teacher)
                contentType(ContentType.Application.Json)
            }
            if (response.status.isSuccess()) Result.success(Unit)
            else Result.failure(Exception("Failed to save teacher"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTeacher(id: Int, teacher: TeacherData): Result<Unit> {
        return try {
            // Backend update expects id inside payload as per React code
            val payload = buildJsonObject {
                put("id", id)
                put("fullName", teacher.fullName)
                put("email", teacher.email)
                put("employee_id", teacher.employee_id)
                teacher.phone?.let { put("phone", it) }
                teacher.subject_specialization?.let { put("subject_specialization", it) }
                teacher.gender?.let { put("gender", it) }
                teacher.aadhaar_id?.let { put("aadhaar_id", it) }
                teacher.sssmid?.let { put("sssmid", it) }
                teacher.status?.let { put("status", it) }
                teacher.education?.let { put("education", it) }
                teacher.password?.let { if (it.isNotBlank()) put("password", it) }
            }
            val response = ktorClient.client.put("/api/v1/teachers/update") {
                setBody(payload)
                contentType(ContentType.Application.Json)
            }
            if (response.status.isSuccess()) Result.success(Unit)
            else Result.failure(Exception("Failed to update teacher"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTeacher(id: Int): Result<Unit> {
        return try {
            val response = ktorClient.client.delete("/api/v1/teachers/$id")
            if (response.status.isSuccess()) Result.success(Unit)
            else Result.failure(Exception("Failed to delete teacher"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
