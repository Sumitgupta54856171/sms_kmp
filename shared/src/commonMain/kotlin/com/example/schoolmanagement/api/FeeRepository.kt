package com.example.schoolmanagement.api

import com.example.schoolmanagement.api.models.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*

class FeeRepository(private val ktorClient: KtorClient) {

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

    suspend fun fetchStudentsByClass(className: String): Result<List<StudentListItem>> {
        return try {
            val response = ktorClient.client.get("/api/v1/students/class/$className")
            val rawList = response.parseList<FeeStudentResponse>()
            val students = rawList.map { item ->
                val s = item.student
                val finalName = s?.studentName ?: s?.name ?: ""
                val finalId = s?.id ?: s?.studentId ?: item.id ?: item.studentId
                StudentListItem(
                    studentName = finalName,
                    name = finalName,
                    studentId = finalId,
                    scholarNo = s?.scholarNo ?: s?.scholar_no,
                    scholar_no = s?.scholarNo ?: s?.scholar_no,
                    faterhName = s?.faterhName ?: s?.father_name,
                    father_name = s?.faterhName ?: s?.father_name,
                    motherName = s?.motherName,
                    status = s?.status,
                    className = s?.className ?: className,
                    class_no = s?.class_no ?: className,
                    rollNo = item.roll_no ?: s?.rollNo ?: s?.roll_no,
                    roll_no = item.roll_no ?: s?.rollNo ?: s?.roll_no,
                    id = finalId,
                    enrollmentId = item.enrollmentId ?: s?.enrollmentId
                )
            }
            Result.success(students)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchStudentFeeDetails(studentId: Int): Result<StudentFeeDetails> {
        return try {
            val response = ktorClient.client.get("/api/v1/fee/student/$studentId/fee")
            val details = response.parseObject<StudentFeeDetails>()
            if (details != null) Result.success(details)
            else Result.failure(Exception("Failed to parse fee details"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchInvoicesByEnrollmentId(enrollmentId: Int): Result<List<InvoiceData>> {
        return try {
            val response = ktorClient.client.get("/api/v1/fee/get/invoice/$enrollmentId")
            val invoices = response.parseList<InvoiceData>()
            Result.success(invoices)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchStudentSessions(studentId: Int): Result<List<StudentSession>> {
        return try {
            val response = ktorClient.client.get("/api/v1/fee/get/session/sessionName/$studentId")
            val sessions = response.parseList<StudentSession>()
            Result.success(sessions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchSessionWiseHistory(studentId: Int): Result<List<SessionWiseHistory>> {
        return try {
            val response = ktorClient.client.get("/api/v1/fee/session-wise/history/$studentId")
            val history = response.parseList<SessionWiseHistory>()
            Result.success(history)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createInvoice(payload: InvoicePayload): Result<InvoiceResponse> {
        return try {
            val response = ktorClient.client.post("/api/v1/fee/student/fees/collection/invoice") {
                setBody(payload)
                contentType(ContentType.Application.Json)
            }
            val invoiceResponse = response.parseObject<InvoiceResponse>()
            if (invoiceResponse != null) Result.success(invoiceResponse)
            else Result.failure(Exception("Failed to create invoice"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun applyDiscount(studentId: Int, discountAmount: Double): Result<Unit> {
        return try {
            val response = ktorClient.client.put("/api/v1/fee/update/fees/$studentId/$discountAmount")
            if (response.status.isSuccess()) Result.success(Unit)
            else Result.failure(Exception("Failed to apply discount"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
