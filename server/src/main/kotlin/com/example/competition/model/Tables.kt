package com.example.competition.model

import org.jetbrains.exposed.sql.Table

object Users : Table("users") {
    val id = text("id")
    val username = text("username").uniqueIndex()
    val passwordHash = text("password_hash")
    val nickname = text("nickname")
    val avatarEmoji = text("avatar_emoji").default("😀")
    val createdAt = long("created_at").default(0)
    val lastLoginAt = long("last_login_at").default(0)

    override val primaryKey = PrimaryKey(id)
}

object Profiles : Table("profiles") {
    val userId = text("user_id").references(Users.id)
    val totalScore = integer("total_score").default(0)
    val maxLevel = integer("max_level").default(0)
    val totalCorrectCount = integer("total_correct_count").default(0)
    val totalGamesPlayed = integer("total_games_played").default(0)
    val maxStreak = integer("max_streak").default(0)
    val levelScores = text("level_scores").default("")
    val levelCorrectCounts = text("level_correct_counts").default("")

    override val primaryKey = PrimaryKey(userId)
}

object ProfileCompletedLevels : Table("profile_completed_levels") {
    val userId = text("user_id").references(Users.id)
    val level = integer("level")

    override val primaryKey = PrimaryKey(userId, level)
}

object Leaderboard : Table("leaderboard") {
    val userId = text("user_id")
    val username = text("username")
    val nickname = text("nickname")
    val avatarEmoji = text("avatar_emoji")
    val score = integer("score")
    val level = integer("level")
    val correctCount = integer("correct_count")
    val timestamp = long("timestamp")

    override val primaryKey = PrimaryKey(userId, level)
}

object Challenges : Table("challenges") {
    val id = text("id")
    val challengerId = text("challenger_id")
    val challengerName = text("challenger_name")
    val targetId = text("target_id")
    val targetName = text("target_name")
    val level = integer("level")
    val challengerScore = integer("challenger_score")
    val targetScore = integer("target_score")
    val isWin = bool("is_win").default(false)
    val timestamp = long("timestamp")

    override val primaryKey = PrimaryKey(id)
}

object AuthTokens : Table("auth_tokens") {
    val token = text("token")
    val userId = text("user_id").references(Users.id)
    val createdAt = long("created_at")
    val expiresAt = long("expires_at")

    override val primaryKey = PrimaryKey(token)
}
