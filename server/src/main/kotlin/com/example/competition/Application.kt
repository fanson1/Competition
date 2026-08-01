package com.example.competition

import com.example.competition.config.DatabaseConfig
import com.example.competition.dto.ErrorResponse
import com.example.competition.route.authRoutes
import com.example.competition.route.challengeRoutes
import com.example.competition.route.leaderboardRoutes
import com.example.competition.route.syncRoutes
import com.example.competition.route.userRoutes
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    DatabaseConfig.init()

    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = false
        })
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(HttpStatusCode.InternalServerError, ErrorResponse(cause.message ?: "Unknown error"))
        }
    }

    routing {
        get("/") {
            call.respond(mapOf("message" to "Competition API Server", "version" to "1.0.0"))
        }

        authRoutes()
        userRoutes()
        leaderboardRoutes()
        challengeRoutes()
        syncRoutes()
    }
}
