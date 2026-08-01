package com.example.competition

expect object PlatformUtils {
    fun currentTimeMillis(): Long
    fun defaultBaseUrl(): String
}
