package com.example.competition.ui.screens

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.competition.data.UserManager
import com.example.competition.model.GameState
import com.example.competition.model.LeaderboardEntry
import com.example.competition.ui.components.AnimatedCount
import com.example.competition.ui.components.DeepGradientColors
import com.example.competition.ui.components.GlassCard
import com.example.competition.ui.components.QuizPrimaryButton
import com.example.competition.ui.components.QuizSecondaryButton
import com.example.competition.ui.components.ScreenBackground
import com.example.competition.ui.components.rememberPulseScale
import com.example.competition.ui.theme.*
import com.example.competition.ui.levelTitle
import org.jetbrains.compose.resources.stringResource
import competition.app.shared.generated.resources.Res
import competition.app.shared.generated.resources.*

@Composable
fun ResultScreen(
    gameState: GameState,
    onRetryLevel: () -> Unit,
    onBackToHome: () -> Unit,
    challengeTarget: LeaderboardEntry? = null
) {
    val isChallengeMode = challengeTarget != null
    val isChallengeWin = isChallengeMode && challengeTarget?.let { gameState.levelScore > it.score } == true
    val effectiveTotalScore = UserManager.getCurrentProfile()?.totalScore ?: gameState.score

    val pulse = rememberPulseScale()
    val heroColor = if (isChallengeWin) Success else QuizPalette.Danger
    val heroEmoji = when {
        isChallengeWin -> "🏆"
        isChallengeMode -> "💔"
        else -> "💪"
    }

    ScreenBackground(colors = DeepGradientColors, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .scale(pulse)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(heroColor.copy(alpha = 0.35f), heroColor.copy(alpha = 0f))
                            )
                        )
                )
                Text(
                    text = heroEmoji,
                    fontSize = 62.sp,
                    modifier = Modifier.scale(pulse)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isChallengeWin) stringResource(Res.string.result_challenge_win)
                else if (isChallengeMode) stringResource(Res.string.result_challenge_lose)
                else stringResource(Res.string.result_try_again),
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                color = heroColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(
                    Res.string.result_level_info,
                    gameState.currentLevel,
                    levelTitle(gameState.currentLevel)
                ),
                fontSize = 15.sp,
                color = QuizPalette.TextSecondary
            )

            if (isChallengeMode && challengeTarget != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(Res.string.result_challenge_target, challengeTarget.nickname),
                    fontSize = 12.sp,
                    color = QuizPalette.TextMuted
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = QuizRadii.lg,
                contentPadding = PaddingValues(22.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(Res.string.result_total_score),
                        fontSize = 12.sp,
                        color = QuizPalette.TextMuted
                    )
                    AnimatedCount(
                        target = effectiveTotalScore,
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Black,
                        color = QuizPalette.Gold
                    )

                    if (isChallengeMode && challengeTarget != null) {
                        Spacer(modifier = Modifier.height(18.dp))
                        ScoreComparisonRow(
                            myScore = gameState.levelScore,
                            targetScore = challengeTarget.score,
                            targetName = challengeTarget.nickname
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ResultStatItem(
                            label = stringResource(Res.string.result_correct),
                            value = gameState.levelCorrectCount,
                            color = Success
                        )
                        ResultStatItem(
                            label = stringResource(Res.string.result_wrong),
                            value = gameState.levelWrongCount,
                            color = Danger
                        )
                        ResultStatItem(
                            label = stringResource(Res.string.result_streak),
                            value = gameState.maxStreak,
                            color = QuizPalette.Gold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            val accuracy = if (gameState.levelCorrectCount + gameState.levelWrongCount > 0) {
                (gameState.levelCorrectCount * 100) /
                    (gameState.levelCorrectCount + gameState.levelWrongCount)
            } else 0

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = QuizRadii.sm,
                contentPadding = PaddingValues(14.dp, 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(Res.string.result_accuracy, accuracy),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Info
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = stringResource(Res.string.result_completion, gameState.levelCorrectCount),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = QuizPalette.TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            QuizPrimaryButton(
                text = stringResource(Res.string.result_retry),
                leading = { Text(text = "↻", fontSize = 18.sp, color = QuizPalette.NightDeep) },
                onClick = onRetryLevel,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            QuizSecondaryButton(
                text = stringResource(Res.string.result_home),
                onClick = onBackToHome,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun ResultStatItem(label: String, value: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AnimatedCount(
            target = value,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = QuizPalette.TextMuted
        )
    }
}

@Composable
private fun ScoreComparisonRow(myScore: Int, targetScore: Int, targetName: String) {
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
                text = stringResource(Res.string.result_my_score),
                fontSize = 11.sp,
                color = QuizPalette.TextMuted
            )
        }

        Text(
            text = stringResource(Res.string.result_vs),
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
                text = stringResource(Res.string.result_target_score, targetName),
                fontSize = 11.sp,
                color = QuizPalette.TextMuted,
                maxLines = 1
            )
        }
    }
}