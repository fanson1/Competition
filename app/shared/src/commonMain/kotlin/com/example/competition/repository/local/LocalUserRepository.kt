package com.example.competition.repository.local

import com.example.competition.data.UserManager
import com.example.competition.model.User
import com.example.competition.model.UserProfile
import com.example.competition.repository.UserRepository

class LocalUserRepository : UserRepository {

    override suspend fun login(username: String, password: String): Result<User> {
        return UserManager.login(username, password)
    }

    override suspend fun register(username: String, password: String, nickname: String): Result<User> {
        return UserManager.register(username, password, nickname)
    }

    override suspend fun logout() {
        UserManager.logout()
    }

    override fun getCurrentUser(): User? {
        return UserManager.getCurrentUser()
    }

    override fun isLoggedIn(): Boolean {
        return UserManager.isLoggedIn()
    }

    override suspend fun updateProfile(nickname: String?, avatarEmoji: String?): Result<User> {
        return UserManager.updateProfile(nickname, avatarEmoji)
    }

    override suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit> {
        return UserManager.changePassword(oldPassword, newPassword)
    }

    override fun getProfile(userId: String): UserProfile? {
        return UserManager.getProfile(userId)
    }

    override fun getCurrentProfile(): UserProfile? {
        return UserManager.getCurrentProfile()
    }

    override suspend fun updateProfileStats(
        score: Int,
        level: Int,
        correctCount: Int,
        streak: Int,
        completedLevels: Set<Int>
    ) {
        UserManager.updateProfileStats(score, level, correctCount, streak, completedLevels)
    }
}
