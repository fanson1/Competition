package com.example.competition.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val dbDir = File(System.getProperty("user.home"), ".competition")
        if (!dbDir.exists() && !dbDir.mkdirs()) {
            throw RuntimeException("Cannot create database directory: ${dbDir.absolutePath}")
        }
        val driver = JdbcSqliteDriver(
            "jdbc:sqlite:${dbDir.absolutePath}/competition.db"
        )
        CompetitionDatabase.Schema.create(driver)
        return driver
    }
}
