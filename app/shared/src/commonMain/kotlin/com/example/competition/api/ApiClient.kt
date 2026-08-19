package com.example.competition.api

import com.example.competition.PlatformUtils
import com.example.competition.api.dto.*
import com.example.competition.db.DatabaseManager
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object ApiClient {
    private var baseUrl: String = PlatformUtils.defaultBaseUrl()
    private var authToken: String? = null

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.HEADERS
        }
    }

    private val db get() = DatabaseManager.db

    fun init() {
        baseUrl = PlatformUtils.defaultBaseUrl()
    }

    fun getBaseUrl(): String = baseUrl

    fun configure(serverUrl: String) {
        baseUrl = serverUrl.trimEnd('/')
        saveUrl(baseUrl)
    }

    private fun saveUrl(url: String) {
        try {
            db.competitionQueriesQueries.setMeta("server_url", url)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadSavedUrl(): String? {
        return try {
            val value = db.competitionQueriesQueries.getMeta("server_url").executeAsOneOrNull()
            if (value.isNullOrBlank()) null else value
        } catch (e: Exception) {
            null
        }
    }

    fun setToken(token: String?) {
        authToken = token
        saveToken(token)
    }

    fun getToken(): String? = authToken ?: loadToken()

    private fun saveToken(token: String?) {
        try {
            if (token != null) {
                db.competitionQueriesQueries.setMeta("api_token", token)
            } else {
                db.competitionQueriesQueries.setMeta("api_token", "")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadToken(): String? {
        return try {
            val value = db.competitionQueriesQueries.getMeta("api_token").executeAsOneOrNull()
            if (value.isNullOrBlank()) null else value
        } catch (e: Exception) {
            null
        }
    }

    fun isAuthenticated(): Boolean = getToken() != null

    private suspend fun HttpRequestBuilder.authorize() {
        val token = getToken()
        if (token != null) {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
    }

    // Auth
    suspend fun register(username: String, password: String, nickname: String): Result<AuthResponse> = runCatching {
        val response = client.post("$baseUrl/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest(username, password, nickname))
        }
        response.body<AuthResponse>()
    }

    suspend fun login(username: String, password: String): Result<AuthResponse> = runCatching {
        val response = client.post("$baseUrl/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(username, password))
        }
        response.body<AuthResponse>()
    }

    suspend fun verifyToken(): Result<VerifyResponse> = runCatching {
        val token = getToken() ?: return@runCatching VerifyResponse(valid = false)
        val response = client.get("$baseUrl/api/auth/verify") {
            parameter("token", token)
        }
        response.body<VerifyResponse>()
    }

    // User
    suspend fun getMe(): Result<MeResponse> = runCatching {
        val response = client.get("$baseUrl/api/users/me") {
            authorize()
        }
        response.body<MeResponse>()
    }

    suspend fun updateProfile(nickname: String?, avatarEmoji: String?): Result<UserDto> = runCatching {
        val response = client.put("$baseUrl/api/users/me") {
            authorize()
            contentType(ContentType.Application.Json)
            setBody(UpdateProfileRequest(nickname, avatarEmoji))
        }
        response.body<UserDto>()
    }

    suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit> = runCatching {
        client.put("$baseUrl/api/users/me/password") {
            authorize()
            contentType(ContentType.Application.Json)
            setBody(ChangePasswordRequest(oldPassword, newPassword))
        }
    }

    // Leaderboard
    suspend fun getLeaderboard(level: Int? = null): Result<List<LeaderboardEntryDto>> = runCatching {
        val response = client.get("$baseUrl/api/leaderboard") {
            if (level != null) parameter("level", level)
        }
        val body = response.body<LeaderboardResponse>()
        body.entries
    }

    // Leaderboard sync
    suspend fun syncLeaderboardEntry(entry: LeaderboardEntryDto): Result<Unit> = runCatching {
        client.put("$baseUrl/api/leaderboard") {
            authorize()
            contentType(ContentType.Application.Json)
            setBody(entry)
        }
    }

    // Profile stats sync
    suspend fun syncProfileStats(profile: ProfileDto): Result<Unit> = runCatching {
        client.put("$baseUrl/api/users/me/stats") {
            authorize()
            contentType(ContentType.Application.Json)
            setBody(profile)
        }
    }

    // Challenges
    suspend fun createChallenge(request: CreateChallengeRequest): Result<ChallengeRecordDto> = runCatching {
        val response = client.post("$baseUrl/api/challenges") {
            authorize()
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        val body = response.body<ChallengeCreateResponse>()
        body.challenge
    }

    suspend fun getChallenges(): Result<List<ChallengeRecordDto>> = runCatching {
        val response = client.get("$baseUrl/api/challenges") {
            authorize()
        }
        val body = response.body<ChallengesResponse>()
        body.challenges
    }

    suspend fun getChallengeStats(): Result<ChallengeStatsDto> = runCatching {
        val response = client.get("$baseUrl/api/challenges/stats") {
            authorize()
        }
        response.body<ChallengeStatsDto>()
    }

    // Sync
    suspend fun uploadData(request: SyncUploadRequest): Result<Unit> = runCatching {
        client.post("$baseUrl/api/sync/upload") {
            authorize()
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun downloadData(): Result<SyncDownloadResponse> = runCatching {
        val response = client.get("$baseUrl/api/sync/download") {
            authorize()
        }
        response.body<SyncDownloadResponse>()
    }
}
