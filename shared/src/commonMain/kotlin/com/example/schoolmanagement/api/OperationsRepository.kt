package com.example.schoolmanagement.api

import com.example.schoolmanagement.api.models.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*

class OperationsRepository(private val ktorClient: KtorClient) {

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

    suspend fun promoteStudents(requests: List<EnrollmentRequest>): Result<Unit> {
        return try {
            val response = ktorClient.client.post("/api/v1/students/promote") {
                setBody(requests)
                contentType(ContentType.Application.Json)
            }
            if (response.status.isSuccess()) Result.success(Unit)
            else Result.failure(Exception("Failed to promote students"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchInvoiceHistory(startDate: String, endDate: String): Result<InvoiceHistoryResponse> {
        return try {
            val response = ktorClient.client.get("/api/v1/fee/invoice/history/$startDate/$endDate")
            val text = response.bodyAsText()
            val result = json.decodeFromString<InvoiceHistoryResponse>(text)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateLogins(payloads: List<RegisterRolePayload>): Result<Unit> {
        return try {
            // Assuming there is a bulk registration endpoint or individual calls
            // Backend might have /api/v1/auth/register-role-bulk or similar
            val response = ktorClient.client.post("/api/v1/auth/register-role-bulk") {
                setBody(payloads)
                contentType(ContentType.Application.Json)
            }
            if (response.status.isSuccess()) Result.success(Unit)
            else Result.failure(Exception("Failed to generate logins"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
