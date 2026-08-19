package com.example.competition.model

data class User(
    val id: String,
    val username: String,
    val passwordHash: String,
    val nickname: String,
    val avatarEmoji: String = "😀",
    val createdAt: Long = 0L,
    val lastLoginAt: Long = 0L
)

data class UserProfile(
    val user: User,
    val totalScore: Int = 0,
    val maxLevel: Int = 0,
    val completedLevels: Set<Int> = emptySet(),
    val totalCorrectCount: Int = 0,
    val totalGamesPlayed: Int = 0,
    val maxStreak: Int = 0,
    val levelScores: Map<Int, Int> = emptyMap(),
    val levelCorrectCounts: Map<Int, Int> = emptyMap(),
    val levelBestTimes: Map<Int, Int> = emptyMap()
)

data class LeaderboardEntry(
    val userId: String,
    val username: String,
    val nickname: String,
    val avatarEmoji: String,
    val score: Int,
    val level: Int,
    val correctCount: Int,
    val timestamp: Long
)

data class ChallengeRecord(
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

data class ChallengeStats(
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

enum class AvatarEmoji(val emoji: String, val label: String) {
    SMILE("😀", "开心"),
    COOL("😎", "酷"),
    SMART("🤓", "学霸"),
    ROCKET("🚀", "冲刺"),
    STAR("⭐", "明星"),
    FIRE("🔥", "火热"),
    CROWN("👑", "王者"),
    SWORD("⚔️", "战士"),
    SHIELD("🛡️", "守护"),
    BRAIN("🧠", "智慧"),
    LIGHTNING("⚡", "闪电"),
    TROPHY("🏆", "冠军"),
    GEM("💎", "钻石"),
    CRYSTAL("🔮", "预言"),
    PAWN("♟️", "棋手")
}
