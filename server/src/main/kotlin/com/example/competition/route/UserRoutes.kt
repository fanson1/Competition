package com.example.competition.route

import com.example.competition.dto.ChangePasswordRequest
import com.example.competition.dto.ErrorResponse
import com.example.competition.dto.MeResponse
import com.example.competition.dto.ProfileDto
import com.example.competition.dto.UpdateProfileRequest
import com.example.competition.dto.UpdateProfileStatsRequest
import com.example.competition.dto.UserDto
import com.example.competition.dto.UsersListResponse
import com.example.competition.middleware.requireUserId
import com.example.competition.service.AuthService
import com.example.competition.service.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoutes() {
    get("/api/users") {
        val userId = call.requireUserId() ?: return@get
        val users = AuthService.getAllUsers()
        call.respond(
            UsersListResponse(
                users = users.map {
                    UserDto(
                        id = it.id,
                        username = it.username,
                        nickname = it.nickname,
                        avatarEmoji = it.avatarEmoji,
                        createdAt = it.createdAt,
                        lastLoginAt = it.lastLoginAt
                    )
                }
            )
        )
    }

    get("/api/users/me") {
        val userId = call.requireUserId() ?: return@get
        val user = AuthService.getUserById(userId) ?: run {
            call.respond(HttpStatusCode.NotFound, ErrorResponse("User not found"))
            return@get
        }
        val profile = UserService.getProfile(userId)
        call.respond(
            MeResponse(
                user = UserDto(
                    id = user.id,
                    username = user.username,
                    nickname = user.nickname,
                    avatarEmoji = user.avatarEmoji,
                    createdAt = user.createdAt,
                    lastLoginAt = user.lastLoginAt
                ),
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
                }
            )
        )
    }

    put("/api/users/me") {
        val userId = call.requireUserId() ?: return@put
        val req = call.receive<UpdateProfileRequest>()
        UserService.updateProfile(userId, req.nickname, req.avatarEmoji)
        val user = AuthService.getUserById(userId) ?: run {
            call.respond(HttpStatusCode.NotFound, ErrorResponse("User not found"))
            return@put
        }
        call.respond(
            UserDto(
                id = user.id,
                username = user.username,
                nickname = user.nickname,
                avatarEmoji = user.avatarEmoji,
                createdAt = user.createdAt,
                lastLoginAt = user.lastLoginAt
            )
        )
    }

    put("/api/users/me/password") {
        val userId = call.requireUserId() ?: return@put
        val req = call.receive<ChangePasswordRequest>()
        val user = AuthService.getUserById(userId) ?: run {
            call.respond(HttpStatusCode.NotFound, ErrorResponse("User not found"))
            return@put
        }
        if (!AuthService.verifyPassword(req.oldPassword, user.passwordHash)) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("原密码错误"))
            return@put
        }
        if (req.newPassword.length < 6) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("新密码至少6个字符"))
            return@put
        }
        UserService.changePassword(userId, req.newPassword)
        call.respond(mapOf("message" to "ok"))
    }

    put("/api/users/me/stats") {
        val userId = call.requireUserId() ?: return@put
        val req = call.receive<UpdateProfileStatsRequest>()
        UserService.upsertProfile(
            UserService.ProfileRow(
                userId = userId,
                totalScore = req.totalScore,
                maxLevel = req.maxLevel,
                totalCorrectCount = req.totalCorrectCount,
                totalGamesPlayed = req.totalGamesPlayed,
                maxStreak = req.maxStreak,
                levelScores = req.levelScores,
                levelCorrectCounts = req.levelCorrectCounts,
                completedLevels = req.completedLevels
            )
        )
        call.respond(mapOf("message" to "ok"))
    }
}
