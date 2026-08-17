package com.example.schoolmanagement.api.models

import kotlinx.serialization.Serializable

@Serializable
data class SessionItem(
    val sessionId: Int,
    val sessionName: String
)
