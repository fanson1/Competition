package com.example.competition.service

import com.example.competition.dto.LeaderboardEntryDto
import com.example.competition.dto.ProfileDto
import com.example.competition.dto.SyncUploadRequest
import com.example.competition.model.Leaderboard
import com.example.competition.model.ProfileCompletedLevels
import com.example.competition.model.Profiles
import com.example.competition.model.Users
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object SyncService {
    fun processUpload(request: SyncUploadRequest) {
        transaction {
            request.users?.let { users ->
                for (user in users) {
                    val existing = Users.selectAll().where { Users.id eq user.id }.singleOrNull()
                    if (existing == null) {
                        Users.insert {
                            it[Users.id] = user.id
                            it[Users.username] = user.username
                            it[Users.passwordHash] = ""
                            it[Users.nickname] = user.nickname
                            it[Users.avatarEmoji] = user.avatarEmoji
                            it[Users.createdAt] = user.createdAt
                            it[Users.lastLoginAt] = user.lastLoginAt
                        }
                    }
                }
            }

            request.profiles?.let { profiles ->
                for (profile in profiles) {
                    val existing = Profiles.selectAll().where { Profiles.userId eq profile.userId }.singleOrNull()
                    if (existing != null) {
                        val oldTotal = existing[Profiles.totalScore]
                        val oldLevel = existing[Profiles.maxLevel]
                        val oldCorrect = existing[Profiles.totalCorrectCount]
                        val oldGames = existing[Profiles.totalGamesPlayed]
                        val oldStreak = existing[Profiles.maxStreak]
                        val oldLevelScores = existing[Profiles.levelScores]
                        val oldLevelCorrectCounts = existing[Profiles.levelCorrectCounts]
                        Profiles.update({ Profiles.userId eq profile.userId }) {
                            it[Profiles.totalScore] = maxOf(oldTotal, profile.totalScore)
                            it[Profiles.maxLevel] = maxOf(oldLevel, profile.maxLevel)
                            it[Profiles.totalCorrectCount] = maxOf(oldCorrect, profile.totalCorrectCount)
                            it[Profiles.totalGamesPlayed] = maxOf(oldGames, profile.totalGamesPlayed)
                            it[Profiles.maxStreak] = maxOf(oldStreak, profile.maxStreak)
                            it[Profiles.levelScores] = if (profile.levelScores.isNotBlank()) profile.levelScores else oldLevelScores
                            it[Profiles.levelCorrectCounts] = if (profile.levelCorrectCounts.isNotBlank()) profile.levelCorrectCounts else oldLevelCorrectCounts
                        }
                    } else {
                        Profiles.insert {
                            it[Profiles.userId] = profile.userId
                            it[Profiles.totalScore] = profile.totalScore
                            it[Profiles.maxLevel] = profile.maxLevel
                            it[Profiles.totalCorrectCount] = profile.totalCorrectCount
                            it[Profiles.totalGamesPlayed] = profile.totalGamesPlayed
                            it[Profiles.maxStreak] = profile.maxStreak
                            it[Profiles.levelScores] = profile.levelScores
                            it[Profiles.levelCorrectCounts] = profile.levelCorrectCounts
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

            request.leaderboardEntries?.let { entries ->
                for (entry in entries) {
                    val existing = Leaderboard.selectAll()
                        .where { (Leaderboard.userId eq entry.userId) and (Leaderboard.level eq entry.level) }
                        .singleOrNull()
                    if (existing == null) {
                        Leaderboard.insert {
                            it[Leaderboard.userId] = entry.userId
                            it[Leaderboard.username] = entry.username
                            it[Leaderboard.nickname] = entry.nickname
                            it[Leaderboard.avatarEmoji] = entry.avatarEmoji
                            it[Leaderboard.score] = entry.score
                            it[Leaderboard.level] = entry.level
                            it[Leaderboard.correctCount] = entry.correctCount
                            it[Leaderboard.timestamp] = entry.timestamp
                        }
                    } else if (entry.score > existing[Leaderboard.score]) {
                        Leaderboard.update({ (Leaderboard.userId eq entry.userId) and (Leaderboard.level eq entry.level) }) {
                            it[Leaderboard.score] = entry.score
                            it[Leaderboard.correctCount] = entry.correctCount
                            it[Leaderboard.timestamp] = entry.timestamp
                            it[Leaderboard.username] = entry.username
                            it[Leaderboard.nickname] = entry.nickname
                            it[Leaderboard.avatarEmoji] = entry.avatarEmoji
                        }
                    }
                }
            }

            request.challenges?.let { challenges ->
                for (challenge in challenges) {
                    com.example.competition.model.Challenges.insert {
                        it[com.example.competition.model.Challenges.id] = challenge.id
                        it[com.example.competition.model.Challenges.challengerId] = challenge.challengerId
                        it[com.example.competition.model.Challenges.challengerName] = challenge.challengerName
                        it[com.example.competition.model.Challenges.targetId] = challenge.targetId
                        it[com.example.competition.model.Challenges.targetName] = challenge.targetName
                        it[com.example.competition.model.Challenges.level] = challenge.level
                        it[com.example.competition.model.Challenges.challengerScore] = challenge.challengerScore
                        it[com.example.competition.model.Challenges.targetScore] = challenge.targetScore
                        it[com.example.competition.model.Challenges.isWin] = challenge.isWin
                        it[com.example.competition.model.Challenges.timestamp] = challenge.timestamp
                    }
                }
            }
        }
    }

    fun getSyncData(userId: String): SyncData {
        val profile = UserService.getProfile(userId)
        val leaderboardEntries = LeaderboardService.getLeaderboard()
        return SyncData(
            profile = profile?.let {
                ProfileDto(
                    userId = it.userId,
                    totalScore = it.totalScore,
                    maxLevel = it.maxLevel,
                    totalCorrectCount = it.totalCorrectCount,
                    totalGamesPlayed = it.totalGamesPlayed,
                    maxStreak = it.maxStreak,
                    levelScores = it.levelScores,
                    levelCorrectCounts = it.levelCorrectCounts,
                    completedLevels = it.completedLevels
                )
            },
            leaderboardEntries = leaderboardEntries.map {
                LeaderboardEntryDto(
                    userId = it.userId,
                    username = it.username,
                    nickname = it.nickname,
                    avatarEmoji = it.avatarEmoji,
                    score = it.score,
                    level = it.level,
                    correctCount = it.correctCount,
                    timestamp = it.timestamp
                )
            }
        )
    }

    data class SyncData(
        val profile: ProfileDto?,
        val leaderboardEntries: List<LeaderboardEntryDto>
    )
}
