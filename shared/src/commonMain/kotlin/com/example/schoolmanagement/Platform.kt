package com.example.schoolmanagement

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform