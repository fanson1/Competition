package com.example.competition.presentation.game

import com.example.competition.PlatformUtils
import com.example.competition.data.GamePreferences
import com.example.competition.data.LevelConfigs
import com.example.competition.data.QuestionLoader
import com.example.competition.data.QuestionRepository
import com.example.competition.model.ChallengeRecord
import com.example.competition.model.Difficulty
import com.example.competition.model.GameState
import com.example.competition.model.GameStatus
import com.example.competition.model.LeaderboardEntry
import com.example.competition.model.PlayerTitle
import com.example.competition.mvi.MviViewModel
import com.example.competition.repository.bridge.RepositoryBridge
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

internal const val ANSWER_FEEDBACK_DELAY_MS = 1200L
internal const val FAILURE_FEEDBACK_DELAY_MS = 2000L
internal const val TIMER_TICK_MS = 50L

class GameViewModel : MviViewModel<GameUiState, GameIntent, GameEffect>(GameUiState()) {

    private var timerJob: Job? = null
    private var currentRoundQuestionIds: List<Int> = emptyList()

    /** Absolute wall-clock deadline (ms) of the current question; 0 when idle. */
    private var questionDeadline: Long = 0L

    override fun onIntent(intent: GameIntent) {
        when (intent) {
            GameIntent.LoadQuestions -> launch { loadQuestions() }
            is GameIntent.StartLevel -> startLevel(intent.level)
            GameIntent.StartGame -> startGame()
            is GameIntent.StartChallenge -> startChallenge(intent.level, intent.target)
            is GameIntent.SelectAnswer -> selectAnswer(intent.answerIndex)
            GameIntent.StartNextLevel -> startNextLevel()
            GameIntent.RetryCurrentLevel -> retryCurrentLevel()
            GameIntent.ResetGame -> resetGame()
            GameIntent.ClearChallengeTarget -> clearChallengeTarget()
            GameIntent.ReloadProgress -> reloadProgress()
            GameIntent.SyncLeaderboardFromServer -> launch { syncLeaderboardFromServer() }
        }
    }

    private suspend fun loadQuestions() {
        try {
            setState { it.copy(isLoading = true) }
            QuestionLoader.loadAndInitialize()
            val savedState = GamePreferences.loadProgress()
            setState { it.copy(game = savedState, isLoading = false) }
        } catch (e: Exception) {
            setState { it.copy(isLoading = false) }
        }
    }

    private fun startLevel(level: Int) {
        val config = LevelConfigs.getConfig(level)
        val questions = QuestionRepository.getQuestionsForLevel(config)
        if (questions.isEmpty()) return
        currentRoundQuestionIds = questions.map { it.question.id }

        val previousState = state.value.game
        setState {
            it.copy(
                game = GameState(
                    status = GameStatus.PLAYING,
                    questions = questions,
                    currentQuestionIndex = 0,
                    score = previousState.score,
                    levelScore = 0,
                    streak = 0,
                    maxStreak = previousState.maxStreak,
                    correctCount = 0,
                    wrongCount = 0,
                    selectedAnswerIndex = null,
                    isAnswerRevealed = false,
                    timeRemaining = questions.first().timeLimitSeconds.toFloat(),
                    playerTitle = null,
                    currentLevel = level,
                    maxUnlockedLevel = previousState.maxUnlockedLevel,
                    levelCorrectCount = 0,
                    levelWrongCount = 0,
                    totalCorrectCount = previousState.totalCorrectCount,
                    completedLevels = previousState.completedLevels
                )
            )
        }
        startTimer()
    }

    private fun startGame() {
        startLevel(state.value.game.maxUnlockedLevel.coerceAtLeast(1))
    }

    private fun startChallenge(level: Int, target: LeaderboardEntry) {
        setState { it.copy(challengeTarget = target) }
        startLevel(level)
    }

