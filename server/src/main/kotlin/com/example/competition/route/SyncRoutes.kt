package com.example.competition.route

import com.example.competition.dto.ErrorResponse
import com.example.competition.dto.SyncDownloadResponse
import com.example.competition.dto.SyncUploadRequest
import com.example.competition.middleware.requireUserId
import com.example.competition.service.SyncService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.syncRoutes() {
    post("/api/sync/upload") {
        val userId = call.requireUserId() ?: return@post
        val req = call.receive<SyncUploadRequest>()
        SyncService.processUpload(req)
        call.respond(mapOf("synced" to true))
    }

    get("/api/sync/download") {
        val userId = call.requireUserId() ?: return@get
        val data = SyncService.getSyncData(userId)

        val allUsers = com.example.competition.service.AuthService.getUserById(userId)
        val allLeaderboard = com.example.competition.service.LeaderboardService.getLeaderboard()

        call.respond(
            SyncDownloadResponse(
                users = if (allUsers != null) {
                    listOf(
                        com.example.competition.dto.UserDto(
                            id = allUsers.id,
                            username = allUsers.username,
                            nickname = allUsers.nickname,
                            avatarEmoji = allUsers.avatarEmoji,
                            createdAt = allUsers.createdAt,
                            lastLoginAt = allUsers.lastLoginAt
                        )
                    )
                } else emptyList(),
                profiles = if (data.profile != null) listOf(data.profile) else emptyList(),
                leaderboardEntries = data.leaderboardEntries,
                challenges = emptyList()
            )
        )
    }
}
