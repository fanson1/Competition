package com.example.competition.model

data class GameState(
    val status: GameStatus = GameStatus.IDLE,
    val currentQuestionIndex: Int = 0,
    val questions: List<QuestionWithShuffledOptions> = emptyList(),
    val score: Int = 0,
    val levelScore: Int = 0,
    val streak: Int = 0,
    val maxStreak: Int = 0,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val selectedAnswerIndex: Int? = null,
    val isAnswerRevealed: Boolean = false,
    val timeRemaining: Float = 0f,
    val playerTitle: PlayerTitle? = null,
    val currentLevel: Int = 1,
    val maxUnlockedLevel: Int = 1,
    val levelCorrectCount: Int = 0,
    val levelWrongCount: Int = 0,
    val totalCorrectCount: Int = 0,
    val completedLevels: Set<Int> = emptySet()
)

enum class GameStatus {
    IDLE,
    PLAYING,
    CORRECT_ANSWER,
    WRONG_ANSWER,
    TIMEOUT,
    LEVEL_COMPLETE,
    GAME_OVER
}

/**
 * Player performance title earned within a single level run.
 *
 * Using an enum instead of the previous raw string keys keeps the domain
 * model type-safe and makes the game rules discoverable in one place.
 */
enum class PlayerTitle(val key: String) {
    RAMPANT("player_title_rampant"),
    EXCELLENT("player_title_excellent"),
    NOTABLE("player_title_notable"),
    SHOWING_POTENTIAL("player_title_showing_potential"),
    COURAGEOUS("player_title_courageous"),
    GRANDMASTER("player_title_grandmaster");

    companion object {
        /** Best-effort reverse lookup for data persisted with legacy string keys. */
        fun fromKey(key: String?): PlayerTitle? = values().firstOrNull { it.key == key }

        /** Titles awarded by number of correct answers within the current level. */
        fun fromCorrectCount(count: Int): PlayerTitle = when {
            count >= 8 -> RAMPANT
            count >= 6 -> EXCELLENT
            count >= 4 -> NOTABLE
            count >= 2 -> SHOWING_POTENTIAL
            else -> COURAGEOUS
        }
    }
}

val GameStatus.isGameOver: Boolean
    get() = this == GameStatus.GAME_OVER

val GameStatus.isAnswerVisible: Boolean
    get() = this == GameStatus.CORRECT_ANSWER || this == GameStatus.WRONG_ANSWER || this == GameStatus.TIMEOUT

val GameStatus.isLevelComplete: Boolean
    get() = this == GameStatus.LEVEL_COMPLETE
