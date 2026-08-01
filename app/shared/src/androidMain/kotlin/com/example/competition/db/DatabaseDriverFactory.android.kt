package com.example.competition.db

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.example.competition.AndroidPlatform
import com.example.competition.PlatformHolder

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val context = (PlatformHolder.get() as AndroidPlatform).context
        return AndroidSqliteDriver(
            schema = CompetitionDatabase.Schema,
            context = context,
            name = "competition.db"
        )
    }
}
