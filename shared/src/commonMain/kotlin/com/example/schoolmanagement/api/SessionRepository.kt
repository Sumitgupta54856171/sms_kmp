package com.example.schoolmanagement.api

import com.example.schoolmanagement.api.models.SessionItem
import io.ktor.client.call.body
import io.ktor.client.request.get

class SessionRepository(private val ktorClient: KtorClient) {

    suspend fun fetchSessions(): Result<List<SessionItem>> {
        return try {
            val response: List<SessionItem> = ktorClient.client.get("/api/v1/session/get").body()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun switchSession(sessionId: Int): Result<Unit> {
        return try {
            ktorClient.client.get("/api/v1/session/switch/session/$sessionId")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
