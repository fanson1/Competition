package com.example.competition.sync

import com.example.competition.api.ApiClient
import com.example.competition.api.dto.LeaderboardEntryDto
import com.example.competition.api.dto.ProfileDto
import com.example.competition.api.dto.SyncUploadRequest
import com.example.competition.api.dto.UserDto
import com.example.competition.data.UserManager
import com.example.competition.repository.bridge.RepositoryBridge

object SyncManager {
    suspend fun syncToOnline(): Result<Unit> = runCatching {
        // 1. Collect all local data
        val allUsers = UserManager.getAllUsers().map { user ->
            UserDto(
                id = user.id,
                username = user.username,
                nickname = user.nickname,
                avatarEmoji = user.avatarEmoji,
                createdAt = user.createdAt,
                lastLoginAt = user.lastLoginAt
            )
        }

        val allProfiles = UserManager.getAllUsers().mapNotNull { user ->
            val profile = UserManager.getProfile(user.id) ?: return@mapNotNull null
            ProfileDto(
                userId = profile.user.id,
                totalScore = profile.totalScore,
                maxLevel = profile.maxLevel,
                totalCorrectCount = profile.totalCorrectCount,
                totalGamesPlayed = profile.totalGamesPlayed,
                maxStreak = profile.maxStreak,
                levelScores = profile.levelScores.entries.joinToString(",") { "${it.key}:${it.value}" },
                levelCorrectCounts = profile.levelCorrectCounts.entries.joinToString(",") { "${it.key}:${it.value}" },
                completedLevels = profile.completedLevels.toList()
            )
        }

        val allLeaderboard = UserManager.getLeaderboard().map { entry ->
            LeaderboardEntryDto(
                userId = entry.userId,
                username = entry.username,
                nickname = entry.nickname,
                avatarEmoji = entry.avatarEmoji,
                score = entry.score,
                level = entry.level,
                correctCount = entry.correctCount,
                timestamp = entry.timestamp
            )
        }

        val currentUserId = UserManager.getCurrentUser()?.id

        // 2. Upload to server
        val uploadResult = ApiClient.uploadData(
            SyncUploadRequest(
                users = allUsers,
                profiles = allProfiles,
                leaderboardEntries = allLeaderboard,
                challenges = null
            )
        )
        if (uploadResult.isFailure) {
            throw uploadResult.exceptionOrNull() ?: Exception("Upload failed")
        }

        // 3. If logged in, load remote user data into cache
        if (currentUserId != null) {
            val remoteUserRepo = RepositoryBridge.getRemoteUser()
            val meResult = ApiClient.getMe()
            meResult.onSuccess { me ->
                remoteUserRepo.loadFromRemote(me.user)
            }
        }
    }

    suspend fun syncToOffline() {
        // When switching to offline, just update the mode
        // Local data is preserved from before going online
    }
}
