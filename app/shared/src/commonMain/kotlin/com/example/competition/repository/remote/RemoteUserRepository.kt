package com.example.competition.repository.remote

import com.example.competition.api.ApiClient
import com.example.competition.data.UserManager
import com.example.competition.model.User
import com.example.competition.model.UserProfile
import com.example.competition.repository.UserRepository

class RemoteUserRepository : UserRepository {
    private var currentUser: User? = null
    private var currentProfile: UserProfile? = null

    override suspend fun login(username: String, password: String): Result<User> {
        val result = ApiClient.login(username, password)
        return result.map { authResp ->
            ApiClient.setToken(authResp.token)
            val user = toUser(authResp.user)
            currentUser = user
            resetState()
            user
        }
    }

    override suspend fun register(username: String, password: String, nickname: String): Result<User> {
        val result = ApiClient.register(username, password, nickname)
        return result.map { authResp ->
            ApiClient.setToken(authResp.token)
            val user = toUser(authResp.user)
            currentUser = user
            resetState()
            user
        }
    }

    override suspend fun logout() {
        ApiClient.setToken(null)
        currentUser = null
        currentProfile = null
        resetState()
    }

    private fun resetState() {
        levelScores.clear()
        levelCorrectCounts.clear()
    }

    override fun getCurrentUser(): User? = currentUser

    override fun isLoggedIn(): Boolean = currentUser != null

    override suspend fun updateProfile(nickname: String?, avatarEmoji: String?): Result<User> {
        val result = ApiClient.updateProfile(nickname, avatarEmoji)
        return result.map { userDto ->
            val updated = currentUser?.copy(
                nickname = userDto.nickname,
                avatarEmoji = userDto.avatarEmoji
            ) ?: toUser(userDto)
            currentUser = updated
            updated
        }
    }

    override suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit> {
        return ApiClient.changePassword(oldPassword, newPassword)
    }

    override fun getProfile(userId: String): UserProfile? = currentProfile

    override fun getCurrentProfile(): UserProfile? = currentProfile

    private var levelScores: MutableMap<Int, Int> = mutableMapOf()
    private var levelCorrectCounts: MutableMap<Int, Int> = mutableMapOf()

    override suspend fun updateProfileStats(
        score: Int,
        level: Int,
        correctCount: Int,
        streak: Int,
        completedLevels: Set<Int>
    ) {
        val user = currentUser ?: return

        if (score > (levelScores[level] ?: 0)) {
            levelScores[level] = score
        }
        if (correctCount > (levelCorrectCounts[level] ?: 0)) {
            levelCorrectCounts[level] = correctCount
        }

        val totalScore = levelScores.values.sum()
        val totalCorrectCount = levelCorrectCounts.values.sum()

        currentProfile = currentProfile?.copy(
            totalScore = totalScore,
            maxLevel = maxOf(currentProfile!!.maxLevel, level),
            totalCorrectCount = totalCorrectCount,
            totalGamesPlayed = currentProfile!!.totalGamesPlayed + 1,
            maxStreak = maxOf(currentProfile!!.maxStreak, streak),
            completedLevels = completedLevels,
            levelScores = levelScores.toMap(),
            levelCorrectCounts = levelCorrectCounts.toMap()
        )
        ApiClient.syncProfileStats(
            com.example.competition.api.dto.ProfileDto(
                userId = user.id,
                totalScore = totalScore,
                maxLevel = currentProfile?.maxLevel ?: level,
                totalCorrectCount = totalCorrectCount,
                totalGamesPlayed = currentProfile?.totalGamesPlayed ?: 1,
                maxStreak = currentProfile?.maxStreak ?: streak,
                levelScores = levelScores.entries.joinToString(",") { "${it.key}:${it.value}" },
                levelCorrectCounts = levelCorrectCounts.entries.joinToString(",") { "${it.key}:${it.value}" },
                completedLevels = completedLevels.toList()
            )
        )

        // Keep local UserManager in sync for UI display
        currentProfile?.let { UserManager.setCurrentProfile(it) }
        currentUser?.let { UserManager.setCurrentUser(it) }
    }

    fun loadFromRemote(userDto: com.example.competition.api.dto.UserDto) {
        currentUser = toUser(userDto)
        resetState()
    }

    fun loadProfile(profile: com.example.competition.model.UserProfile) {
        currentProfile = profile
        levelScores.clear()
        levelScores.putAll(profile.levelScores)
        levelCorrectCounts.clear()
        levelCorrectCounts.putAll(profile.levelCorrectCounts)
    }

    private fun toUser(dto: com.example.competition.api.dto.UserDto): User {
        return User(
            id = dto.id,
            username = dto.username,
            passwordHash = "",
            nickname = dto.nickname,
            avatarEmoji = dto.avatarEmoji,
            createdAt = dto.createdAt,
            lastLoginAt = dto.lastLoginAt
        )
    }
}
