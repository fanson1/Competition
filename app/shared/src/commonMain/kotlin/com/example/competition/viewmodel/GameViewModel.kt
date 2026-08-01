package com.example.competition.viewmodel

import com.example.competition.data.GamePreferences
import com.example.competition.data.LevelConfigs
import com.example.competition.data.QuestionLoader
import com.example.competition.data.QuestionRepository
import com.example.competition.model.Difficulty
import com.example.competition.model.ChallengeRecord
import com.example.competition.model.GameState
import com.example.competition.model.GameStatus
import com.example.competition.model.LeaderboardEntry
import com.example.competition.model.QuestionWithShuffledOptions
import com.example.competition.PlatformUtils
import com.example.competition.repository.bridge.RepositoryBridge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameViewModel() {
    private val _uiState = MutableStateFlow(GameState())
    val uiState: StateFlow<GameState> = _uiState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Default)
    private var timerJob: Job? = null

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Challenge mode tracking
    private var _challengeTarget = MutableStateFlow<LeaderboardEntry?>(null)
    val challengeTarget: StateFlow<LeaderboardEntry?> = _challengeTarget.asStateFlow()

    private var currentRoundQuestionIds: List<Int> = emptyList()

    suspend fun loadQuestions() {
        _isLoading.value = true
        QuestionLoader.loadAndInitialize()
        val savedState = GamePreferences.loadProgress()
        _uiState.value = savedState
        _isLoading.value = false
    }

    fun startLevel(level: Int) {
        val config = LevelConfigs.getConfig(level)
        val questions = QuestionRepository.getQuestionsForLevel(config)
        if (questions.isEmpty()) return
        currentRoundQuestionIds = questions.map { it.question.id }

        val previousState = _uiState.value
        _uiState.value = GameState(
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
        startTimer()
    }

    fun startGame() {
        startLevel(_uiState.value.maxUnlockedLevel.coerceAtLeast(1))
    }

    fun startChallenge(level: Int, target: LeaderboardEntry) {
        _challengeTarget.value = target
        startLevel(level)
    }

    fun selectAnswer(answerIndex: Int) {
        val state = _uiState.value
        if (state.status != GameStatus.PLAYING || state.isAnswerRevealed) return

        stopTimer()

        val questionWithOpts = state.questions[state.currentQuestionIndex]
        val isCorrect = answerIndex == questionWithOpts.shuffledCorrectIndex
        val timeUsed = questionWithOpts.timeLimitSeconds - state.timeRemaining

        if (isCorrect) {
            val baseScore = when (questionWithOpts.difficulty) {
                Difficulty.EASY -> 100
                Difficulty.MEDIUM -> 200
                Difficulty.HARD -> 300
            }
            val timeBonus = ((state.timeRemaining / questionWithOpts.timeLimitSeconds) * 50).toInt()
            val streakMultiplier = 1 + (state.streak * 0.2f)
            val totalPoints = ((baseScore + timeBonus) * streakMultiplier).toInt()

            val newStreak = state.streak + 1
            val newLevelCorrect = state.levelCorrectCount + 1
            _uiState.value = state.copy(
                status = GameStatus.CORRECT_ANSWER,
                selectedAnswerIndex = answerIndex,
                isAnswerRevealed = true,
                score = state.score + totalPoints,
                levelScore = state.levelScore + totalPoints,
                streak = newStreak,
                maxStreak = maxOf(state.maxStreak, newStreak),
                correctCount = state.correctCount + 1,
                levelCorrectCount = newLevelCorrect,
                totalCorrectCount = state.totalCorrectCount + 1
            )

            scope.launch {
                delay(1200)
                advanceToNextQuestion()
            }
        } else {
            val title = getPlayerTitle(state)
            _uiState.value = state.copy(
                status = GameStatus.WRONG_ANSWER,
                selectedAnswerIndex = answerIndex,
                isAnswerRevealed = true,
                streak = 0,
                wrongCount = state.wrongCount + 1,
                levelWrongCount = state.levelWrongCount + 1,
                playerTitle = title
            )

            scope.launch {
                delay(2000)
                val finalState = _uiState.value.copy(status = GameStatus.GAME_OVER)
                _uiState.value = finalState
                saveProgress(finalState)
            }
        }
    }

    fun tick(deltaTime: Float) {
        val state = _uiState.value
        if (state.status != GameStatus.PLAYING) return

        val newTime = (state.timeRemaining - deltaTime).coerceAtLeast(0f)
        _uiState.value = state.copy(timeRemaining = newTime)

        if (newTime <= 0f) {
            handleTimeout()
        }
    }

    private fun handleTimeout() {
        stopTimer()
        val state = _uiState.value
        val title = getPlayerTitle(state)
        _uiState.value = state.copy(
            status = GameStatus.TIMEOUT,
            isAnswerRevealed = true,
            streak = 0,
            wrongCount = state.wrongCount + 1,
            levelWrongCount = state.levelWrongCount + 1,
            playerTitle = title
        )

        scope.launch {
            delay(2000)
            val finalState = _uiState.value.copy(status = GameStatus.GAME_OVER)
            _uiState.value = finalState
            saveProgress(finalState)
        }
    }

    private fun advanceToNextQuestion() {
        val state = _uiState.value
        val nextIndex = state.currentQuestionIndex + 1

        if (nextIndex >= state.questions.size) {
            val newCompletedLevels = state.completedLevels + state.currentLevel
            val newMaxUnlocked = maxOf(state.maxUnlockedLevel, state.currentLevel + 1)

            val finalState = state.copy(
                status = GameStatus.LEVEL_COMPLETE,
                currentQuestionIndex = nextIndex,
                playerTitle = "level_${state.currentLevel}_title",
                completedLevels = newCompletedLevels,
                maxUnlockedLevel = newMaxUnlocked
            )
            _uiState.value = finalState
            saveProgress(finalState)
        } else {
            _uiState.value = state.copy(
                status = GameStatus.PLAYING,
                currentQuestionIndex = nextIndex,
                selectedAnswerIndex = null,
                isAnswerRevealed = false,
                timeRemaining = state.questions[nextIndex].timeLimitSeconds.toFloat()
            )
            startTimer()
        }
    }

    fun startNextLevel() {
        val state = _uiState.value
        val nextLevel = state.currentLevel + 1
        if (nextLevel <= 10) {
            startLevel(nextLevel)
        } else {
            val finalState = state.copy(
                status = GameStatus.GAME_OVER,
                playerTitle = "player_title_grandmaster"
            )
            _uiState.value = finalState
            saveProgress(finalState)
        }
    }

    fun retryCurrentLevel() {
        startLevel(_uiState.value.currentLevel)
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

    private fun saveChallengeResult(state: GameState) {
        val target = _challengeTarget.value ?: return
        if (state.status != GameStatus.GAME_OVER && state.status != GameStatus.LEVEL_COMPLETE) return

        val challenger = RepositoryBridge.user().getCurrentUser() ?: return
        val isWin = state.levelScore > target.score

        val record = ChallengeRecord(
            id = "challenge_${PlatformUtils.currentTimeMillis()}_${(1000..9999).random()}",
            challengerId = challenger.id,
            challengerName = challenger.nickname,
            targetId = target.userId,
            targetName = target.nickname,
            level = state.currentLevel,
            challengerScore = state.levelScore,
            targetScore = target.score,
            isWin = isWin,
            timestamp = PlatformUtils.currentTimeMillis()
        )
        scope.launch { RepositoryBridge.challenge().addChallenge(record) }
    }

    fun clearChallengeTarget() {
        _challengeTarget.value = null
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
            scope.launch {
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
        timerJob = scope.launch {
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

    fun resetGame() {
        stopTimer()
        _uiState.value = GamePreferences.loadProgress()
    }

    fun reloadProgress() {
        stopTimer()
        _uiState.value = GamePreferences.loadProgress()
    }

    suspend fun syncLeaderboardFromServer() {
        try {
            RepositoryBridge.leaderboard().getLeaderboard()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
