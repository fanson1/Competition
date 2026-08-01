package com.example.competition.route

import com.example.competition.dto.LeaderboardEntryDto
import com.example.competition.dto.UpdateLeaderboardEntryRequest
import com.example.competition.middleware.requireUserId
import com.example.competition.service.LeaderboardService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.leaderboardRoutes() {
    get("/api/leaderboard") {
        val levelParam = call.request.queryParameters["level"]
        val level = levelParam?.toIntOrNull()
        val entries = LeaderboardService.getLeaderboard(level)
        call.respond(
            mapOf(
                "entries" to entries.map {
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
        )
    }

    put("/api/leaderboard") {
        val userId = call.requireUserId() ?: return@put
        val req = call.receive<UpdateLeaderboardEntryRequest>()
        LeaderboardService.upsertEntry(
            LeaderboardService.LeaderboardRow(
                userId = req.userId,
                username = req.username,
                nickname = req.nickname,
                avatarEmoji = req.avatarEmoji,
                score = req.score,
                level = req.level,
                correctCount = req.correctCount,
                timestamp = req.timestamp
            )
        )
        call.respond(mapOf("message" to "ok"))
    }
}
