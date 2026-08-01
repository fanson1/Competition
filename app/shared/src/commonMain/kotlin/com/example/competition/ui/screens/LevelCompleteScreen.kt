package com.example.competition.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.competition.model.GameState
import com.example.competition.model.LeaderboardEntry
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
        initialValue = 0.5f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "titleScale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val rotateAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing)
        ),
        label = "rotate"
    )

    val floatY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY"
    )

    val accuracy = if (gameState.levelCorrectCount + gameState.levelWrongCount > 0) {
        (gameState.levelCorrectCount * 100) / (gameState.levelCorrectCount + gameState.levelWrongCount)
    } else 0

    val starCount = when {
        gameState.levelCorrectCount >= 10 -> 3
        gameState.levelCorrectCount >= 8 -> 3
        gameState.levelCorrectCount >= 6 -> 2
        else -> 1
    }

    val isChallengeMode = challengeTarget != null
    val isChallengeWin = isChallengeMode && challengeTarget?.let { gameState.levelScore > it.score } == true

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D1B3E),
                        Color(0xFF1A2980),
                        Color(0xFF0D47A1)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Floating particles
        repeat(12) { i ->
            val offsetX = (i * 37 % 300 - 150).toFloat()
            val offsetY = (i * 53 % 400 - 200).toFloat()
            val particleSize = (8 + i * 3 % 12).dp

            Box(
                modifier = Modifier
                    .offset(x = offsetX.dp, y = offsetY.dp + floatY.dp)
                    .size(particleSize)
                    .clip(CircleShape)
                    .background(
                        when (i % 3) {
                            0 -> Gold.copy(alpha = glowAlpha * 0.6f)
                            1 -> CorrectGreen.copy(alpha = glowAlpha * 0.4f)
                            else -> LightBlue.copy(alpha = glowAlpha * 0.5f)
                        }
                    )
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Trophy / star icon
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .rotate(rotateAngle)
                    .scale(titleScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Gold, DarkGold)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(Res.string.level_complete_star_filled),
                    fontSize = 48.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(Res.string.level_complete_title, gameState.currentLevel),
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Gold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = levelTitle(gameState.currentLevel),
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.scale(titleScale)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = levelSubtitle(gameState.currentLevel),
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Challenge result display
            if (isChallengeMode && challengeTarget != null) {
                val resultText = if (isChallengeWin) stringResource(Res.string.level_complete_challenge_win) else stringResource(Res.string.level_complete_challenge_lose)
                val resultColor = if (isChallengeWin) CorrectGreen else WrongRed
                Text(
                    text = resultText,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = resultColor,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(Res.string.level_complete_challenge_target, challengeTarget.nickname),
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                LevelChallengeScoreRow(
                    myScore = gameState.levelScore,
                    targetScore = challengeTarget.score,
                    targetName = challengeTarget.nickname
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Stars
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                repeat(3) { index ->
                    Text(
                        text = if (index < starCount) stringResource(Res.string.level_complete_star_filled) else stringResource(Res.string.level_complete_star_empty),
                        fontSize = 40.sp,
                        color = if (index < starCount) Gold else Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.scale(
                            if (index < starCount) titleScale else 1f
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Stats card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        LevelStatItem(stringResource(Res.string.level_complete_correct), "${gameState.levelCorrectCount}", CorrectGreen)
                        LevelStatItem(stringResource(Res.string.level_complete_wrong), "${gameState.levelWrongCount}", WrongRed)
                        LevelStatItem(stringResource(Res.string.level_complete_accuracy), stringResource(Res.string.level_complete_accuracy_value, accuracy), Gold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        LevelStatItem(stringResource(Res.string.level_complete_score), "${gameState.score}", LightBlue)
                        LevelStatItem(stringResource(Res.string.level_complete_streak), "${gameState.maxStreak}", TimerOrange)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Next level button (hidden in challenge mode since it's a single level)
            if (!isChallengeMode) {
                Button(
                    onClick = onNextLevel,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Gold,
                        contentColor = DeepBlue
                    ),
                    contentPadding = PaddingValues(horizontal = 40.dp, vertical = 14.dp),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "▶",
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (gameState.currentLevel < 10) stringResource(Res.string.level_complete_next_level, levelTitle(gameState.currentLevel + 1)) else stringResource(Res.string.level_complete_all_done),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onRetryLevel,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(Res.string.level_complete_retry), fontSize = 14.sp)
                }

                OutlinedButton(
                    onClick = onBackToHome,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(Res.string.level_complete_home), fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun LevelStatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 24.sp,
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
                color = if (isWin) Gold else Color.White
            )
            Text(
                text = stringResource(Res.string.level_complete_my_score),
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.6f)
            )
        }

        Text(
            text = stringResource(Res.string.level_complete_vs),
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
                text = stringResource(Res.string.level_complete_target_score, targetName),
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}
