package com.example.competition.data

import com.example.competition.PlatformUtils
import com.example.competition.db.DatabaseManager
import com.example.competition.model.ChallengeRecord
import com.example.competition.model.ChallengeStats
import com.example.competition.model.LeaderboardEntry
import com.example.competition.model.User
import com.example.competition.model.UserProfile
import com.example.competition.util.LevelMapCodec

object UserManager {
    private var currentUser: User? = null
    private var users: MutableMap<String, User> = mutableMapOf()
    private var profiles: MutableMap<String, UserProfile> = mutableMapOf()
    private var leaderboard: MutableList<LeaderboardEntry> = mutableListOf()
    private var challenges: MutableList<ChallengeRecord> = mutableListOf()
    private var autoLoginEnabled: Boolean = true
    private var initialized = false

    private val db get() = DatabaseManager.db

    fun init() {
        if (initialized) return

        DatabaseManager.init()
        loadAllFromDatabase()

        loadAutoLoginSetting()
        if (autoLoginEnabled) {
            val lastUserId = loadLastUserIdSetting()
            if (lastUserId != null) {
                val user = users.values.firstOrNull { it.id == lastUserId }
                if (user != null) {
                    currentUser = user
                }
            }
        }

        initialized = true
    }

    fun getCurrentUser(): User? = currentUser

    fun setCurrentUser(user: User) {
        currentUser = user
        if (!users.containsKey(user.id)) {
            users[user.id] = user
            saveUserToDb(user)
        }
    }

    fun isLoggedIn(): Boolean = currentUser != null

    fun register(username: String, password: String, nickname: String): Result<User> {
        if (username.length < 3) return Result.failure(Exception("用户名至少3个字符"))
        if (username.length > 20) return Result.failure(Exception("用户名最多20个字符"))
        if (password.length < 6) return Result.failure(Exception("密码至少6个字符"))
        if (nickname.isBlank()) return Result.failure(Exception("昵称不能为空"))
        if (nickname.length > 15) return Result.failure(Exception("昵称最多15个字符"))
        if (users.containsKey(username)) return Result.failure(Exception("用户名已存在"))

        val user = User(
            id = generateId(),
            username = username,
            passwordHash = hashPassword(password),
            nickname = nickname,
            avatarEmoji = "😀",
            createdAt = PlatformUtils.currentTimeMillis(),
            lastLoginAt = PlatformUtils.currentTimeMillis()
        )

        users[username] = user
        profiles[user.id] = UserProfile(user = user)
        currentUser = user

        saveUserToDb(user)
        saveProfileToDb(user.id)
        saveLastUserIdSetting(user.id)

        return Result.success(user)
    }

    fun login(username: String, password: String): Result<User> {
        val user = users[username] ?: return Result.failure(Exception("用户不存在"))
        if (user.passwordHash != hashPassword(password)) return Result.failure(Exception("密码错误"))

        val updatedUser = user.copy(lastLoginAt = PlatformUtils.currentTimeMillis())
        users[username] = updatedUser
        currentUser = updatedUser
        saveUserToDb(updatedUser)
        saveLastUserIdSetting(user.id)

        return Result.success(updatedUser)
    }

    fun logout() {
        currentUser = null
        clearLastUserIdSetting()
    }

    fun updateProfile(
        nickname: String? = null,
        avatarEmoji: String? = null
    ): Result<User> {
        val user = currentUser ?: return Result.failure(Exception("未登录"))

        val updatedUser = user.copy(
            nickname = nickname ?: user.nickname,
            avatarEmoji = avatarEmoji ?: user.avatarEmoji
        )

        users[user.username] = updatedUser
        currentUser = updatedUser

        val profile = profiles[user.id]
        if (profile != null) {
            profiles[user.id] = profile.copy(user = updatedUser)
        }

        saveUserToDb(updatedUser)
        saveProfileToDb(user.id)

        for (i in leaderboard.indices) {
            val entry = leaderboard[i]
            if (entry.userId == user.id) {
                val updated = LeaderboardEntry(
                    userId = entry.userId,
                    username = updatedUser.username,
                    nickname = updatedUser.nickname,
                    avatarEmoji = updatedUser.avatarEmoji,
                    score = entry.score,
                    level = entry.level,
                    correctCount = entry.correctCount,
                    timestamp = entry.timestamp
                )
                leaderboard[i] = updated
                saveLeaderboardToDb(updated)
            }
        }

        return Result.success(updatedUser)
    }

