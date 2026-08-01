package com.example.competition.data

import com.example.competition.db.DatabaseManager
import com.example.competition.model.GameState
import com.example.competition.model.GameStatus
import com.example.competition.model.UserProfile

object GamePreferences {
    private var userId: String? = null

    private val db get() = DatabaseManager.db

    fun setUserId(userId: String?) {
        this.userId = userId
    }

    fun saveProgress(state: GameState) {
        try {
            val id = userId ?: return
            db.competitionQueriesQueries.insertOrReplaceGameProgress(
                userId = id,
                score = state.score.toLong(),
                maxUnlockedLevel = state.maxUnlockedLevel.toLong(),
                totalCorrectCount = state.totalCorrectCount.toLong(),
                maxStreak = state.maxStreak.toLong()
            )
            // Save completed levels
            db.competitionQueriesQueries.deleteGameProgressCompletedLevels(id)
            for (level in state.completedLevels) {
                db.competitionQueriesQueries.insertGameProgressCompletedLevel(id, level.toLong())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveProgressFromProfile(profile: UserProfile) {
        try {
            val id = userId ?: return
            db.competitionQueriesQueries.insertOrReplaceGameProgress(
                userId = id,
                score = profile.totalScore.toLong(),
                maxUnlockedLevel = (profile.maxLevel + 1).toLong(),
                totalCorrectCount = profile.totalCorrectCount.toLong(),
                maxStreak = profile.maxStreak.toLong()
            )
            db.competitionQueriesQueries.deleteGameProgressCompletedLevels(id)
            for (level in profile.completedLevels) {
                db.competitionQueriesQueries.insertGameProgressCompletedLevel(id, level.toLong())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadProgress(): GameState {
        try {
            val id = userId ?: return GameState()
            val entity = db.competitionQueriesQueries.getGameProgress(id).executeAsOneOrNull() ?: return GameState()

            val completedLevels = db.competitionQueriesQueries.getGameProgressCompletedLevels(id)
                .executeAsList().map { it.toInt() }.toSet()

            return GameState(
                status = GameStatus.IDLE,
                score = entity.score.toInt(),
                maxUnlockedLevel = entity.maxUnlockedLevel.toInt(),
                totalCorrectCount = entity.totalCorrectCount.toInt(),
                maxStreak = entity.maxStreak.toInt(),
                completedLevels = completedLevels
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return GameState()
        }
    }
}
