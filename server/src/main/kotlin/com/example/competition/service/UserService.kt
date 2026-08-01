package com.example.competition.service

import com.example.competition.model.ProfileCompletedLevels
import com.example.competition.model.Profiles
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object UserService {
    fun getProfile(userId: String): ProfileRow? {
        return transaction {
            val row = Profiles.selectAll().where { Profiles.userId eq userId }.singleOrNull() ?: return@transaction null
            val completedLevels = ProfileCompletedLevels.selectAll()
                .where { ProfileCompletedLevels.userId eq userId }
                .map { it[ProfileCompletedLevels.level] }
            ProfileRow(
                userId = row[Profiles.userId],
                totalScore = row[Profiles.totalScore],
                maxLevel = row[Profiles.maxLevel],
                totalCorrectCount = row[Profiles.totalCorrectCount],
                totalGamesPlayed = row[Profiles.totalGamesPlayed],
                maxStreak = row[Profiles.maxStreak],
                levelScores = row[Profiles.levelScores],
                levelCorrectCounts = row[Profiles.levelCorrectCounts],
                completedLevels = completedLevels
            )
        }
    }

    fun updateProfile(userId: String, nickname: String?, avatarEmoji: String?) {
        transaction {
            if (nickname != null) {
                com.example.competition.model.Users.update({ com.example.competition.model.Users.id eq userId }) {
                    it[com.example.competition.model.Users.nickname] = nickname
                }
            }
            if (avatarEmoji != null) {
                com.example.competition.model.Users.update({ com.example.competition.model.Users.id eq userId }) {
                    it[com.example.competition.model.Users.avatarEmoji] = avatarEmoji
                }
            }
        }
    }

    fun changePassword(userId: String, newPassword: String) {
        val hash = AuthService.hashPassword(newPassword)
        transaction {
            com.example.competition.model.Users.update({ com.example.competition.model.Users.id eq userId }) {
                it[com.example.competition.model.Users.passwordHash] = hash
            }
        }
    }

    fun upsertProfile(profile: ProfileRow) {
        transaction {
            val existing = Profiles.selectAll().where { Profiles.userId eq profile.userId }.singleOrNull()
            if (existing != null) {
                Profiles.update({ Profiles.userId eq profile.userId }) {
                    it[totalScore] = profile.totalScore
                    it[maxLevel] = profile.maxLevel
                    it[totalCorrectCount] = profile.totalCorrectCount
                    it[totalGamesPlayed] = profile.totalGamesPlayed
                    it[maxStreak] = profile.maxStreak
                    it[levelScores] = profile.levelScores
                    it[levelCorrectCounts] = profile.levelCorrectCounts
                }
            } else {
                Profiles.insert {
                    it[Profiles.userId] = profile.userId
                    it[totalScore] = profile.totalScore
                    it[maxLevel] = profile.maxLevel
                    it[totalCorrectCount] = profile.totalCorrectCount
                    it[totalGamesPlayed] = profile.totalGamesPlayed
                    it[maxStreak] = profile.maxStreak
                    it[levelScores] = profile.levelScores
                    it[levelCorrectCounts] = profile.levelCorrectCounts
                }
            }
            ProfileCompletedLevels.deleteWhere { ProfileCompletedLevels.userId eq profile.userId }
            for (level in profile.completedLevels) {
                ProfileCompletedLevels.insert {
                    it[ProfileCompletedLevels.userId] = profile.userId
                    it[ProfileCompletedLevels.level] = level
                }
            }
        }
    }

    data class ProfileRow(
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
}
