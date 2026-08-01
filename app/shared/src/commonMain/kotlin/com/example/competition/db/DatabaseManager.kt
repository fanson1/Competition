package com.example.competition.db

import app.cash.sqldelight.db.SqlDriver

object DatabaseManager {
    private var database: CompetitionDatabase? = null
    private var initialized = false

    fun init() {
        if (initialized) return
        val driver = DatabaseDriverFactory().createDriver()
        database = CompetitionDatabase(driver)
        initialized = true
    }

    val db: CompetitionDatabase
        get() = database ?: throw IllegalStateException("Database not initialized. Call DatabaseManager.init() first.")

    fun isInitialized(): Boolean = initialized
}
