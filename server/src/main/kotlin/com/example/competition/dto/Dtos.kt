package com.example.competition.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val username: String,
    val password: String,
    val nickname: String
)

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val user: UserDto,
    val token: String
)

@Serializable
data class VerifyResponse(
    val valid: Boolean,
    val user: UserDto? = null
)

@Serializable
data class UpdateProfileRequest(
    val nickname: String? = null,
    val avatarEmoji: String? = null
)

@Serializable
data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)

@Serializable
data class UserDto(
    val id: String,
    val username: String,
    val nickname: String,
    val avatarEmoji: String,
    val createdAt: Long,
    val lastLoginAt: Long
)

@Serializable
data class ProfileDto(
    val userId: String,
    val totalScore: Int = 0,
    val maxLevel: Int = 0,
    val totalCorrectCount: Int = 0,
    val totalGamesPlayed: Int = 0,
    val maxStreak: Int = 0,
    val levelScores: String = "",
    val levelCorrectCounts: String = "",
    val completedLevels: List<Int> = emptyList()
)

@Serializable
data class LeaderboardEntryDto(
    val userId: String,
    val username: String,
    val nickname: String,
    val avatarEmoji: String,
    val score: Int,
    val level: Int,
    val correctCount: Int,
    val timestamp: Long
)

@Serializable
data class ChallengeRecordDto(
    val id: String,
    val challengerId: String,
    val challengerName: String,
    val targetId: String,
    val targetName: String,
    val level: Int,
    val challengerScore: Int,
    val targetScore: Int,
    val isWin: Boolean,
    val timestamp: Long
)

@Serializable
data class CreateChallengeRequest(
    val targetId: String,
    val targetName: String,
    val level: Int,
    val challengerScore: Int,
    val targetScore: Int,
    val isWin: Boolean
)

@Serializable
data class ChallengeStatsDto(
    val userId: String,
    val totalChallenges: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val totalChallengeScore: Int = 0,
    val winRate: Float = 0f,
    val challengedTotal: Int = 0,
    val challengedWins: Int = 0,
    val challengedLosses: Int = 0,
    val challengedWinRate: Float = 0f
)

@Serializable
data class SyncUploadRequest(
    val users: List<UserDto>? = null,
    val profiles: List<ProfileDto>? = null,
    val leaderboardEntries: List<LeaderboardEntryDto>? = null,
    val challenges: List<ChallengeRecordDto>? = null
)

@Serializable
data class SyncDownloadResponse(
    val users: List<UserDto> = emptyList(),
    val profiles: List<ProfileDto> = emptyList(),
    val leaderboardEntries: List<LeaderboardEntryDto> = emptyList(),
    val challenges: List<ChallengeRecordDto> = emptyList()
)

@Serializable
data class UpdateLeaderboardEntryRequest(
    val userId: String,
    val username: String,
    val nickname: String,
    val avatarEmoji: String,
    val score: Int,
    val level: Int,
    val correctCount: Int,
    val timestamp: Long
)

@Serializable
data class UpdateProfileStatsRequest(
    val userId: String,
    val totalScore: Int = 0,
    val maxLevel: Int = 0,
    val totalCorrectCount: Int = 0,
    val totalGamesPlayed: Int = 0,
    val maxStreak: Int = 0,
    val levelScores: String = "",
    val levelCorrectCounts: String = "",
    val completedLevels: List<Int> = emptyList()
)

@Serializable
data class MeResponse(
    val user: UserDto,
    val profile: ProfileDto? = null
)

@Serializable
data class ErrorResponse(
    val error: String
)

@Serializable
data class SuccessResponse(
    val message: String = "ok"
)

@Serializable
data class UsersListResponse(
    val users: List<UserDto>
)
