package com.example.schoolmanagement.api

import com.example.schoolmanagement.auth.TokenManager
import com.example.schoolmanagement.util.ToastManager
import io.ktor.client.HttpClient
import io.ktor.client.call.save
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class KtorClient(private val tokenManager: TokenManager, private val toastManager: ToastManager) {
    private val jsonInstance = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        isLenient = true
    }

    private val ToastPlugin = createClientPlugin("ToastPlugin") {
        onResponse { response ->
            if (response.request.headers["X-Skip-Toast"] == "true") return@onResponse

            val method = response.request.method
            val isMutation = method == HttpMethod.Post || method == HttpMethod.Put || 
                             method == HttpMethod.Patch || method == HttpMethod.Delete
            
            val status = response.status.value
            if (status in 200..299) {
                if (isMutation) {
                    try {
                        val text = response.call.save().response.bodyAsText()
                        val element = jsonInstance.parseToJsonElement(text)
                        if (element is JsonObject) {
                            val json = element.jsonObject
                            val message = json["message"]?.jsonPrimitive?.content ?: json["success"]?.jsonPrimitive?.content
                            if (!message.isNullOrBlank()) {
                                toastManager.success(message)
                            }
                        }
                    } catch (e: Exception) { }
                }
            } else {
                try {
                    val text = response.call.save().response.bodyAsText()
                    val element = jsonInstance.parseToJsonElement(text)
                    val message = if (element is JsonObject) {
                        element.jsonObject["message"]?.jsonPrimitive?.content ?: "Error: ${response.status}"
                    } else "Error: ${response.status}"
                    toastManager.error(message)
                } catch (e: Exception) {
                    toastManager.error("Something went wrong ($status)")
                }
            }
        }
    }

    val client = HttpClient {
        expectSuccess = false
        install(ContentNegotiation) {
            json(jsonInstance)
        }
        install(Logging) {
            level = LogLevel.ALL
        }
        install(ToastPlugin)
        
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
