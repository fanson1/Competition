package com.example.competition

actual object PlatformUtils {
    actual fun currentTimeMillis(): Long = System.currentTimeMillis()
    actual fun defaultBaseUrl(): String = "http://localhost:8080"
}
