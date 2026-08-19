package com.example.competition

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.competition.model.GameStatus
import com.example.competition.presentation.app.AppEffect
import com.example.competition.presentation.app.AppIntent
import com.example.competition.presentation.app.AppViewModel
import com.example.competition.presentation.app.Screen
import com.example.competition.presentation.game.GameEffect
import com.example.competition.presentation.game.GameIntent
import com.example.competition.presentation.game.GameViewModel
import com.example.competition.ui.MviEffectCollector
import com.example.competition.ui.components.QuizProgressBar
import com.example.competition.ui.components.ScreenBackground
import com.example.competition.ui.components.rememberPulseScale
import com.example.competition.ui.rememberViewModel
import com.example.competition.ui.screens.*
import com.example.competition.ui.theme.QuizPalette
import com.example.competition.ui.theme.QuizTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import competition.app.shared.generated.resources.Res
import competition.app.shared.generated.resources.app_logo
import competition.app.shared.generated.resources.*

@Composable
fun App() {
    QuizTheme {
        val appViewModel = rememberViewModel { AppViewModel() }
        val gameViewModel = rememberViewModel { GameViewModel() }
        val appState by appViewModel.state.collectAsState()
        val gameState by gameViewModel.state.collectAsState()

        LaunchedEffect(Unit) {
            appViewModel.dispatch(AppIntent.Init)
            gameViewModel.dispatch(GameIntent.LoadQuestions)
        }

        MviEffectCollector(appViewModel) { effect ->
            when (effect) {
                AppEffect.ReloadGameProgress -> gameViewModel.dispatch(GameIntent.ReloadProgress)
                AppEffect.SyncLeaderboard -> gameViewModel.dispatch(GameIntent.SyncLeaderboardFromServer)
            }
        }

        MviEffectCollector(gameViewModel) { effect ->
            when (effect) {
                GameEffect.GameOver -> Unit
                GameEffect.LevelComplete -> Unit
            }
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (gameState.isLoading) {
                AppLoadingScreen()
            } else {
                AnimatedContent(
                    targetState = appState.currentScreen,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300), initialAlpha = 0f) togetherWith
                            fadeOut(animationSpec = tween(200), targetAlpha = 0f)
                    },
                    label = "screen"
                ) { screen ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        when (screen) {
                            Screen.MODE_SELECT -> {
                                ModeSelectionScreen(
                                    onComplete = {
                                        appViewModel.dispatch(AppIntent.ModeSelectionComplete)
                                    }
                                )
                            }

                            Screen.LOGIN -> {
                                LoginScreen(
                                    onLoginSuccess = {
                                        appViewModel.dispatch(AppIntent.LoginSuccess)
                                    }
                                )
                            }

                            Screen.HOME -> {
                                HomeScreen(
                                    gameState = gameState.game,
                                    user = appState.user,
                                    profile = com.example.competition.data.UserManager.getCurrentProfile(),
                                    onStartGame = { gameViewModel.dispatch(GameIntent.StartGame) },
                                    onStartLevel = { level -> gameViewModel.dispatch(GameIntent.StartLevel(level)) },
                                    onNavigateToProfile = { appViewModel.dispatch(AppIntent.Navigate(Screen.PROFILE)) },
                                    onNavigateToLeaderboard = { appViewModel.dispatch(AppIntent.Navigate(Screen.LEADERBOARD)) },
                                    onNavigateToChallenge = { appViewModel.dispatch(AppIntent.Navigate(Screen.CHALLENGE)) },
                                    onNavigateToChallengeHero = { appViewModel.dispatch(AppIntent.Navigate(Screen.CHALLENGE_HERO)) }
                                )
                            }

                            Screen.PROFILE -> {
                                appState.user?.let { user ->
                                    ProfileScreen(
                                        user = user,
                                        profile = com.example.competition.data.UserManager.getCurrentProfile(),
                                        onProfileUpdated = {
                                            appViewModel.dispatch(AppIntent.RefreshUser)
                                        },
                                        onLogout = {
                                            appViewModel.dispatch(AppIntent.Logout)
                                        },
                                        onBack = {
                                            appViewModel.dispatch(AppIntent.Navigate(Screen.HOME))
                                        }
                                    )
                                }
                            }

                            Screen.LEADERBOARD -> {
                                LeaderboardScreen(
                                    onBack = {
                                        appViewModel.dispatch(AppIntent.Navigate(Screen.HOME))
                                    }
                                )
                            }

                            Screen.CHALLENGE -> {
                                appState.user?.let { user ->
                                    ChallengeScreen(
                                        user = user,
                                        onBack = {
                                            appViewModel.dispatch(AppIntent.Navigate(Screen.HOME))
                                        },
                                        onStartChallenge = { level, target ->
                                            gameViewModel.dispatch(GameIntent.StartChallenge(level, target))
                                            appViewModel.dispatch(AppIntent.Navigate(Screen.HOME))
                                        }
                                    )
                                }
                            }

                            Screen.CHALLENGE_HERO -> {
                                appState.user?.let { user ->
                                    ChallengeHeroScreen(
                                        user = user,
                                        onBack = {
                                            appViewModel.dispatch(AppIntent.Navigate(Screen.HOME))
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Game screens overlay
                val game = gameState.game
                when (game.status) {
                    GameStatus.PLAYING,
                    GameStatus.CORRECT_ANSWER,
                    GameStatus.WRONG_ANSWER,
                    GameStatus.TIMEOUT -> {
                        QuizScreen(
                            gameState = game,
                            onAnswerSelected = { index -> gameViewModel.dispatch(GameIntent.SelectAnswer(index)) }
                        )
                    }

                    GameStatus.LEVEL_COMPLETE -> {
                        LevelCompleteScreen(
                            gameState = game,
                            onNextLevel = { gameViewModel.dispatch(GameIntent.StartNextLevel) },
                            onRetryLevel = { gameViewModel.dispatch(GameIntent.RetryCurrentLevel) },
                            onBackToHome = {
                                gameViewModel.dispatch(GameIntent.ClearChallengeTarget)
                                gameViewModel.dispatch(GameIntent.ResetGame)
                                appViewModel.dispatch(AppIntent.Navigate(Screen.HOME))
                            },
                            challengeTarget = gameState.challengeTarget
                        )
                    }

                    GameStatus.GAME_OVER -> {
                        ResultScreen(
                            gameState = game,
                            onRetryLevel = { gameViewModel.dispatch(GameIntent.RetryCurrentLevel) },
                            onBackToHome = {
                                gameViewModel.dispatch(GameIntent.ClearChallengeTarget)
                                gameViewModel.dispatch(GameIntent.ResetGame)
                                appViewModel.dispatch(AppIntent.Navigate(Screen.HOME))
                            },
                            challengeTarget = gameState.challengeTarget
                        )
                    }

                    else -> {}
                }
            }
        }
    }
}

@Composable
private fun AppLoadingScreen() {
    val pulseScale = rememberPulseScale()
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "loadProgress"
    )

    ScreenBackground(contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(Res.drawable.app_logo),
                contentDescription = stringResource(Res.string.home_logo_description),
                modifier = Modifier
                    .size(110.dp)
                    .scale(pulseScale),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = stringResource(Res.string.home_title),
                fontSize = 26.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                color = QuizPalette.Gold
            )
            Spacer(modifier = Modifier.height(30.dp))
            QuizProgressBar(
                fraction = progress,
                modifier = Modifier.fillMaxWidth(0.55f),
                height = 6.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(Res.string.app_loading),
                fontSize = 14.sp,
                color = QuizPalette.TextMuted
            )
        }
    }
}
