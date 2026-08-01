package com.example.competition.repository.remote

import com.example.competition.api.ApiClient
import com.example.competition.api.dto.CreateChallengeRequest
import com.example.competition.model.ChallengeRecord
import com.example.competition.model.ChallengeStats
import com.example.competition.repository.ChallengeRepository

class RemoteChallengeRepository : ChallengeRepository {
    override suspend fun addChallenge(record: ChallengeRecord) {
        ApiClient.createChallenge(
            CreateChallengeRequest(
                targetId = record.targetId,
                targetName = record.targetName,
                level = record.level,
                challengerScore = record.challengerScore,
                targetScore = record.targetScore,
                isWin = record.isWin
            )
        )
    }

    override suspend fun getChallenges(userId: String): List<ChallengeRecord> {
        val result = ApiClient.getChallenges()
        return result.getOrDefault(emptyList()).map { toRecord(it) }
    }

    override suspend fun getChallengesAsChallenger(userId: String): List<ChallengeRecord> {
        return getChallenges(userId).filter { it.challengerId == userId }
    }

    override suspend fun getChallengesAsTarget(userId: String): List<ChallengeRecord> {
        return getChallenges(userId).filter { it.targetId == userId }
    }

    override suspend fun getChallengeStats(userId: String): ChallengeStats {
        val result = ApiClient.getChallengeStats()
        return result.map { dto ->
            ChallengeStats(
                userId = dto.userId,
                totalChallenges = dto.totalChallenges,
                wins = dto.wins,
                losses = dto.losses,
                totalChallengeScore = dto.totalChallengeScore,
                winRate = dto.winRate,
                challengedTotal = dto.challengedTotal,
                challengedWins = dto.challengedWins,
                challengedLosses = dto.challengedLosses,
                challengedWinRate = dto.challengedWinRate
            )
        }.getOrDefault(
            ChallengeStats(userId = userId)
        )
    }

    private fun toRecord(dto: com.example.competition.api.dto.ChallengeRecordDto): ChallengeRecord {
        return ChallengeRecord(
            id = dto.id,
            challengerId = dto.challengerId,
            challengerName = dto.challengerName,
            targetId = dto.targetId,
            targetName = dto.targetName,
            level = dto.level,
            challengerScore = dto.challengerScore,
            targetScore = dto.targetScore,
            isWin = dto.isWin,
            timestamp = dto.timestamp
        )
    }
}
