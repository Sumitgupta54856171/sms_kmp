package com.example.schoolmanagement.auth

import com.russhwolf.settings.Settings

class TokenManager(private val settings: Settings = Settings()) {
    companion object {
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_SESSION_ID = "current_session_id"
    }

    fun saveSessionId(id: Int) {
        settings.putInt(KEY_SESSION_ID, id)
    }

    fun getSessionId(): Int? {
        val id = settings.getInt(KEY_SESSION_ID, -1)
        return if (id == -1) null else id
    }

    fun saveToken(token: String) {
        settings.putString(KEY_TOKEN, token)
    }

    fun getToken(): String? {
        return settings.getStringOrNull(KEY_TOKEN)
    }

    fun saveUserInfo(name: String, role: String) {
        settings.putString(KEY_USER_NAME, name)
        settings.putString(KEY_USER_ROLE, role)
    }

    fun getUserName(): String? = settings.getStringOrNull(KEY_USER_NAME)
    fun getUserRole(): String? = settings.getStringOrNull(KEY_USER_ROLE)

    fun clear() {
        settings.remove(KEY_TOKEN)
        settings.remove(KEY_USER_NAME)
        settings.remove(KEY_USER_ROLE)
    }
}
