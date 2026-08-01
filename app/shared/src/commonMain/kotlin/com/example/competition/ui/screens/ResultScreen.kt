package com.example.competition.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    val infiniteTransition = rememberInfiniteTransition(label = "shake")
    val titleScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "titleScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A0A0A),
                        Color(0xFF2D1B1B),
                        Color(0xFF0D1B2A)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = if (isChallengeWin) stringResource(Res.string.result_challenge_win) else stringResource(Res.string.result_challenge_lose),
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color = if (isChallengeWin) CorrectGreen else WrongRed,
                textAlign = TextAlign.Center,
                modifier = Modifier.scale(titleScale)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(Res.string.result_level_info, gameState.currentLevel, levelTitle(gameState.currentLevel)),
                fontSize = 18.sp,
                color = Color.White.copy(alpha = 0.7f)
            )

            if (isChallengeMode && challengeTarget != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(Res.string.result_challenge_target, challengeTarget.nickname),
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "$effectiveTotalScore",
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Black,
                        color = Gold
                    )
                    Text(
                        text = stringResource(Res.string.result_total_score),
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (isChallengeMode && challengeTarget != null) {
                        ScoreComparisonRow(
                            myScore = gameState.levelScore,
                            targetScore = challengeTarget.score,
                            targetName = challengeTarget.nickname
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ResultStatItem(stringResource(Res.string.result_correct), "${gameState.levelCorrectCount}", CorrectGreen)
                        ResultStatItem(stringResource(Res.string.result_wrong), "${gameState.levelWrongCount}", WrongRed)
                        ResultStatItem(stringResource(Res.string.result_streak), "${gameState.maxStreak}", Gold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            val accuracy = if (gameState.levelCorrectCount + gameState.levelWrongCount > 0) {
                (gameState.levelCorrectCount * 100) / (gameState.levelCorrectCount + gameState.levelWrongCount)
            } else 0

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.08f)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(Res.string.result_accuracy, accuracy),
                        fontSize = 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(Res.string.result_completion, gameState.levelCorrectCount),
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = onRetryLevel,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Gold,
                    contentColor = DeepBlue
                ),
                contentPadding = PaddingValues(horizontal = 40.dp, vertical = 14.dp),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "↻",
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(Res.string.result_retry),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onBackToHome,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(Res.string.result_home), fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun ResultStatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.6f)
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
                color = if (isWin) Gold else Color.White
            )
            Text(
                text = stringResource(Res.string.result_my_score),
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.6f)
            )
        }

        Text(
            text = stringResource(Res.string.result_vs),
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = Color.White.copy(alpha = 0.4f)
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$targetScore",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (!isWin) Gold else Color.White
            )
            Text(
                text = stringResource(Res.string.result_target_score, targetName),
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}
