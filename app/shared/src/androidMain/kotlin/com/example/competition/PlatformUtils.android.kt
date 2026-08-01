package com.example.competition

actual object PlatformUtils {
    actual fun currentTimeMillis(): Long = System.currentTimeMillis()
    actual fun defaultBaseUrl(): String = "http://192.168.50.39:8080"
}