    fun changePassword(oldPassword: String, newPassword: String): Result<Unit> {
        val user = currentUser ?: return Result.failure(Exception("未登录"))
        if (user.passwordHash != hashPassword(oldPassword)) return Result.failure(Exception("原密码错误"))
        if (newPassword.length < 6) return Result.failure(Exception("新密码至少6个字符"))

        val updatedUser = user.copy(passwordHash = hashPassword(newPassword))
        users[user.username] = updatedUser
        currentUser = updatedUser

        saveUserToDb(updatedUser)
        return Result.success(Unit)
    }

    fun getProfile(userId: String): UserProfile? = profiles[userId]

    fun getCurrentProfile(): UserProfile? {
        val user = currentUser ?: return null
        return profiles[user.id]
    }

    fun setCurrentProfile(profile: UserProfile) {
        profiles[profile.user.id] = profile
    }

    fun updateProfileStats(
        score: Int,
        level: Int,
        correctCount: Int,
        streak: Int,
        completedLevels: Set<Int>
    ) {
        val user = currentUser ?: return
        val profile = profiles[user.id] ?: return

        // Per-level: only keep the highest score and max correct count
        val newLevelScores = profile.levelScores.toMutableMap()
        if (score > (newLevelScores[level] ?: 0)) {
            newLevelScores[level] = score
        }

        val newLevelCorrectCounts = profile.levelCorrectCounts.toMutableMap()
        if (correctCount > (newLevelCorrectCounts[level] ?: 0)) {
            newLevelCorrectCounts[level] = correctCount
        }

        val updatedProfile = profile.copy(
            totalScore = newLevelScores.values.sum(),
            totalCorrectCount = newLevelCorrectCounts.values.sum(),
            maxLevel = maxOf(profile.maxLevel, level),
            completedLevels = completedLevels,
            totalGamesPlayed = profile.totalGamesPlayed + 1,
            maxStreak = maxOf(profile.maxStreak, streak),
            levelScores = newLevelScores,
            levelCorrectCounts = newLevelCorrectCounts
        )
        profiles[user.id] = updatedProfile

        // Persist to database
        saveProfileToDb(user.id)
    }

    fun updateLeaderboard(entry: LeaderboardEntry) {
        val existingIndex = leaderboard.indexOfFirst {
            it.userId == entry.userId && it.level == entry.level
        }
        if (existingIndex >= 0) {
            if (entry.score > leaderboard[existingIndex].score) {
                leaderboard[existingIndex] = entry
            } else {
                return // No change needed
            }
        } else {
            leaderboard.add(entry)
        }
        leaderboard.sortByDescending { it.score }

        // Persist to database
        saveLeaderboardToDb(entry)
    }

    fun getLeaderboard(level: Int? = null): List<LeaderboardEntry> {
        return if (level != null) {
            leaderboard.filter { it.level == level }
                .groupBy { it.userId }
                .map { (_, entries) -> entries.maxBy { it.score } }
                .sortedByDescending { it.score }
        } else {
            leaderboard.groupBy { it.userId }.map { (_, entries) ->
                val best = entries.maxBy { it.score }
                val profile = profiles[best.userId]
                LeaderboardEntry(
                    userId = best.userId,
                    username = best.username,
                    nickname = best.nickname,
                    avatarEmoji = best.avatarEmoji,
                    score = entries.sumOf { it.score },
                    level = entries.maxOf { it.level },
                    correctCount = profile?.totalCorrectCount ?: entries.sumOf { it.correctCount },
                    timestamp = entries.maxOf { it.timestamp }
                )
            }.sortedByDescending { it.score }
        }
    }

    fun getAllUsers(): List<User> = users.values.toList()

