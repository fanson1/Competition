package com.example.competition.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
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
import com.example.competition.model.LeaderboardEntry
import com.example.competition.model.User
import com.example.competition.presentation.challenge.ChallengeEffect
import com.example.competition.presentation.challenge.ChallengeIntent
import com.example.competition.presentation.challenge.ChallengeViewModel
import com.example.competition.ui.MviEffectCollector
import com.example.competition.ui.components.AnimatedCount
import com.example.competition.ui.components.EmptyState
import com.example.competition.ui.components.GlassCard
import com.example.competition.ui.components.ScreenBackground
import com.example.competition.ui.components.ScreenHeader
import com.example.competition.ui.rememberViewModel
import com.example.competition.ui.theme.*
import org.jetbrains.compose.resources.stringResource
import competition.app.shared.generated.resources.Res
import competition.app.shared.generated.resources.*

@Composable
fun ChallengeScreen(
    user: User,
    onBack: () -> Unit,
    onStartChallenge: (Int, LeaderboardEntry) -> Unit
) {
    val viewModel = rememberViewModel { ChallengeViewModel() }
    val state by viewModel.state.collectAsState()

    MviEffectCollector(viewModel) { effect ->
        when (effect) {
            is ChallengeEffect.StartChallenge -> onStartChallenge(effect.level, effect.target)
            ChallengeEffect.Back -> onBack()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.dispatch(ChallengeIntent.SelectLevel(1))
    }

    val challengers = state.challengers
    val currentUserEntry = challengers.firstOrNull { it.userId == user.id }

    ScreenBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            ScreenHeader(
                backLabel = stringResource(Res.string.challenge_back),
                title = stringResource(Res.string.challenge_title),
                onBack = { viewModel.dispatch(ChallengeIntent.Back) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Level selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                (1..10).forEach { level ->
                    LevelPill(
                        level = level,
                        selected = state.selectedLevel == level,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.dispatch(ChallengeIntent.SelectLevel(level)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Current user's score
            if (currentUserEntry != null) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = QuizRadii.md,
                    color = QuizPalette.Gold.copy(alpha = 0.10f),
                    borderColor = QuizPalette.Gold.copy(alpha = 0.5f),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🏆", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(Res.string.challenge_my_score),
                                fontSize = 12.sp,
                                color = QuizPalette.TextSecondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AnimatedCount(
                                    target = currentUserEntry.score,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = QuizPalette.Gold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = stringResource(Res.string.challenge_score_format, currentUserEntry.score),
                                    fontSize = 13.sp,
                                    color = QuizPalette.TextMuted
                                )
                            }
                        }
                    }
                }
            } else {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = QuizRadii.md,
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.challenge_not_played),
                        fontSize = 14.sp,
                        color = QuizPalette.TextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = stringResource(Res.string.challenge_section_header, state.selectedLevel),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = QuizPalette.TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (challengers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        emoji = "⚔️",
                        title = stringResource(Res.string.challenge_empty),
                        subtitle = ""
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(challengers) { index, entry ->
                        ChallengeLeaderboardItem(
                            rank = index + 1,
                            entry = entry,
                            currentUserId = user.id,
                            canChallenge = entry.userId != user.id && currentUserEntry != null && entry.score > currentUserEntry.score,
                            onChallenge = { viewModel.dispatch(ChallengeIntent.StartChallenge(entry)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LevelPill(
    level: Int,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "pill"
    )
    Box(
        modifier = modifier
            .scale(scale)
            .clip(QuizRadii.sm)
            .background(
                if (selected) {
                    Brush.linearGradient(listOf(QuizPalette.GoldPeak, QuizPalette.Gold, QuizPalette.GoldDeep))
                } else {
                    Brush.linearGradient(listOf(QuizPalette.Glass, QuizPalette.Glass))
                }
            )
            .border(
                1.dp,
                if (selected) QuizPalette.Gold else QuizPalette.GlassBorder,
                QuizRadii.sm
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$level",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) QuizPalette.NightDeep else QuizPalette.TextSecondary
        )
    }
}

@Composable
private fun ChallengeLeaderboardItem(
    rank: Int,
    entry: LeaderboardEntry,
    currentUserId: String,
    canChallenge: Boolean,
    onChallenge: () -> Unit
) {
    val isCurrentUser = entry.userId == currentUserId
    val rankGradient = when (rank) {
        1 -> listOf(QuizPalette.GoldPeak, QuizPalette.GoldDeep)
        2 -> listOf(Color(0xFFE8E8F0), Color(0xFF9CA3AF))
        3 -> listOf(Color(0xFFF1B07A), Color(0xFFB4642B))
        else -> listOf(Color.White.copy(alpha = 0.12f), Color.White.copy(alpha = 0.12f))
    }
    val rankTextColor = when (rank) {
        1 -> QuizPalette.NightDeep
        2 -> Color(0xFF3D4153)
        3 -> Color.White
        else -> QuizPalette.TextSecondary
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(QuizRadii.md)
            .background(
                if (isCurrentUser) QuizPalette.Gold.copy(alpha = 0.12f)
                else QuizPalette.Glass
            )
            .border(
                1.dp,
                if (isCurrentUser) QuizPalette.Gold.copy(alpha = 0.6f) else QuizPalette.GlassBorder,
                QuizRadii.md
            )
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Rank badge
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(rankGradient)),
                contentAlignment = Alignment.Center
            ) {
                if (rank <= 3) {
                    Text(
                        text = when (rank) {
                            1 -> "🥇"
                            2 -> "🥈"
                            else -> "🥉"
                        },
                        fontSize = 15.sp
                    )
                } else {
                    Text(
                        text = "$rank",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = rankTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(QuizPalette.Gold.copy(alpha = 0.2f))
                    .border(1.dp, QuizPalette.Gold.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = entry.avatarEmoji, fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Name and score
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.nickname,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCurrentUser) QuizPalette.Gold else Color.White,
                    maxLines = 1
                )
                Text(
                    text = stringResource(Res.string.challenge_score_detail, entry.correctCount),
                    fontSize = 12.sp,
                    color = QuizPalette.TextMuted
                )
            }

            // Score and challenge button
            Column(horizontalAlignment = Alignment.End) {
                AnimatedCount(
                    target = entry.score,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = QuizPalette.Gold
                )
                if (canChallenge) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(QuizRadii.sm)
                            .background(QuizPalette.Danger.copy(alpha = 0.85f))
                            .clickable { onChallenge() }
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.challenge_challenge_button),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
