package com.example.schoolmanagement.auth

import com.example.schoolmanagement.api.KtorClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val token: String? = null,
    val name: String? = null,
    val role: String? = null,
    val sessionId: Int? = null,
    val message: String? = null
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

class AuthRepository(
    private val ktorClient: KtorClient,
    private val tokenManager: TokenManager
) {
    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response: LoginResponse = ktorClient.client.post("/api/v1/auth/login") {
                setBody(LoginRequest(email, password))
            }.body()

            if (response.token != null) {
                tokenManager.saveToken(response.token)
                tokenManager.saveUserInfo(response.name ?: "", response.role ?: "")
                response.sessionId?.let { tokenManager.saveSessionId(it) }
                Result.success(response)
            } else {
                Result.failure(Exception(response.message ?: "Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        tokenManager.clear()
    }

    fun isLoggedIn(): Boolean = tokenManager.getToken() != null
}
