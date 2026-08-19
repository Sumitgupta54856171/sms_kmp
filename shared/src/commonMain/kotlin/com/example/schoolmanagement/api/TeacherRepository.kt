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

    suspend fun updateTeacher(id: Int, teacher: PartialTeacherData): Result<Unit> {
        return try {
            // The backend update API might expect ID inside the body or as a param
            // Mirroring React: updateTeacher = async (id: string, data: Partial<TeacherData>) => { ... post("/api/v1/teachers/update", { ...data, id }); }
            val payload = buildJsonObject {
                teacher.fullName?.let { put("fullName", it) }
                teacher.email?.let { put("email", it) }
                teacher.employee_id?.let { put("employee_id", it) }
                put("id", id)
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

data class PartialTeacherData(
    val fullName: String? = null,
    val email: String? = null,
    val employee_id: String? = null
)
