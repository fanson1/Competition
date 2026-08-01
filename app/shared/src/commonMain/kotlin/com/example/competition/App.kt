package com.example.competition

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.example.competition.api.ApiClient
import com.example.competition.data.GamePreferences
import com.example.competition.data.UserManager
import com.example.competition.model.GameStatus
import com.example.competition.model.LeaderboardEntry
import com.example.competition.repository.ModeRouter
import com.example.competition.repository.bridge.RepositoryBridge
import com.example.competition.ui.screens.*
import com.example.competition.ui.theme.QuizTheme
import com.example.competition.ui.theme.Gold
import com.example.competition.viewmodel.GameViewModel
import com.example.competition.api.dto.ProfileDto
import com.example.competition.api.dto.UserDto
import competition.app.shared.generated.resources.Res
import competition.app.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

private suspend fun doOnlineLogin(authResp: com.example.competition.api.dto.AuthResponse, viewModel: GameViewModel) {
    ApiClient.setToken(authResp.token)
    val dto = authResp.user
    val user = com.example.competition.model.User(
        id = dto.id, username = dto.username,
        passwordHash = "", nickname = dto.nickname,
        avatarEmoji = dto.avatarEmoji,
        createdAt = dto.createdAt, lastLoginAt = dto.lastLoginAt
    )
    RepositoryBridge.getRemoteUser().loadFromRemote(dto)
    UserManager.setCurrentUser(user)

    // Sync profile from server
    ApiClient.getMe().onSuccess { me ->
        val profile = me.profile
        if (profile != null) {
            val userProfile = com.example.competition.model.UserProfile(
                user = user,
                totalScore = profile.totalScore,
                maxLevel = profile.maxLevel,
                totalCorrectCount = profile.totalCorrectCount,
                totalGamesPlayed = profile.totalGamesPlayed,
                maxStreak = profile.maxStreak,
                levelScores = parseLevelMap(profile.levelScores),
                levelCorrectCounts = parseLevelMap(profile.levelCorrectCounts),
                completedLevels = profile.completedLevels.toSet()
            )
            RepositoryBridge.getRemoteUser().loadProfile(userProfile)
            UserManager.setCurrentProfile(userProfile)
            GamePreferences.saveProgressFromProfile(userProfile)
        }
    }

    // Sync leaderboard from server
    viewModel.syncLeaderboardFromServer()
}

private fun parseLevelMap(serialized: String): Map<Int, Int> {
    if (serialized.isBlank()) return emptyMap()
    return serialized.split(",").mapNotNull { entry ->
        val parts = entry.split(":")
        if (parts.size == 2) parts[0].toIntOrNull() to parts[1].toIntOrNull()
        else null
    }.filter { it.second != null }.map { it.first!! to it.second!! }.toMap()
}

enum class Screen {
    MODE_SELECT, LOGIN, HOME, PROFILE, LEADERBOARD, CHALLENGE, CHALLENGE_HERO
}

