package com.example.competition.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import com.example.competition.data.LevelConfig
import com.example.competition.data.LevelConfigs
import com.example.competition.model.GameState
import com.example.competition.model.User
import com.example.competition.model.UserProfile
import com.example.competition.ui.components.AnimatedCount
import com.example.competition.ui.components.DeepGradientColors
import com.example.competition.ui.components.GlassCard
import com.example.competition.ui.components.ModeChip
import com.example.competition.ui.components.PressableCard
import com.example.competition.ui.components.QuizPrimaryButton
import com.example.competition.ui.components.QuizProgressBar
import com.example.competition.ui.components.ScreenBackground
import com.example.competition.ui.components.rememberPulseScale
import com.example.competition.ui.levelTitle
import com.example.competition.ui.theme.*
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
    val snackbarHostState = remember { SnackbarHostState() }
    val pulseScale = rememberPulseScale()
    val maxLevel = gameState.maxUnlockedLevel.coerceAtLeast(1)
    val completion =
        if (gameState.completedLevels.isEmpty()) 0f
        else gameState.completedLevels.size / 10f

    ScreenBackground(colors = DeepGradientColors) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            // ---- top avatar / name + mode / score ----
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
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(QuizPalette.Gold.copy(alpha = 0.9f), QuizPalette.GoldDeep)
                                    )
                                )
                                .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = user.avatarEmoji, fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = user.nickname,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1
                            )
                            Text(
                                text = stringResource(Res.string.home_level_display, maxLevel),
                                fontSize = 11.sp,
                                color = QuizPalette.TextMuted
                            )
                        }
                    }
                } else {
                    Box(modifier = Modifier.size(42.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    ModeChip(snackbarHostState = snackbarHostState)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "⭐",
                            fontSize = 12.sp
                        )
                        AnimatedCount(
                            target = profile?.totalScore ?: gameState.score,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = QuizPalette.Gold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ---- branding ----
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(Res.drawable.app_logo),
                    contentDescription = stringResource(Res.string.home_logo_description),
                    modifier = Modifier
                        .size(84.dp)
                        .scale(pulseScale),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(Res.string.home_title),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                    color = QuizPalette.Gold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(Res.string.home_tagline),
                    fontSize = 13.sp,
                    color = QuizPalette.TextMuted,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ---- overall progress ----
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(Res.string.home_progress_label),
                        fontSize = 13.sp,
                        color = QuizPalette.TextSecondary
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = stringResource(
                            Res.string.home_progress_value,
                            gameState.completedLevels.size,
                            10
                        ),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = QuizPalette.Gold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                QuizProgressBar(fraction = completion, height = 7.dp)
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ---- action shortcuts ----
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HomeActionCell(
                    modifier = Modifier.weight(1f),
                    emoji = "🏆",
                    label = stringResource(Res.string.home_leaderboard),
                    accent = QuizPalette.Gold,
                    onClick = onNavigateToLeaderboard
                )
                HomeActionCell(
                    modifier = Modifier.weight(1f),
                    emoji = "⚔️",
                    label = stringResource(Res.string.home_challenge),
                    accent = Danger,
                    onClick = onNavigateToChallenge
                )
                HomeActionCell(
                    modifier = Modifier.weight(1f),
                    emoji = "🎖️",
                    label = stringResource(Res.string.home_hero),
                    accent = Info,
                    onClick = onNavigateToChallengeHero
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ---- primary CTA ----
            QuizPrimaryButton(
                text = stringResource(Res.string.home_start_button),
                leading = { Text(text = "▶", fontSize = 15.sp, color = QuizPalette.NightDeep) },
                onClick = onStartGame,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(22.dp))

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
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(LevelConfigs.levels) { config ->
                    val isUnlocked = config.level <= gameState.maxUnlockedLevel
                    val isCompleted = config.level in gameState.completedLevels

                    LevelCard(
                        config = config,
                        isUnlocked = isUnlocked,
                        isCompleted = isCompleted,
                        bestScore = if (isCompleted)
                            profile?.levelScores?.get(config.level) else null,
                        onClick = { if (isUnlocked) onStartLevel(config.level) }
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun HomeActionCell(
    modifier: Modifier = Modifier,
    emoji: String,
    label: String,
    accent: Color,
    onClick: () -> Unit
) {
    PressableCard(onClick = onClick, modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 17.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun LevelCard(
    config: LevelConfig,
    isUnlocked: Boolean,
    isCompleted: Boolean,
    bestScore: Int?,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "levelPress"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(QuizRadii.lg)
            .background(
                when {
                    isCompleted -> QuizPalette.Success.copy(alpha = 0.13f)
                    isUnlocked -> QuizPalette.GlassStrong
                    else -> Color.White.copy(alpha = 0.035f)
                }
            )
            .border(
                1.dp,
                when {
                    isCompleted -> QuizPalette.Success.copy(alpha = 0.4f)
                    isUnlocked -> QuizPalette.Gold.copy(alpha = 0.3f)
                    else -> QuizPalette.GlassBorder.copy(alpha = 0.4f)
                },
                QuizRadii.lg
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = isUnlocked
            ) { onClick() }
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val accent = when {
            isCompleted -> QuizPalette.Success
            isUnlocked -> QuizPalette.Gold
            else -> QuizPalette.TextMuted
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(accent.copy(alpha = 0.9f), accent.copy(alpha = 0.65f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Text(
                    text = "✓",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            } else {
                Text(
                    text = "${config.level}",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isUnlocked) QuizPalette.NightDeep else Color.White.copy(alpha = 0.45f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = levelTitle(config.level),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (isUnlocked) Color.White else Color.White.copy(alpha = 0.38f),
            textAlign = TextAlign.Center,
            maxLines = 1
        )

Spacer(modifier = Modifier.height(6.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (config.easyCount > 0) MiniD(QuizPalette.Success, "E${config.easyCount}")
            if (config.mediumCount > 0) MiniD(Warning, "M${config.mediumCount}")
            if (config.hardCount > 0) MiniD(Danger, "H${config.hardCount}")
        }

        if (!isUnlocked) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(Res.string.home_locked),
                fontSize = 10.sp,
                color = QuizPalette.TextMuted
            )
        } else if (bestScore != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(Res.string.home_level_best, bestScore),
                fontSize = 10.sp,
                color = QuizPalette.Gold
            )
        }
    }
}

@Composable
private fun MiniD(
    color: Color,
    text: String
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .background(color.copy(alpha = 0.16f))
            .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}