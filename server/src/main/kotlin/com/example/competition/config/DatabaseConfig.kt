package com.example.competition.config

import com.example.competition.model.AuthTokens
import com.example.competition.model.Challenges
import com.example.competition.model.Leaderboard
import com.example.competition.model.ProfileCompletedLevels
import com.example.competition.model.Profiles
import com.example.competition.model.Users
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseConfig {
    private var initialized = false

    fun init() {
        if (initialized) return
        initialized = true

        val dbUrl = System.getenv("DATABASE_URL") ?: "jdbc:h2:file:./data/competition;DB_CLOSE_DELAY=-1"
        val dbUser = System.getenv("DATABASE_USER") ?: "sa"
        val dbPassword = System.getenv("DATABASE_PASSWORD") ?: ""

        Database.connect(url = dbUrl, user = dbUser, password = dbPassword)

        transaction {
            SchemaUtils.create(
                Users,
                Profiles,
                ProfileCompletedLevels,
                Leaderboard,
                Challenges,
                AuthTokens
            )
        }
    }
}