@Composable
fun App() {
    QuizTheme {
        val viewModel = remember { GameViewModel() }
        val uiState by viewModel.uiState.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()

        var currentScreen by remember { mutableStateOf(Screen.MODE_SELECT) }
        var challengeLevel by remember { mutableStateOf(1) }
        var challengeTarget by remember { mutableStateOf<LeaderboardEntry?>(null) }

        val currentUser = remember { mutableStateOf(UserManager.getCurrentUser()) }
        val isLoggedIn = remember { mutableStateOf(UserManager.isLoggedIn()) }

        LaunchedEffect(Unit) {
            try {
                UserManager.init()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            ApiClient.init()
            ModeRouter.init()

            // Skip mode selection if already chosen on a previous launch
            if (ModeRouter.isModeSelected() && currentScreen == Screen.MODE_SELECT) {
                currentScreen = Screen.LOGIN
            }

            try {
                viewModel.loadQuestions()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Auto-login with stored token if in online mode
            if (ModeRouter.isOnline() && ApiClient.isAuthenticated()) {
                ApiClient.verifyToken().onSuccess { verify ->
                    if (verify.valid && verify.user != null) {
                        val remoteUser = RepositoryBridge.getRemoteUser()
                        remoteUser.loadFromRemote(verify.user)
                        currentUser.value = remoteUser.getCurrentUser()
                        isLoggedIn.value = true
                        GamePreferences.setUserId(currentUser.value?.id)
                        // Load server profile into local game progress
                        ApiClient.getMe().onSuccess { me ->
                            val profile = me.profile
                            if (profile != null) {
                                val userProfile = com.example.competition.model.UserProfile(
                                    user = currentUser.value!!,
                                    totalScore = profile.totalScore,
                                    maxLevel = profile.maxLevel,
                                    totalCorrectCount = profile.totalCorrectCount,
                                    totalGamesPlayed = profile.totalGamesPlayed,
                                    maxStreak = profile.maxStreak,
                                    levelScores = parseLevelMap(profile.levelScores),
                                    levelCorrectCounts = parseLevelMap(profile.levelCorrectCounts),
                                    completedLevels = profile.completedLevels.toSet()
                                )
                                remoteUser.loadProfile(userProfile)
                                UserManager.setCurrentProfile(userProfile)
                                GamePreferences.saveProgressFromProfile(userProfile)
                            }
                        }
                        viewModel.reloadProgress()
                        currentScreen = Screen.HOME
                    }
                }
            }

            if (isLoggedIn.value) {
                GamePreferences.setUserId(currentUser.value?.id)
                viewModel.reloadProgress()
                currentScreen = Screen.HOME
            }
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Gold)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(Res.string.app_loading),
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            } else {
                when (currentScreen) {
                    Screen.MODE_SELECT -> {
                        ModeSelectionScreen(
                            onComplete = {
                                currentScreen = Screen.LOGIN
                            }
                        )
                    }

                    Screen.LOGIN -> {
                        LoginScreen(
                            onLogin = { username, password ->
                                if (ModeRouter.isOnline()) {
                                    ApiClient.login(username, password).map { authResp ->
                                        doOnlineLogin(authResp, viewModel)
                                    }.map { }
                                } else {
                                    UserManager.login(username, password).map { }
                                }
                            },
                            onRegister = { username, password, nickname ->
                                if (ModeRouter.isOnline()) {
                                    ApiClient.register(username, password, nickname).map { authResp ->
                                        doOnlineLogin(authResp, viewModel)
                                    }.map { }
                                } else {
                                    UserManager.register(username, password, nickname).map { }
                                }
                            },
                            onLoginSuccess = {
                                currentUser.value = UserManager.getCurrentUser()
                                isLoggedIn.value = true
                                GamePreferences.setUserId(currentUser.value?.id)
                                viewModel.reloadProgress()
                                currentScreen = Screen.HOME
                            }
                        )
                    }

                    Screen.HOME -> {
                        HomeScreen(
                            gameState = uiState,
                            user = currentUser.value,
                            profile = UserManager.getCurrentProfile(),
                            onStartGame = { viewModel.startGame() },
                            onStartLevel = { level -> viewModel.startLevel(level) },
                            onNavigateToProfile = { currentScreen = Screen.PROFILE },
                            onNavigateToLeaderboard = { currentScreen = Screen.LEADERBOARD },
                            onNavigateToChallenge = { currentScreen = Screen.CHALLENGE },
                            onNavigateToChallengeHero = { currentScreen = Screen.CHALLENGE_HERO }
                        )
                    }

                    Screen.PROFILE -> {
                        ProfileScreen(
                            user = currentUser.value!!,
                            profile = UserManager.getCurrentProfile(),
                            onProfileUpdated = {
                                currentUser.value = UserManager.getCurrentUser()
                            },
                            onLogout = {
                                UserManager.logout()
                                GamePreferences.setUserId(null)
                                viewModel.resetGame()
                                isLoggedIn.value = false
                                currentUser.value = null
                                currentScreen = Screen.LOGIN
                            },
                            onBack = { currentScreen = Screen.HOME }
                        )
                    }

                    Screen.LEADERBOARD -> {
                        LeaderboardScreen(
                            onBack = { currentScreen = Screen.HOME }
                        )
                    }

                    Screen.CHALLENGE -> {
                        ChallengeScreen(
                            user = currentUser.value!!,
                            onBack = { currentScreen = Screen.HOME },
                            onStartChallenge = { level, target ->
                                challengeLevel = level
                                challengeTarget = target
                                viewModel.startChallenge(level, target)
                                currentScreen = Screen.HOME
                            }
                        )
                    }

                    Screen.CHALLENGE_HERO -> {
                        ChallengeHeroScreen(
                            user = currentUser.value!!,
                            onBack = { currentScreen = Screen.HOME }
                        )
                    }
                }

                // Game screens overlay
                when (uiState.status) {
                    GameStatus.PLAYING,
                    GameStatus.CORRECT_ANSWER,
                    GameStatus.WRONG_ANSWER,
                    GameStatus.TIMEOUT -> {
                        QuizScreen(
                            gameState = uiState,
                            onAnswerSelected = { index -> viewModel.selectAnswer(index) }
                        )
                    }

                    GameStatus.LEVEL_COMPLETE -> {
                        val target by viewModel.challengeTarget.collectAsState()
                        LevelCompleteScreen(
                            gameState = uiState,
                            onNextLevel = { viewModel.startNextLevel() },
                            onRetryLevel = { viewModel.retryCurrentLevel() },
                            onBackToHome = {
                                viewModel.clearChallengeTarget()
                                viewModel.resetGame()
                                currentScreen = Screen.HOME
                            },
                            challengeTarget = target
                        )
                    }

                    GameStatus.GAME_OVER -> {
                        val target by viewModel.challengeTarget.collectAsState()
                        ResultScreen(
                            gameState = uiState,
                            onRetryLevel = { viewModel.retryCurrentLevel() },
                            onBackToHome = {
                                viewModel.clearChallengeTarget()
                                viewModel.resetGame()
                                currentScreen = Screen.HOME
                            },
                            challengeTarget = target
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}