    fun addChallenge(record: ChallengeRecord) {
        challenges.add(record)
        // Persist to database
        saveChallengeToDb(record)
    }

    fun getChallenges(userId: String): List<ChallengeRecord> {
        return challenges.filter {
            it.challengerId == userId || it.targetId == userId
        }.sortedByDescending { it.timestamp }
    }

    fun getChallengesAsChallenger(userId: String): List<ChallengeRecord> {
        return challenges.filter { it.challengerId == userId }
            .sortedByDescending { it.timestamp }
    }

    fun getChallengesAsTarget(userId: String): List<ChallengeRecord> {
        return challenges.filter { it.targetId == userId }
            .sortedByDescending { it.timestamp }
    }

    fun getChallengeStats(userId: String): ChallengeStats {
        val asChallenger = challenges.filter { it.challengerId == userId }
        val challengerWins = asChallenger.count { it.isWin }
        val challengerTotal = asChallenger.size
        val challengerScore = asChallenger.sumOf { it.challengerScore }

        val asTarget = challenges.filter { it.targetId == userId }
        val challengedLosses = asTarget.count { it.isWin }
        val challengedWins = asTarget.count { !it.isWin }
        val challengedTotal = asTarget.size

        return ChallengeStats(
            userId = userId,
            totalChallenges = challengerTotal,
            wins = challengerWins,
            losses = challengerTotal - challengerWins,
            totalChallengeScore = challengerScore,
            winRate = if (challengerTotal > 0) challengerWins.toFloat() / challengerTotal else 0f,
            challengedTotal = challengedTotal,
            challengedWins = challengedWins,
            challengedLosses = challengedLosses,
            challengedWinRate = if (challengedTotal > 0) challengedWins.toFloat() / challengedTotal else 0f
        )
    }

    fun getLevelChallengers(level: Int): List<LeaderboardEntry> {
        return leaderboard.filter { it.level == level }
            .groupBy { it.userId }
            .map { (_, entries) -> entries.maxBy { it.score } }
            .sortedByDescending { it.score }
    }

    // ---- Database persistence methods ----

