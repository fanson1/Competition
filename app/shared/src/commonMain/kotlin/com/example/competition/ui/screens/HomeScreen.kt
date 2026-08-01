package com.example.competition.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.competition.data.LevelConfigs
import com.example.competition.model.GameState
import com.example.competition.model.User
import com.example.competition.model.UserProfile
import com.example.competition.repository.AppMode
import com.example.competition.repository.ModeRouter
import com.example.competition.ui.theme.*
import com.example.competition.ui.levelTitle
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import competition.app.shared.generated.resources.Res
import competition.app.shared.generated.resources.*
import competition.app.shared.generated.resources.app_logo

@Composable
fun HomeScreen(
    gameState: GameState,
    user: User?,
    profile: UserProfile?,
    onStartGame: () -> Unit,
    onStartLevel: (Int) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToChallenge: () -> Unit,
    onNavigateToChallengeHero: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D1B3E),
                        Color(0xFF1A2980),
                        Color(0xFF0D1B3E)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // User info bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (user != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onNavigateToProfile() }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Gold.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = user.avatarEmoji, fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = user.nickname,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = stringResource(Res.string.home_level_display, gameState.maxUnlockedLevel),
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mode indicator
                    val currentMode by ModeRouter.currentMode.collectAsState()
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (currentMode == AppMode.ONLINE) CorrectGreen
                                else Color.White.copy(alpha = 0.3f)
                            )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (currentMode == AppMode.ONLINE) stringResource(Res.string.mode_online)
                               else stringResource(Res.string.mode_offline),
                        fontSize = 11.sp,
                        color = if (currentMode == AppMode.ONLINE) CorrectGreen
                                else Color.White.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(Res.string.home_score_display, profile?.totalScore ?: gameState.score),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Gold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Image(
                painter = painterResource(Res.drawable.app_logo),
                contentDescription = stringResource(Res.string.home_logo_description),
                modifier = Modifier
                    .size(80.dp)
                    .scale(pulseScale),
                contentScale = ContentScale.Fit
            )

            Text(
                text = stringResource(Res.string.home_title),
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Gold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionButton(
                    modifier = Modifier.weight(1f),
                    icon = "🏆",
                    label = stringResource(Res.string.home_leaderboard),
                    onClick = onNavigateToLeaderboard
                )
                ActionButton(
                    modifier = Modifier.weight(1f),
                    icon = "⚔️",
                    label = stringResource(Res.string.home_challenge),
                    onClick = onNavigateToChallenge
                )
                ActionButton(
                    modifier = Modifier.weight(1f),
                    icon = "🎖️",
                    label = stringResource(Res.string.home_hero),
                    onClick = onNavigateToChallengeHero
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Start button
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onStartGame() },
                shape = RoundedCornerShape(16.dp),
                color = Gold,
                shadowElevation = 4.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.home_start_button),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(Res.string.home_select_level),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(LevelConfigs.levels) { config ->
                    val isUnlocked = config.level <= gameState.maxUnlockedLevel
                    val isCompleted = config.level in gameState.completedLevels

                    LevelCard(
                        config = config,
                        isUnlocked = isUnlocked,
                        isCompleted = isCompleted,
                        onClick = {
                            if (isUnlocked) {
                                onStartLevel(config.level)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButton(
    modifier: Modifier = Modifier,
    icon: String,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.08f)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun LevelCard(
    config: com.example.competition.data.LevelConfig,
    isUnlocked: Boolean,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    val bgColor = when {
        isCompleted -> CorrectGreen.copy(alpha = 0.2f)
        isUnlocked -> Color.White.copy(alpha = 0.1f)
        else -> Color.White.copy(alpha = 0.04f)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isUnlocked) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        shadowElevation = if (isUnlocked) 2.dp else 0.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCompleted -> CorrectGreen
                            isUnlocked -> Gold
                            else -> Color.White.copy(alpha = 0.1f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Text(
                        text = "✓",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "${config.level}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUnlocked) DeepBlue else Color.White.copy(alpha = 0.4f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = levelTitle(config.level),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isUnlocked) Color.White else Color.White.copy(alpha = 0.4f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                if (config.easyCount > 0) {
                    MiniDot(CorrectGreen, "E${config.easyCount}")
                }
                if (config.mediumCount > 0) {
                    MiniDot(TimerOrange, "M${config.mediumCount}")
                }
                if (config.hardCount > 0) {
                    MiniDot(BrightRed, "H${config.hardCount}")
                }
            }
        }
    }
}

@Composable
private fun MiniDot(color: Color, text: String) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.3f)
    ) {
        Text(
            text = text,
            fontSize = 9.sp,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}
