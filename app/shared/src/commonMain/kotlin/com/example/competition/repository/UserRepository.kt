package com.example.competition.repository

import com.example.competition.model.User
import com.example.competition.model.UserProfile

interface UserRepository {
    suspend fun login(username: String, password: String): Result<User>
    suspend fun register(username: String, password: String, nickname: String): Result<User>
    suspend fun logout()
    fun getCurrentUser(): User?
    fun isLoggedIn(): Boolean
    suspend fun updateProfile(nickname: String?, avatarEmoji: String?): Result<User>
    suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit>
    fun getProfile(userId: String): UserProfile?
    fun getCurrentProfile(): UserProfile?
    suspend fun updateProfileStats(
        score: Int,
        level: Int,
        correctCount: Int,
        streak: Int,
        completedLevels: Set<Int>
    )
}
