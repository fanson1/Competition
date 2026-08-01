package com.example.competition.service

import com.example.competition.model.Challenges
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object ChallengeService {
    fun addChallenge(record: ChallengeRow) {
        transaction {
            Challenges.insert {
                it[Challenges.id] = record.id
                it[Challenges.challengerId] = record.challengerId
                it[Challenges.challengerName] = record.challengerName
                it[Challenges.targetId] = record.targetId
                it[Challenges.targetName] = record.targetName
                it[Challenges.level] = record.level
                it[Challenges.challengerScore] = record.challengerScore
                it[Challenges.targetScore] = record.targetScore
                it[Challenges.isWin] = record.isWin
                it[Challenges.timestamp] = record.timestamp
            }
        }
    }

    fun getChallenges(userId: String): List<ChallengeRow> {
        return transaction {
            Challenges.selectAll()
                .where { (Challenges.challengerId eq userId) or (Challenges.targetId eq userId) }
                .orderBy(Challenges.timestamp to org.jetbrains.exposed.sql.SortOrder.DESC)
                .map { mapRow(it) }
        }
    }

    fun getChallengesAsChallenger(userId: String): List<ChallengeRow> {
        return transaction {
            Challenges.selectAll()
                .where { Challenges.challengerId eq userId }
                .orderBy(Challenges.timestamp to org.jetbrains.exposed.sql.SortOrder.DESC)
                .map { mapRow(it) }
        }
    }

    fun getChallengesAsTarget(userId: String): List<ChallengeRow> {
        return transaction {
            Challenges.selectAll()
                .where { Challenges.targetId eq userId }
                .orderBy(Challenges.timestamp to org.jetbrains.exposed.sql.SortOrder.DESC)
                .map { mapRow(it) }
        }
    }

    fun getChallengeStats(userId: String): ChallengeStatsRow {
        val asChallenger = getChallengesAsChallenger(userId)
        val asTarget = getChallengesAsTarget(userId)

        val challengerWins = asChallenger.count { it.isWin }
        val challengerTotal = asChallenger.size
        val challengerScore = asChallenger.sumOf { it.challengerScore }

        val challengedWins = asTarget.count { !it.isWin }
        val challengedLosses = asTarget.count { it.isWin }
        val challengedTotal = asTarget.size

        return ChallengeStatsRow(
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

    private fun mapRow(row: org.jetbrains.exposed.sql.ResultRow): ChallengeRow {
        return ChallengeRow(
            id = row[Challenges.id],
            challengerId = row[Challenges.challengerId],
            challengerName = row[Challenges.challengerName],
            targetId = row[Challenges.targetId],
            targetName = row[Challenges.targetName],
            level = row[Challenges.level],
            challengerScore = row[Challenges.challengerScore],
            targetScore = row[Challenges.targetScore],
            isWin = row[Challenges.isWin],
            timestamp = row[Challenges.timestamp]
        )
    }

    data class ChallengeRow(
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

    data class ChallengeStatsRow(
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
}
