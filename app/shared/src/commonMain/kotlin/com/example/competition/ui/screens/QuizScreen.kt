package com.example.competition.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.competition.model.Difficulty
import com.example.competition.model.GameState
import com.example.competition.model.GameStatus
import com.example.competition.ui.theme.*
import com.example.competition.ui.categoryLabel
import com.example.competition.ui.difficultyLabel
import com.example.competition.ui.levelTitle
import org.jetbrains.compose.resources.stringResource
import competition.app.shared.generated.resources.Res
import competition.app.shared.generated.resources.*

internal enum class OptionState { Active, Correct, Wrong, Disabled }

@Composable
fun QuizScreen(
    gameState: GameState,
    onAnswerSelected: (Int) -> Unit
) {
    val questionWithOpts = gameState.questions[gameState.currentQuestionIndex]
    val progress = gameState.timeRemaining / questionWithOpts.timeLimitSeconds
    val timerColor = when {
        progress > 0.5f -> CorrectGreen
        progress > 0.2f -> TimerOrange
        else -> BrightRed
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0D1B2A), Color(0xFF1B2838), Color(0xFF1A237E))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Level header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(Res.string.game_score_label),
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "${gameState.score}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Gold
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Gold.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = stringResource(Res.string.game_level_header, gameState.currentLevel, levelTitle(gameState.currentLevel)),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Gold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(Res.string.game_streak_label),
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "x${gameState.streak}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (gameState.streak > 0) Gold else Color.White.copy(alpha = 0.4f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.game_question_progress, gameState.currentQuestionIndex + 1, gameState.questions.size),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(end = 8.dp)
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction = progress.coerceIn(0f, 1f))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(timerColor, timerColor.copy(alpha = 0.7f))
                                )
                            )
                    )
                }

                Text(
                    text = "${gameState.timeRemaining.toInt()}s",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = timerColor,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tags
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Purple.copy(alpha = 0.3f)
                ) {
                    Text(
                        text = categoryLabel(questionWithOpts.category),
                        fontSize = 12.sp,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when (questionWithOpts.difficulty) {
                        Difficulty.EASY -> CorrectGreen.copy(alpha = 0.3f)
                        Difficulty.MEDIUM -> TimerOrange.copy(alpha = 0.3f)
                        Difficulty.HARD -> BrightRed.copy(alpha = 0.3f)
                    }
                ) {
                    Text(
                        text = difficultyLabel(questionWithOpts.difficulty),
                        fontSize = 12.sp,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Question card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.1f),
                shadowElevation = 8.dp
            ) {
                Text(
                    text = questionWithOpts.text,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Options
            questionWithOpts.shuffledOptions.forEachIndexed { index, option ->
                val optionState = when {
                    gameState.isAnswerRevealed && index == questionWithOpts.shuffledCorrectIndex -> OptionState.Correct
                    gameState.isAnswerRevealed && index == gameState.selectedAnswerIndex && index != questionWithOpts.shuffledCorrectIndex -> OptionState.Wrong
                    gameState.isAnswerRevealed -> OptionState.Disabled
                    gameState.status == GameStatus.PLAYING -> OptionState.Active
                    else -> OptionState.Disabled
                }

                val label = listOf(
                    stringResource(Res.string.game_option_a),
                    stringResource(Res.string.game_option_b),
                    stringResource(Res.string.game_option_c),
                    stringResource(Res.string.game_option_d)
                )[index]

                OptionCard(
                    label = label,
                    text = option,
                    state = optionState,
                    onClick = { onAnswerSelected(index) }
                )

                Spacer(modifier = Modifier.height(10.dp))
            }

            // Answer feedback
            AnimatedVisibility(
                visible = gameState.isAnswerRevealed,
                enter = fadeIn() + slideInVertically(),
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = when (gameState.status) {
                        GameStatus.CORRECT_ANSWER -> CorrectGreen.copy(alpha = 0.2f)
                        GameStatus.WRONG_ANSWER -> WrongRed.copy(alpha = 0.2f)
                        GameStatus.TIMEOUT -> TimerOrange.copy(alpha = 0.2f)
                        else -> Color.Transparent
                    }
                ) {
                    Text(
                        text = when (gameState.status) {
                            GameStatus.CORRECT_ANSWER -> stringResource(Res.string.game_correct_answer)
                            GameStatus.WRONG_ANSWER -> stringResource(Res.string.game_wrong_answer)
                            GameStatus.TIMEOUT -> stringResource(Res.string.game_timeout)
                            else -> ""
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (gameState.status) {
                            GameStatus.CORRECT_ANSWER -> CorrectGreen
                            GameStatus.WRONG_ANSWER -> WrongRed
                            GameStatus.TIMEOUT -> TimerOrange
                            else -> Color.White
                        },
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun OptionCard(
    label: String,
    text: String,
    state: OptionState,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = when (state) {
            OptionState.Active -> OptionDefault
            OptionState.Correct -> CorrectGreen
            OptionState.Wrong -> WrongRed
            OptionState.Disabled -> Color(0xFFB0BEC5).copy(alpha = 0.3f)
        },
        animationSpec = tween(300),
        label = "optionBg"
    )

    val textColor by animateColorAsState(
        targetValue = when (state) {
            OptionState.Active -> DeepBlue
            OptionState.Correct -> Color.White
            OptionState.Wrong -> Color.White
            OptionState.Disabled -> Color.White.copy(alpha = 0.5f)
        },
        animationSpec = tween(300),
        label = "optionText"
    )

    val borderColor by animateColorAsState(
        targetValue = when (state) {
            OptionState.Active -> LightBlue.copy(alpha = 0.3f)
            OptionState.Correct -> CorrectGreen
            OptionState.Wrong -> WrongRed
            OptionState.Disabled -> Color.Transparent
        },
        animationSpec = tween(300),
        label = "optionBorder"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (state == OptionState.Active) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(14.dp),
        color = backgroundColor,
        shadowElevation = if (state == OptionState.Active) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = textColor.copy(alpha = 0.15f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = label,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = textColor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
