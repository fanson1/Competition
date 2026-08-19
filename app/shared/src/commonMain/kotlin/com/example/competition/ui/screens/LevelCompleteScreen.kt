package com.example.competition.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.competition.model.GameState
import com.example.competition.model.LeaderboardEntry
import com.example.competition.ui.components.AnimatedCount
import com.example.competition.ui.components.DeepGradientColors
import com.example.competition.ui.components.GlassCard
import com.example.competition.ui.components.QuizPrimaryButton
import com.example.competition.ui.components.QuizSecondaryButton
import com.example.competition.ui.components.ScreenBackground
import com.example.competition.ui.theme.*
import com.example.competition.ui.levelTitle
import com.example.competition.ui.levelSubtitle
import org.jetbrains.compose.resources.stringResource
import competition.app.shared.generated.resources.Res
import competition.app.shared.generated.resources.*

@Composable
fun LevelCompleteScreen(
    gameState: GameState,
    onNextLevel: () -> Unit,
    onRetryLevel: () -> Unit,
    onBackToHome: () -> Unit,
    challengeTarget: LeaderboardEntry? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "celebrate")

    val titleScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "titleScale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val accuracy = if (gameState.levelCorrectCount + gameState.levelWrongCount > 0) {
        (gameState.levelCorrectCount * 100) / (gameState.levelCorrectCount + gameState.levelWrongCount)
    } else 0

    val starCount = when {
        gameState.levelCorrectCount >= 8 -> 3
        gameState.levelCorrectCount >= 6 -> 2
        else -> 1
    }

    val isChallengeMode = challengeTarget != null
    val isChallengeWin = isChallengeMode && challengeTarget?.let { gameState.levelScore > it.score } == true

    ScreenBackground(colors = DeepGradientColors, contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.fillMaxSize()) {
            repeat(10) { i ->
                val offsetX = ((i * 37 % 280) - 140).toFloat()
                val offsetY = ((i * 53 % 380) - 190).toFloat()
                val particleSize = (6 + i * 3 % 10).dp
                val floatY by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = -14f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(900 + i * 130, easing = EaseInOutCubic),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "float$i"
                )
                Box(
                    modifier = Modifier
                        .offset(x = offsetX.dp, y = offsetY.dp + floatY.dp)
                        .size(particleSize)
                        .clip(CircleShape)
                        .background(
                            when (i % 3) {
                                0 -> QuizPalette.Gold.copy(alpha = glowAlpha * 0.7f)
                                1 -> Success.copy(alpha = glowAlpha * 0.5f)
                                else -> Info.copy(alpha = glowAlpha * 0.5f)
                            }
                        )
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .statusBarsPadding()
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // ---- celebratory emblem ----
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .scale(titleScale)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        QuizPalette.Gold.copy(alpha = glowAlpha * 0.6f),
                                        QuizPalette.Gold.copy(alpha = 0f)
                                    )
                                )
                            )
                    )
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .scale(titleScale)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(QuizPalette.GoldPeak, QuizPalette.Gold, QuizPalette.GoldDeep)
                                )
                            )
                            .border(3.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🏆",
                            fontSize = 52.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = stringResource(Res.string.level_complete_title, gameState.currentLevel),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = QuizPalette.Gold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = levelTitle(gameState.currentLevel),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.scale(titleScale)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = levelSubtitle(gameState.currentLevel),
                    fontSize = 15.sp,
                    color = QuizPalette.TextSecondary
                )

                // ---- challenge verdict ----
                if (isChallengeMode && challengeTarget != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = if (isChallengeWin)
                            stringResource(Res.string.level_complete_challenge_win)
                        else
                            stringResource(Res.string.level_complete_challenge_lose),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isChallengeWin) QuizPalette.Success else QuizPalette.Danger,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.scale(titleScale)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(Res.string.level_complete_challenge_target, challengeTarget.nickname),
                        fontSize = 13.sp,
                        color = QuizPalette.TextMuted
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    LevelChallengeScoreRow(
                        myScore = gameState.levelScore,
                        targetScore = challengeTarget.score,
                        targetName = challengeTarget.nickname
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // ---- stars with pop-in stagger ----
                Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    repeat(3) { index ->
                        val filled = index < starCount
                        val reveal by animateFloatAsState(
                            targetValue = if (filled) 1f else 0f,
                            animationSpec = spring(
                                dampingRatio = 0.5f,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "star$index"
                        )
                        Text(
                            text = if (filled) "★" else "☆",
                            fontSize = 46.sp,
                            color = if (filled) QuizPalette.Gold else Color.White.copy(alpha = 0.25f),
                            modifier = Modifier
                                .scale(0.3f + 0.7f * reveal)
                                .graphicsLayer { alpha = reveal }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ---- stats card ----
                GlassCard(modifier = Modifier.fillMaxWidth(), shape = QuizRadii.lg, contentPadding = PaddingValues(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        LevelStatItem(
                            label = stringResource(Res.string.level_complete_correct),
                            value = gameState.levelCorrectCount,
                            color = Success
                        )
                        LevelStatItem(
                            label = stringResource(Res.string.level_complete_wrong),
                            value = gameState.levelWrongCount,
                            color = QuizPalette.Danger
                        )
                        LevelStatItem(
                            label = stringResource(Res.string.level_complete_accuracy),
                            value = accuracy,
                            color = QuizPalette.Gold
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        LevelStatItem(
                            label = stringResource(Res.string.level_complete_score),
                            value = gameState.score,
                            color = Info
                        )
                        LevelStatItem(
                            label = stringResource(Res.string.level_complete_streak),
                            value = gameState.maxStreak,
                            color = TimerOrange
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (!isChallengeMode) {
                    QuizPrimaryButton(
                        text = if (gameState.currentLevel < 10)
                            stringResource(Res.string.level_complete_next_level, levelTitle(gameState.currentLevel + 1))
                        else
                            stringResource(Res.string.level_complete_all_done),
                        leading = { Text(text = "▶", fontSize = 15.sp, color = QuizPalette.NightDeep) },
                        onClick = onNextLevel,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    QuizSecondaryButton(
                        text = stringResource(Res.string.level_complete_retry),
                        onClick = onRetryLevel,
                        modifier = Modifier.weight(1f)
                    )
                    QuizSecondaryButton(
                        text = stringResource(Res.string.level_complete_home),
                        onClick = onBackToHome,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun LevelStatItem(label: String, value: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AnimatedCount(
            target = value,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = QuizPalette.TextMuted
        )
    }
}

@Composable
private fun LevelChallengeScoreRow(myScore: Int, targetScore: Int, targetName: String) {
    val isWin = myScore > targetScore
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$myScore",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (isWin) QuizPalette.Gold else Color.White
            )
            Text(
                text = stringResource(Res.string.level_complete_my_score),
                fontSize = 11.sp,
                color = QuizPalette.TextMuted
            )
        }

        Text(
            text = stringResource(Res.string.level_complete_vs),
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = QuizPalette.Gold
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$targetScore",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (!isWin) QuizPalette.Gold else Color.White
            )
            Text(
                text = stringResource(Res.string.level_complete_target_score, targetName),
                fontSize = 11.sp,
                color = QuizPalette.TextMuted,
                maxLines = 1
            )
        }
    }
}