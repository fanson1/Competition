package com.example.competition

class JVMPlatform : Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
    override fun getUserDataPath(): String {
        val home = System.getProperty("user.home") ?: "."
        return "$home/.competition"
    }
}