    private fun loadAllFromDatabase() {
        try {
            loadUsersFromDb()
            loadProfilesFromDb()
            loadLeaderboardFromDb()
            loadChallengesFromDb()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadUsersFromDb() {
        db.competitionQueriesQueries.getAllUsers().executeAsList().forEach { entity ->
            users[entity.username] = User(
                id = entity.id,
                username = entity.username,
                passwordHash = entity.passwordHash,
                nickname = entity.nickname,
                avatarEmoji = entity.avatarEmoji,
                createdAt = entity.createdAt,
                lastLoginAt = entity.lastLoginAt
            )
        }
    }

    private fun loadProfilesFromDb() {
        db.competitionQueriesQueries.getAllProfiles().executeAsList().forEach { entity ->
            val user = users.values.firstOrNull { it.id == entity.userId } ?: return@forEach
            val completedLevels = db.competitionQueriesQueries.getProfileCompletedLevels(entity.userId)
                .executeAsList().map { it.toInt() }.toSet()
            val levelScores = LevelMapCodec.decode(entity.levelScores)

            profiles[entity.userId] = UserProfile(
                user = user,
                totalScore = entity.totalScore.toInt(),
                maxLevel = entity.maxLevel.toInt(),
                completedLevels = completedLevels,
                totalCorrectCount = entity.totalCorrectCount.toInt(),
                totalGamesPlayed = entity.totalGamesPlayed.toInt(),
                maxStreak = entity.maxStreak.toInt(),
                levelScores = levelScores,
                levelCorrectCounts = LevelMapCodec.decode(entity.levelCorrectCounts)
            )
        }
    }

    private fun loadLeaderboardFromDb() {
        db.competitionQueriesQueries.getLeaderboardAll().executeAsList().forEach { entity ->
            leaderboard.add(
                LeaderboardEntry(
                    userId = entity.userId,
                    username = entity.username,
                    nickname = entity.nickname,
                    avatarEmoji = entity.avatarEmoji,
                    score = entity.score.toInt(),
                    level = entity.level.toInt(),
                    correctCount = entity.correctCount.toInt(),
                    timestamp = entity.timestamp
                )
            )
        }
    }

    private fun loadChallengesFromDb() {
        db.competitionQueriesQueries.getAllChallenges().executeAsList().forEach { entity ->
            challenges.add(
                ChallengeRecord(
                    id = entity.id,
                    challengerId = entity.challengerId,
                    challengerName = entity.challengerName,
                    targetId = entity.targetId,
                    targetName = entity.targetName,
                    level = entity.level.toInt(),
                    challengerScore = entity.challengerScore.toInt(),
                    targetScore = entity.targetScore.toInt(),
                    isWin = entity.isWin == 1L,
                    timestamp = entity.timestamp
                )
            )
        }
    }

    private fun saveUserToDb(user: User) {
        try {
            db.competitionQueriesQueries.insertUser(
                id = user.id,
                username = user.username,
                passwordHash = user.passwordHash,
                nickname = user.nickname,
                avatarEmoji = user.avatarEmoji,
                createdAt = user.createdAt,
                lastLoginAt = user.lastLoginAt
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveProfileToDb(userId: String) {
        try {
            val profile = profiles[userId] ?: return
            db.competitionQueriesQueries.insertOrReplaceProfile(
                userId = profile.user.id,
                totalScore = profile.totalScore.toLong(),
                maxLevel = profile.maxLevel.toLong(),
                totalCorrectCount = profile.totalCorrectCount.toLong(),
                totalGamesPlayed = profile.totalGamesPlayed.toLong(),
                maxStreak = profile.maxStreak.toLong(),
                levelScores = LevelMapCodec.encode(profile.levelScores),
                levelCorrectCounts = LevelMapCodec.encode(profile.levelCorrectCounts)
            )
            // Save completed levels
            db.competitionQueriesQueries.deleteProfileCompletedLevels(userId)
            for (level in profile.completedLevels) {
                db.competitionQueriesQueries.insertProfileCompletedLevel(userId, level.toLong())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveLeaderboardToDb(entry: LeaderboardEntry) {
        try {
            db.competitionQueriesQueries.insertOrReplaceLeaderboard(
                userId = entry.userId,
                username = entry.username,
                nickname = entry.nickname,
                avatarEmoji = entry.avatarEmoji,
                score = entry.score.toLong(),
                level = entry.level.toLong(),
                correctCount = entry.correctCount.toLong(),
                timestamp = entry.timestamp
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveChallengeToDb(record: ChallengeRecord) {
        try {
            db.competitionQueriesQueries.insertChallenge(
                id = record.id,
                challengerId = record.challengerId,
                challengerName = record.challengerName,
                targetId = record.targetId,
                targetName = record.targetName,
                level = record.level.toLong(),
                challengerScore = record.challengerScore.toLong(),
                targetScore = record.targetScore.toLong(),
                isWin = if (record.isWin) 1L else 0L,
                timestamp = record.timestamp
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ---- Settings (meta table) ----

    private fun loadAutoLoginSetting() {
        try {
            val value = db.competitionQueriesQueries.getMeta("autoLogin").executeAsOneOrNull()
            autoLoginEnabled = value != "false"
        } catch (e: Exception) {
            autoLoginEnabled = true
        }
    }

    private fun loadLastUserIdSetting(): String? {
        return try {
            db.competitionQueriesQueries.getMeta("lastUserId").executeAsOneOrNull()
        } catch (e: Exception) {
            null
        }
    }

    private fun saveLastUserIdSetting(userId: String) {
        try {
            db.competitionQueriesQueries.setMeta("lastUserId", userId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun clearLastUserIdSetting() {
        try {
            db.competitionQueriesQueries.setMeta("lastUserId", "")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ---- Utility methods ----

    private fun generateId(): String {
        return "user_${PlatformUtils.currentTimeMillis()}_${(1000..9999).random()}"
    }

    private fun hashPassword(password: String): String {
        val sb = StringBuilder()
        for (ch in password) {
            val code = ch.code
            sb.append(code.toString(16).padStart(2, '0'))
        }
        return sb.toString()
    }
}
