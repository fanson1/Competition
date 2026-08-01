package com.example.competition

object PlatformHolder {
    private var platform: Platform? = null

    fun init(p: Platform) {
        platform = p
    }

    fun get(): Platform = platform ?: throw IllegalStateException("Platform not initialized")
}
