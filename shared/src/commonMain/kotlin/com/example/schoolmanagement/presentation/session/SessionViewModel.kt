package com.example.schoolmanagement.presentation.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schoolmanagement.api.SessionRepository
import com.example.schoolmanagement.api.models.SessionItem
import com.example.schoolmanagement.auth.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SessionState {
    object Loading : SessionState()
    data class Success(val sessions: List<SessionItem>, val currentSession: SessionItem?) : SessionState()
    data class Error(val message: String) : SessionState()
}

class SessionViewModel(
    private val repository: SessionRepository,
    private val tokenManager: TokenManager,
    private val onSessionChanged: () -> Unit
) : ViewModel() {
    private val _state = MutableStateFlow<SessionState>(SessionState.Loading)
    val state: StateFlow<SessionState> = _state.asStateFlow()

    init {
        loadSessions()
    }

    fun loadSessions() {
        viewModelScope.launch {
            _state.value = SessionState.Loading
            repository.fetchSessions()
                .onSuccess { sessions ->
                    val currentId = tokenManager.getSessionId()
                    val currentSession = sessions.find { it.sessionId == currentId }
                    _state.value = SessionState.Success(sessions, currentSession)
                }
                .onFailure { error ->
                    _state.value = SessionState.Error(error.message ?: "Failed to load sessions")
                }
        }
    }

    fun switchSession(sessionId: Int) {
        viewModelScope.launch {
            repository.switchSession(sessionId)
                .onSuccess {
                    tokenManager.saveSessionId(sessionId)
                    loadSessions()
                    onSessionChanged()
                }
        }
    }
}
