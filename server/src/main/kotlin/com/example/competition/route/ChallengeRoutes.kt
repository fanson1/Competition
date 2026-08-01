package com.example.competition.route

import com.example.competition.dto.ChallengeRecordDto
import com.example.competition.dto.ChallengeStatsDto
import com.example.competition.dto.CreateChallengeRequest
import com.example.competition.dto.ErrorResponse
import com.example.competition.middleware.requireUserId
import com.example.competition.service.ChallengeService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.challengeRoutes() {
    post("/api/challenges") {
        val userId = call.requireUserId() ?: return@post
        val req = call.receive<CreateChallengeRequest>()
        val id = "challenge_${System.currentTimeMillis()}_${(1000..9999).random()}"
        val record = ChallengeService.ChallengeRow(
            id = id,
            challengerId = userId,
            challengerName = req.targetName,
            targetId = req.targetId,
            targetName = req.targetName,
            level = req.level,
            challengerScore = req.challengerScore,
            targetScore = req.targetScore,
            isWin = req.isWin,
            timestamp = System.currentTimeMillis()
        )
        ChallengeService.addChallenge(record)
        call.respond(
            mapOf(
                "challenge" to ChallengeRecordDto(
                    id = record.id,
                    challengerId = record.challengerId,
                    challengerName = record.challengerName,
                    targetId = record.targetId,
                    targetName = record.targetName,
                    level = record.level,
                    challengerScore = record.challengerScore,
                    targetScore = record.targetScore,
                    isWin = record.isWin,
                    timestamp = record.timestamp
                )
            )
        )
    }

    get("/api/challenges") {
        val userId = call.requireUserId() ?: return@get
        val challenges = ChallengeService.getChallenges(userId)
        call.respond(
            mapOf(
                "challenges" to challenges.map {
                    ChallengeRecordDto(
                        id = it.id,
                        challengerId = it.challengerId,
                        challengerName = it.challengerName,
                        targetId = it.targetId,
                        targetName = it.targetName,
                        level = it.level,
                        challengerScore = it.challengerScore,
                        targetScore = it.targetScore,
                        isWin = it.isWin,
                        timestamp = it.timestamp
                    )
                }
            )
        )
    }

    get("/api/challenges/stats") {
        val userId = call.requireUserId() ?: return@get
        val stats = ChallengeService.getChallengeStats(userId)
        call.respond(
            ChallengeStatsDto(
                userId = stats.userId,
                totalChallenges = stats.totalChallenges,
                wins = stats.wins,
                losses = stats.losses,
                totalChallengeScore = stats.totalChallengeScore,
                winRate = stats.winRate,
                challengedTotal = stats.challengedTotal,
                challengedWins = stats.challengedWins,
                challengedLosses = stats.challengedLosses,
                challengedWinRate = stats.challengedWinRate
            )
        )
    }
}
