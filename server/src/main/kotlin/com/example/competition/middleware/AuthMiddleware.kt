package com.example.competition.middleware

import com.example.competition.dto.ErrorResponse
import com.example.competition.service.AuthService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*

suspend fun ApplicationCall.requireUserId(): String? {
    val authHeader = request.headers[HttpHeaders.Authorization] ?: return null
    if (!authHeader.startsWith("Bearer ")) return null
    val token = authHeader.removePrefix("Bearer ")
    val userId = AuthService.validateToken(token)
    if (userId == null) {
        respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid or expired token"))
        return null
    }
    return userId
}
