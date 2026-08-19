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
import com.example.competition.mvi.MviViewModel
import com.example.competition.repository.bridge.RepositoryBridge
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

class GameViewModel : MviViewModel<GameUiState, GameIntent, GameEffect>(GameUiState()) {

    private var timerJob: Job? = null
    private var currentRoundQuestionIds: List<Int> = emptyList()

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
            e.printStackTrace()
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
                    playerTitle = "",
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
        val timeUsed = questionWithOpts.timeLimitSeconds - game.timeRemaining

        if (isCorrect) {
            val baseScore = when (questionWithOpts.difficulty) {
                Difficulty.EASY -> 100
                Difficulty.MEDIUM -> 200
                Difficulty.HARD -> 300
            }
            val timeBonus = ((game.timeRemaining / questionWithOpts.timeLimitSeconds) * 50).toInt()
            val streakMultiplier = 1 + (game.streak * 0.2f)
            val totalPoints = ((baseScore + timeBonus) * streakMultiplier).toInt()

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
                delay(1200)
                advanceToNextQuestion()
            }
        } else {
            val title = getPlayerTitle(game)
            setState {
                it.copy(
                    game = it.game.copy(
                        status = GameStatus.WRONG_ANSWER,
                        selectedAnswerIndex = answerIndex,
                        isAnswerRevealed = true,
                        streak = 0,
                        wrongCount = it.game.wrongCount + 1,
                        levelWrongCount = it.game.levelWrongCount + 1,
                        playerTitle = title
                    )
                )
            }

            launch {
                delay(2000)
                val finalState = state.value.game.copy(status = GameStatus.GAME_OVER)
                setState { it.copy(game = finalState) }
                saveProgress(finalState)
                emit(GameEffect.GameOver)
            }
        }
    }

    private fun tick(deltaTime: Float) {
        val game = state.value.game
        if (game.status != GameStatus.PLAYING) return

        val newTime = (game.timeRemaining - deltaTime).coerceAtLeast(0f)
        setState { it.copy(game = it.game.copy(timeRemaining = newTime)) }

        if (newTime <= 0f) {
            handleTimeout()
        }
    }

    private fun handleTimeout() {
        stopTimer()
        val game = state.value.game
        val title = getPlayerTitle(game)
        setState {
            it.copy(
                game = it.game.copy(
                    status = GameStatus.TIMEOUT,
                    isAnswerRevealed = true,
                    streak = 0,
                    wrongCount = it.game.wrongCount + 1,
                    levelWrongCount = it.game.levelWrongCount + 1,
                    playerTitle = title
                )
            )
        }

        launch {
            delay(2000)
            val finalState = state.value.game.copy(status = GameStatus.GAME_OVER)
            setState { it.copy(game = finalState) }
            saveProgress(finalState)
            emit(GameEffect.GameOver)
        }
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
                playerTitle = "level_${game.currentLevel}_title",
                completedLevels = newCompletedLevels,
                maxUnlockedLevel = newMaxUnlocked
            )
            setState { it.copy(game = finalState) }
            saveProgress(finalState)
            emit(GameEffect.LevelComplete)
        } else {
            setState {
                it.copy(
                    game = it.game.copy(
                        status = GameStatus.PLAYING,
                        currentQuestionIndex = nextIndex,
                        selectedAnswerIndex = null,
                        isAnswerRevealed = false,
                        timeRemaining = it.game.questions[nextIndex].timeLimitSeconds.toFloat()
                    )
                )
            }
            startTimer()
        }
    }

    private fun startNextLevel() {
        val game = state.value.game
        val nextLevel = game.currentLevel + 1
        if (nextLevel <= 10) {
            startLevel(nextLevel)
        } else {
            val finalState = game.copy(
                status = GameStatus.GAME_OVER,
                playerTitle = "player_title_grandmaster"
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
            e.printStackTrace()
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
            e.printStackTrace()
        }
    }

    private fun startTimer() {
        stopTimer()
        timerJob = launch {
            val frameTime = 50L
            while (true) {
                delay(frameTime)
                tick(frameTime / 1000f)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun getPlayerTitle(state: GameState): String {
        return when {
            state.levelCorrectCount >= 8 -> "player_title_rampant"
            state.levelCorrectCount >= 6 -> "player_title_excellent"
            state.levelCorrectCount >= 4 -> "player_title_notable"
            state.levelCorrectCount >= 2 -> "player_title_showing_potential"
            else -> "player_title_courageous"
        }
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
            e.printStackTrace()
        }
    }
}
