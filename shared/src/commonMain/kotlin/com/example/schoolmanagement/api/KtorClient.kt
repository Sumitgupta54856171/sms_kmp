package com.example.schoolmanagement.api

import com.example.schoolmanagement.auth.TokenManager
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class KtorClient(private val tokenManager: TokenManager) {
    val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
        install(Logging) {
            level = LogLevel.ALL
        }
        defaultRequest {
            url("http://100.102.150.51:9091")
            contentType(ContentType.Application.Json)
            tokenManager.getToken()?.let {
                header("Authorization", "Bearer $it")
            }
            tokenManager.getSessionId()?.let {
                header("X-Session-Id", it.toString())
            }
        }
    }
}
