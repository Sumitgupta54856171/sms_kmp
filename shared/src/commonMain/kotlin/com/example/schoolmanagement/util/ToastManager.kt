package com.example.schoolmanagement.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

enum class ToastType {
    SUCCESS, ERROR, INFO
}

data class ToastEvent(
    val message: String,
    val type: ToastType = ToastType.INFO
)

class ToastManager {
    private val _events = MutableSharedFlow<ToastEvent>()
    val events = _events.asSharedFlow()

    suspend fun showToast(message: String, type: ToastType = ToastType.INFO) {
        _events.emit(ToastEvent(message, type))
    }

    suspend fun success(message: String) = showToast(message, ToastType.SUCCESS)
    suspend fun error(message: String) = showToast(message, ToastType.ERROR)
    suspend fun info(message: String) = showToast(message, ToastType.INFO)
}
