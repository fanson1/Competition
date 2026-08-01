package com.example.competition.service

import com.example.competition.model.Leaderboard
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object LeaderboardService {
    fun getLeaderboard(level: Int? = null): List<LeaderboardRow> {
        return transaction {
            val query = if (level != null) {
                Leaderboard.selectAll().where { Leaderboard.level eq level }
            } else {
                Leaderboard.selectAll()
            }
            query.orderBy(Leaderboard.score to org.jetbrains.exposed.sql.SortOrder.DESC)
                .map {
                    LeaderboardRow(
                        userId = it[Leaderboard.userId],
                        username = it[Leaderboard.username],
                        nickname = it[Leaderboard.nickname],
                        avatarEmoji = it[Leaderboard.avatarEmoji],
                        score = it[Leaderboard.score],
                        level = it[Leaderboard.level],
                        correctCount = it[Leaderboard.correctCount],
                        timestamp = it[Leaderboard.timestamp]
                    )
                }
        }
    }

    fun upsertEntry(entry: LeaderboardRow) {
        transaction {
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

    data class LeaderboardRow(
        val userId: String,
        val username: String,
        val nickname: String,
        val avatarEmoji: String,
        val score: Int,
        val level: Int,
        val correctCount: Int,
        val timestamp: Long
    )
}
