package com.example.competition.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import com.example.competition.ui.components.AnimatedCount
import com.example.competition.ui.components.GlassCard
import com.example.competition.ui.components.QuizChip
import com.example.competition.ui.components.QuizProgressBar
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
    val progress = (gameState.timeRemaining / questionWithOpts.timeLimitSeconds).coerceIn(0f, 1f)
    val timerGradient = when {
        progress > 0.5f -> listOf(QuizPalette.Success, QuizPalette.Success)
        progress > 0.2f -> listOf(Warning, QuizPalette.GoldDeep)
        else -> listOf(Danger, Danger)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(QuizPalette.NightDeep, QuizPalette.Night, QuizPalette.NightMid)
                )
            )
    ) {
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .size(280.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(QuizPalette.Gold.copy(alpha = 0.12f), QuizPalette.Gold.copy(alpha = 0f))
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .statusBarsPadding()
                .padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ---- header: score · level · streak ----
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                QuizHeaderStat(
                    label = stringResource(Res.string.game_score_label),
                    value = { AnimatedCount(target = gameState.score, fontSize = 22.sp, fontWeight = FontWeight.Black, color = QuizPalette.Gold) }
                )
                Box(
                    modifier = Modifier
                        .clip(QuizRadii.md)
                        .background(QuizPalette.Gold.copy(alpha = 0.16f))
                        .border(1.dp, QuizPalette.Gold.copy(alpha = 0.4f), QuizRadii.md)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(
                            Res.string.game_level_header,
                            gameState.currentLevel,
                            levelTitle(gameState.currentLevel)
                        ),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = QuizPalette.Gold
                    )
                }
                QuizHeaderStat(
                    label = stringResource(Res.string.game_streak_label),
                    value = { AnimatedCount(target = gameState.streak, fontSize = 22.sp, fontWeight = FontWeight.Black, color = if (gameState.streak > 0) QuizPalette.Gold else QuizPalette.TextMuted) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ---- progress + time ----
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(
                        Res.string.game_question_progress,
                        gameState.currentQuestionIndex + 1,
                        gameState.questions.size
                    ),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(end = 10.dp)
                )
                QuizProgressBar(
                    fraction = progress,
                    modifier = Modifier.weight(1f),
                    height = 8.dp,
                    colors = timerGradient
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "${gameState.timeRemaining.toInt()}s",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = timerGradient.first(),
                    modifier = Modifier.width(38.dp),
                    textAlign = TextAlign.End
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ---- category / difficulty tags ----
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuizChip(
                    text = categoryLabel(questionWithOpts.category),
                    color = VioletAccent,
                    emoji = when (questionWithOpts.category) {
                        com.example.competition.model.Category.SCIENCE -> "🔬"
                        com.example.competition.model.Category.HISTORY -> "🏺"
                        com.example.competition.model.Category.GEOGRAPHY -> "🌏"
                        com.example.competition.model.Category.CULTURE -> "🎭"
                        com.example.competition.model.Category.GENERAL -> "✍️"
                    }
                )
                QuizChip(
                    text = difficultyLabel(questionWithOpts.difficulty),
                    color = when (questionWithOpts.difficulty) {
                        Difficulty.EASY -> QuizPalette.Success
                        Difficulty.MEDIUM -> Warning
                        Difficulty.HARD -> Danger
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ---- question card ----
            AnimatedContent(
                targetState = gameState.currentQuestionIndex,
                transitionSpec = {
                    (slideInVertically { it / 4 } + fadeIn(animationSpec = tween(300))) togetherWith
                        (slideOutVertically { -it / 4 } + fadeOut(animationSpec = tween(200)))
                },
                label = "question"
            ) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = QuizRadii.lg,
                    contentPadding = PaddingValues(24.dp, 28.dp)
                ) {
                    Text(
                        text = questionWithOpts.text,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ---- options ----
            questionWithOpts.shuffledOptions.forEachIndexed { index, option ->
                val optionState = when {
                    gameState.isAnswerRevealed &&
                        index == questionWithOpts.shuffledCorrectIndex -> OptionState.Correct
                    gameState.isAnswerRevealed &&
                        index == gameState.selectedAnswerIndex &&
                        index != questionWithOpts.shuffledCorrectIndex -> OptionState.Wrong
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

            // ---- answer feedback ----
            AnimatedVisibility(
                visible = gameState.isAnswerRevealed,
                modifier = Modifier.padding(top = 10.dp),
                enter = scaleIn(initialScale = 0.8f) + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
val (bg, fg, icon, extra) = when (gameState.status) {
                    GameStatus.CORRECT_ANSWER -> listOf(
                        QuizPalette.Success.copy(alpha = 0.16f),
                        QuizPalette.Success,
                        "🎉",
                        if (gameState.streak > 1)
                            stringResource(Res.string.game_streak_bonus, gameState.streak)
                        else stringResource(Res.string.game_correct_answer)
                    )
                    GameStatus.WRONG_ANSWER -> listOf(
                        Danger.copy(alpha = 0.16f),
                        Danger,
                        "💥",
                        stringResource(Res.string.game_wrong_answer)
                    )
                    GameStatus.TIMEOUT -> listOf(
                        Warning.copy(alpha = 0.16f),
                        Warning,
                        "⏰",
                        stringResource(Res.string.game_timeout)
                    )
                    else -> listOf(Color.White.copy(alpha = 0.16f), Color.White, "", "")
                }
                val feedbackBg = bg as Color
                val feedbackFg = fg as Color
                val feedbackIcon = icon as String
                val feedbackText = extra as String
                Surface(
                    shape = QuizRadii.md,
                    color = feedbackBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (feedbackIcon.isNotEmpty()) {
                            Text(text = feedbackIcon, fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                        }
                        Text(
                            text = feedbackText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = feedbackFg,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Surface(
                    shape = QuizRadii.md,
                    color = bg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = icon, fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = extra,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = fg,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizHeaderStat(
    label: String,
    value: @Composable () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = QuizPalette.TextMuted
        )
        Spacer(modifier = Modifier.height(2.dp))
        value()
    }
}

@Composable
private fun OptionCard(
    label: String,
    text: String,
    state: OptionState,
    onClick: () -> Unit
) {
    val isValidated = state == OptionState.Correct || state == OptionState.Wrong
    val backgroundColor by animateColorAsState(
        targetValue = when (state) {
            OptionState.Active -> QuizPalette.GlassStrong
            OptionState.Correct -> QuizPalette.Success.copy(alpha = 0.22f)
            OptionState.Wrong -> Danger.copy(alpha = 0.22f)
            OptionState.Disabled -> QuizPalette.Glass.copy(alpha = 0.5f)
        },
        animationSpec = tween(300),
        label = "optionBg"
    )

    val borderColor by animateColorAsState(
        targetValue = when (state) {
            OptionState.Active -> QuizPalette.GlassBorder
            OptionState.Correct -> QuizPalette.Success
            OptionState.Wrong -> Danger
            OptionState.Disabled -> QuizPalette.GlassBorder.copy(alpha = 0.3f)
        },
        animationSpec = tween(300),
        label = "optionBorder"
    )

    val labelColor = when (state) {
        OptionState.Active -> QuizPalette.Info
        OptionState.Correct -> QuizPalette.Success
        OptionState.Wrong -> Danger
        OptionState.Disabled -> QuizPalette.TextMuted
    }

    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed && state == OptionState.Active) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "optionPress"
    )
    val bounce by animateFloatAsState(
        targetValue = if (state == OptionState.Correct) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "optionBounce"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(pressScale * bounce)
            .clip(QuizRadii.md)
            .background(backgroundColor)
            .border(1.5.dp, borderColor, QuizRadii.md)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = state == OptionState.Active
            ) { onClick() }
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(labelColor.copy(alpha = 0.18f))
                .border(1.dp, labelColor.copy(alpha = 0.6f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (state == OptionState.Correct) "✓"
                else if (state == OptionState.Wrong) "✗"
                else label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = labelColor
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = if (state == OptionState.Disabled) QuizPalette.TextMuted else Color.White,
            modifier = Modifier.weight(1f)
        )
    }
}