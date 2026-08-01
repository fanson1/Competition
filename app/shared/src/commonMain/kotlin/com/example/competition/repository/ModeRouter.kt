package com.example.competition.repository

import com.example.competition.db.DatabaseManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ModeRouter {
    private val _currentMode = MutableStateFlow(AppMode.OFFLINE)
    val currentMode: StateFlow<AppMode> = _currentMode.asStateFlow()

    private val db get() = DatabaseManager.db

    fun init() {
        val saved = loadSavedMode()
        _currentMode.value = saved
    }

    fun isModeSelected(): Boolean {
        return try {
            val value = db.competitionQueriesQueries.getMeta("mode_selected").executeAsOneOrNull()
            value == "true"
        } catch (e: Exception) {
            false
        }
    }

    fun markModeSelected() {
        try {
            db.competitionQueriesQueries.setMeta("mode_selected", "true")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getMode(): AppMode = _currentMode.value

    fun isOnline(): Boolean = _currentMode.value == AppMode.ONLINE

    fun switch(mode: AppMode) {
        _currentMode.value = mode
        saveMode(mode)
    }

    private fun loadSavedMode(): AppMode {
        return try {
            val value = db.competitionQueriesQueries.getMeta("app_mode").executeAsOneOrNull()
            when (value) {
                "online" -> AppMode.ONLINE
                else -> AppMode.OFFLINE
            }
        } catch (e: Exception) {
            AppMode.OFFLINE
        }
    }

    private fun saveMode(mode: AppMode) {
        try {
            db.competitionQueriesQueries.setMeta("app_mode", mode.name.lowercase())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
