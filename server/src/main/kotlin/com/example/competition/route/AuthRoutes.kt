package com.example.competition.route

import com.example.competition.dto.AuthResponse
import com.example.competition.dto.ErrorResponse
import com.example.competition.dto.LoginRequest
import com.example.competition.dto.RegisterRequest
import com.example.competition.dto.UserDto
import com.example.competition.dto.VerifyResponse
import com.example.competition.service.AuthService
import com.example.competition.service.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes() {
    post("/api/auth/register") {
        val req = call.receive<RegisterRequest>()

        if (req.username.length < 3) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("用户名至少3个字符"))
            return@post
        }
        if (req.password.length < 6) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("密码至少6个字符"))
            return@post
        }
        if (req.nickname.isBlank() || req.nickname.length > 15) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("昵称无效"))
            return@post
        }
        if (AuthService.usernameExists(req.username)) {
            call.respond(HttpStatusCode.Conflict, ErrorResponse("用户名已存在"))
            return@post
        }

        val id = "user_${System.currentTimeMillis()}_${(1000..9999).random()}"
        val user = AuthService.createUser(id, req.username, req.password, req.nickname)
        val token = AuthService.createToken(user.id)

        call.respond(
            AuthResponse(
                user = UserDto(
                    id = user.id,
                    username = user.username,
                    nickname = user.nickname,
                    avatarEmoji = user.avatarEmoji,
                    createdAt = user.createdAt,
                    lastLoginAt = user.lastLoginAt
                ),
                token = token
            )
        )
    }

    post("/api/auth/login") {
        val req = call.receive<LoginRequest>()

        val user = AuthService.getUserByUsername(req.username)
        if (user == null || !AuthService.verifyPassword(req.password, user.passwordHash)) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse("用户名或密码错误"))
            return@post
        }

        val token = AuthService.createToken(user.id)

        call.respond(
            AuthResponse(
                user = UserDto(
                    id = user.id,
                    username = user.username,
                    nickname = user.nickname,
                    avatarEmoji = user.avatarEmoji,
                    createdAt = user.createdAt,
                    lastLoginAt = user.lastLoginAt
                ),
                token = token
            )
        )
    }

    get("/api/auth/verify") {
        val token = call.request.queryParameters["token"]
        if (token == null) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing token"))
            return@get
        }
        val userId = AuthService.validateToken(token)
        if (userId == null) {
            call.respond(VerifyResponse(valid = false))
            return@get
        }
        val user = AuthService.getUserById(userId)
        if (user == null) {
            call.respond(VerifyResponse(valid = false))
            return@get
        }
        call.respond(
            VerifyResponse(
                valid = true,
                user = UserDto(
                    id = user.id,
                    username = user.username,
                    nickname = user.nickname,
                    avatarEmoji = user.avatarEmoji,
                    createdAt = user.createdAt,
                    lastLoginAt = user.lastLoginAt
                )
            )
        )
    }
}