    private fun selectAnswer(answerIndex: Int) {
        val game = state.value.game
        if (game.status != GameStatus.PLAYING || game.isAnswerRevealed) return

        stopTimer()

        val questionWithOpts = game.questions[game.currentQuestionIndex]
        val isCorrect = answerIndex == questionWithOpts.shuffledCorrectIndex

        if (isCorrect) {
            val totalPoints = pointsFor(questionWithOpts.difficulty, game.timeRemaining, game.streak)

            val newStreak = game.streak + 1
            val newLevelCorrect = game.levelCorrectCount + 1
            setState {
                it.copy(
                    game = it.game.copy(
                        status = GameStatus.CORRECT_ANSWER,
                        selectedAnswerIndex = answerIndex,
                        isAnswerRevealed = true,
                        score = it.game.score + totalPoints,
                        levelScore = it.game.levelScore + totalPoints,
                        streak = newStreak,
                        maxStreak = maxOf(it.game.maxStreak, newStreak),
                        correctCount = it.game.correctCount + 1,
                        levelCorrectCount = newLevelCorrect,
                        totalCorrectCount = it.game.totalCorrectCount + 1
                    )
                )
            }

            launch {
                delay(ANSWER_FEEDBACK_DELAY_MS)
                advanceToNextQuestion()
            }
        } else {
            setState {
                it.copy(
                    game = it.game.copy(
                        status = GameStatus.WRONG_ANSWER,
                        selectedAnswerIndex = answerIndex,
                        isAnswerRevealed = true,
                        streak = 0,
                        wrongCount = it.game.wrongCount + 1,
                        levelWrongCount = it.game.levelWrongCount + 1,
                        playerTitle = PlayerTitle.fromCorrectCount(it.game.levelCorrectCount)
                    )
                )
            }
            scheduleGameOver()
        }
    }

    private fun scheduleGameOver() {
        launch {
            delay(FAILURE_FEEDBACK_DELAY_MS)
            val finalState = state.value.game.copy(status = GameStatus.GAME_OVER)
            setState { it.copy(game = finalState) }
            saveProgress(finalState)
            emit(GameEffect.GameOver)
        }
    }

    private fun tick() {
        val game = state.value.game
        if (game.status != GameStatus.PLAYING) return

        val newTime = if (questionDeadline > 0L) {
            ((questionDeadline - PlatformUtils.currentTimeMillis()) / 1000f).coerceIn(0f, game.timeRemaining.coerceAtLeast(0f))
        } else {
            0f
        }
        setState { it.copy(game = it.game.copy(timeRemaining = newTime)) }

        if (newTime <= 0f) {
            handleTimeout()
        }
    }

    private fun handleTimeout() {
        stopTimer()
        questionDeadline = 0L
        val game = state.value.game
        setState {
            it.copy(
                game = it.game.copy(
                    status = GameStatus.TIMEOUT,
                    isAnswerRevealed = true,
                    streak = 0,
                    wrongCount = it.game.wrongCount + 1,
                    levelWrongCount = it.game.levelWrongCount + 1,
                    playerTitle = PlayerTitle.fromCorrectCount(it.game.levelCorrectCount)
                )
            )
        }
        scheduleGameOver()
    }

    private fun advanceToNextQuestion() {
        val game = state.value.game
        val nextIndex = game.currentQuestionIndex + 1

        if (nextIndex >= game.questions.size) {
            val newCompletedLevels = game.completedLevels + game.currentLevel
            val newMaxUnlocked = maxOf(game.maxUnlockedLevel, game.currentLevel + 1)

            val finalState = game.copy(
                status = GameStatus.LEVEL_COMPLETE,
                currentQuestionIndex = nextIndex,
                completedLevels = newCompletedLevels,
                maxUnlockedLevel = newMaxUnlocked
            )
            setState { it.copy(game = finalState) }
            saveProgress(finalState)
            emit(GameEffect.LevelComplete)
        } else {
            val timeLimit = game.questions[nextIndex].timeLimitSeconds.toFloat()
            setState {
                it.copy(
                    game = it.game.copy(
                        status = GameStatus.PLAYING,
                        currentQuestionIndex = nextIndex,
                        selectedAnswerIndex = null,
                        isAnswerRevealed = false,
                        timeRemaining = timeLimit
                    )
                )
            }
            startTimer()
        }
    }

