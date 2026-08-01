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
    val playerTitle: String = "",
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

val GameStatus.isGameOver: Boolean
    get() = this == GameStatus.GAME_OVER

val GameStatus.isAnswerVisible: Boolean
    get() = this == GameStatus.CORRECT_ANSWER || this == GameStatus.WRONG_ANSWER || this == GameStatus.TIMEOUT

val GameStatus.isLevelComplete: Boolean
    get() = this == GameStatus.LEVEL_COMPLETE
