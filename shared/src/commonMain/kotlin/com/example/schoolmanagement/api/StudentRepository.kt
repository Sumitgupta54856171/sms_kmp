package com.example.schoolmanagement.api

import com.example.schoolmanagement.api.models.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*

class StudentRepository(private val ktorClient: KtorClient) {

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
            // Always unwrap body/data wrapper first (mirrors React: body ?? data ?? response)
            val wrapped = obj["body"] ?: obj["data"]
            val target = wrapped ?: element
            json.decodeFromJsonElement<T>(target)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun fetchStudentList(): Result<List<StudentListItem>> {
        return try {
            val response = ktorClient.client.get("/api/v1/students/studentlist")
            val students = response.parseList<StudentListItem>()
            Result.success(students)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchStudentsByClass(classNo: String): Result<List<StudentListItem>> {
        return try {
            val response = ktorClient.client.get("/api/v1/students/class/v1/$classNo")
            val text = response.bodyAsText()
            val element = json.parseToJsonElement(text)
            
            // Unwrap body or data if the response is wrapped
            val root = if (element is JsonObject) {
                element.jsonObject["body"] ?: element.jsonObject["data"] ?: element
            } else element

            val students = if (root is JsonObject) {
                val detail = root.jsonObject["studentdetail"]
                if (detail is JsonArray) {
                    json.decodeFromJsonElement<List<StudentListItem>>(detail)
                } else emptyList()
            } else emptyList()
            
            Result.success(students)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchStudentDetail(studentId: Int): Result<StudentDetailResponse> {
        return try {
            val response = ktorClient.client.get("/api/v1/students/student-detail/$studentId")
            val detail = response.parseObject<StudentDetailResponse>()
            if (detail != null) Result.success(detail)
            else Result.failure(Exception("Failed to parse student detail"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveStudent(request: StudentCreateRequest): Result<Unit> {
        return try {
            val response = ktorClient.client.post("/api/v1/students/save") {
                setBody(request)
                contentType(ContentType.Application.Json)
            }
            if (response.status.isSuccess()) Result.success(Unit)
            else Result.failure(Exception("Failed to save student: ${response.status}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveBankDetails(payload: BankDetailData): Result<Unit> {
        return try {
            val response = ktorClient.client.post("/api/v1/students/bank-details") {
                setBody(payload)
                contentType(ContentType.Application.Json)
            }
            if (response.status.isSuccess()) Result.success(Unit)
            else Result.failure(Exception("Failed to save bank details"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteStudentPhoto(studentId: Int): Result<Unit> {
        return try {
            val response = ktorClient.client.delete("/api/v1/students/photo/delete/$studentId")
            if (response.status.isSuccess()) Result.success(Unit)
            else Result.failure(Exception("Failed to delete photo"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchStudentPhoto(studentId: Int): Result<PhotoData> {
        return try {
            val response = ktorClient.client.get("/api/v1/students/photo/$studentId")
            val photo = response.parseObject<PhotoData>()
            if (photo != null) Result.success(photo)
            else Result.failure(Exception("No photo found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