    private fun startNextLevel() {
        val game = state.value.game
        val nextLevel = game.currentLevel + 1
        if (nextLevel <= LevelConfigs.levels.size) {
            startLevel(nextLevel)
        } else {
            val finalState = game.copy(
                status = GameStatus.GAME_OVER,
                playerTitle = PlayerTitle.GRANDMASTER
            )
            setState { it.copy(game = finalState) }
            saveProgress(finalState)
            emit(GameEffect.GameOver)
        }
    }

    private fun retryCurrentLevel() {
        startLevel(state.value.game.currentLevel)
    }

    private fun saveProgress(state: GameState) {
        try {
            QuestionRepository.markQuestionsAsUsed(currentRoundQuestionIds)
            GamePreferences.saveProgress(state)
            updateLeaderboardAndProfile(state)
            saveChallengeResult(state)
        } catch (e: Exception) {
            // Non-critical: persistence failures should not break the game flow.
        }
    }

    private fun saveChallengeResult(gameState: GameState) {
        val target = state.value.challengeTarget ?: return
        if (gameState.status != GameStatus.GAME_OVER && gameState.status != GameStatus.LEVEL_COMPLETE) return

        val challenger = RepositoryBridge.user().getCurrentUser() ?: return
        val isWin = gameState.levelScore > target.score

        val record = ChallengeRecord(
            id = "challenge_${PlatformUtils.currentTimeMillis()}_${(1000..9999).random()}",
            challengerId = challenger.id,
            challengerName = challenger.nickname,
            targetId = target.userId,
            targetName = target.nickname,
            level = gameState.currentLevel,
            challengerScore = gameState.levelScore,
            targetScore = target.score,
            isWin = isWin,
            timestamp = PlatformUtils.currentTimeMillis()
        )
        launch { RepositoryBridge.challenge().addChallenge(record) }
    }

    private fun clearChallengeTarget() {
        setState { it.copy(challengeTarget = null) }
    }

    private fun updateLeaderboardAndProfile(state: GameState) {
        try {
            val user = RepositoryBridge.user().getCurrentUser() ?: return
            val level = state.currentLevel
            val levelScore = state.levelScore

            val entry = LeaderboardEntry(
                userId = user.id,
                username = user.username,
                nickname = user.nickname,
                avatarEmoji = user.avatarEmoji,
                score = levelScore,
                level = level,
                correctCount = state.levelCorrectCount,
                timestamp = PlatformUtils.currentTimeMillis()
            )
            launch {
                RepositoryBridge.user().updateProfileStats(
                    score = levelScore,
                    level = level,
                    correctCount = state.levelCorrectCount,
                    streak = state.maxStreak,
                    completedLevels = state.completedLevels
                )
                RepositoryBridge.leaderboard().updateLeaderboard(entry)
            }
        } catch (e: Exception) {
            // Non-critical: leaderboard/profile stats are best-effort.
        }
    }

    private fun startTimer() {
        stopTimer()
        val remaining = state.value.game.timeRemaining.coerceAtLeast(0f)
        questionDeadline = PlatformUtils.currentTimeMillis() + (remaining * 1000f).toLong()
        timerJob = launch {
            while (true) {
                delay(TIMER_TICK_MS)
                tick()
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        questionDeadline = 0L
    }

    private fun resetGame() {
        stopTimer()
        setState { it.copy(game = GamePreferences.loadProgress()) }
    }

    private fun reloadProgress() {
        stopTimer()
        setState { it.copy(game = GamePreferences.loadProgress()) }
    }

    private suspend fun syncLeaderboardFromServer() {
        try {
            RepositoryBridge.leaderboard().getLeaderboard()
        } catch (e: Exception) {
            // Swallow: leaderboard sync is best-effort on login.
        }
    }

    private fun pointsFor(difficulty: Difficulty, timeRemaining: Float, streak: Int): Int {
        val baseScore = when (difficulty) {
            Difficulty.EASY -> 100
            Difficulty.MEDIUM -> 200
            Difficulty.HARD -> 300
        }
        val timeBonus = (timeRemaining.coerceIn(0f, 1f) * 50).toInt()
        val streakMultiplier = 1 + streak * 0.2f
        return ((baseScore + timeBonus) * streakMultiplier).toInt()
    }
}