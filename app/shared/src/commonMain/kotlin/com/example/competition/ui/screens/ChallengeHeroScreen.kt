package com.example.competition.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.competition.PlatformUtils
import com.example.competition.model.ChallengeRecord
import com.example.competition.model.User
import com.example.competition.presentation.challengehero.ChallengeHeroEffect
import com.example.competition.presentation.challengehero.ChallengeHeroIntent
import com.example.competition.presentation.challengehero.ChallengeHeroViewModel
import com.example.competition.ui.MviEffectCollector
import com.example.competition.ui.components.AnimatedCount
import com.example.competition.ui.components.EmptyState
import com.example.competition.ui.components.GlassCard
import com.example.competition.ui.components.QuizChip
import com.example.competition.ui.components.ScreenBackground
import com.example.competition.ui.components.ScreenHeader
import com.example.competition.ui.components.StatItem
import com.example.competition.ui.rememberViewModel
import com.example.competition.ui.theme.*
import org.jetbrains.compose.resources.stringResource
import competition.app.shared.generated.resources.Res
import competition.app.shared.generated.resources.*

@Composable
fun ChallengeHeroScreen(
    user: User,
    onBack: () -> Unit
) {
    val viewModel = rememberViewModel { ChallengeHeroViewModel(user.id) }
    val state by viewModel.state.collectAsState()

    MviEffectCollector(viewModel) { effect ->
        when (effect) {
            ChallengeHeroEffect.Back -> onBack()
        }
    }

    val stats = state.stats
    val challenges = state.challenges

    ScreenBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            ScreenHeader(
                backLabel = stringResource(Res.string.challenge_hero_back),
                title = stringResource(Res.string.challenge_hero_title),
                onBack = { viewModel.dispatch(ChallengeHeroIntent.Back) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Stats card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = QuizRadii.lg,
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 22.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(82.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(QuizPalette.Gold.copy(alpha = 0.35f), QuizPalette.GoldDeep.copy(alpha = 0.2f))
                            )
                        )
                        .border(2.dp, QuizPalette.Gold.copy(alpha = 0.7f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = user.avatarEmoji, fontSize = 40.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = user.nickname,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(22.dp))

                if (state.selectedTab == 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(stringResource(Res.string.challenge_hero_total), "${stats.totalChallenges}", QuizPalette.Info, valueFontSize = 24.sp, labelFontSize = 12.sp)
                        StatItem(stringResource(Res.string.challenge_hero_wins), "${stats.wins}", QuizPalette.Success, valueFontSize = 24.sp, labelFontSize = 12.sp)
                        StatItem(stringResource(Res.string.challenge_hero_losses), "${stats.losses}", QuizPalette.Danger, valueFontSize = 24.sp, labelFontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(stringResource(Res.string.challenge_hero_win_rate), "${(stats.winRate * 100).toInt()}%", QuizPalette.Gold, valueFontSize = 24.sp, labelFontSize = 12.sp)
                        StatItem(stringResource(Res.string.challenge_hero_score), "${stats.totalChallengeScore}", QuizPalette.Warning, valueFontSize = 24.sp, labelFontSize = 12.sp)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(stringResource(Res.string.challenge_hero_total), "${stats.challengedTotal}", QuizPalette.Info, valueFontSize = 24.sp, labelFontSize = 12.sp)
                        StatItem(stringResource(Res.string.challenge_hero_wins), "${stats.challengedWins}", QuizPalette.Success, valueFontSize = 24.sp, labelFontSize = 12.sp)
                        StatItem(stringResource(Res.string.challenge_hero_losses), "${stats.challengedLosses}", QuizPalette.Danger, valueFontSize = 24.sp, labelFontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(stringResource(Res.string.challenge_hero_win_rate), "${(stats.challengedWinRate * 100).toInt()}%", QuizPalette.Gold, valueFontSize = 24.sp, labelFontSize = 12.sp)
                        StatItem(stringResource(Res.string.challenge_hero_score), "${stats.totalChallengeScore}", QuizPalette.Warning.copy(alpha = 0.3f), valueFontSize = 24.sp, labelFontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tabs
            ScrollableTabRow(
                selectedTabIndex = state.selectedTab,
                containerColor = Color.Transparent,
                contentColor = QuizPalette.Gold,
                edgePadding = 0.dp,
                indicator = {}
            ) {
                Tab(
                    selected = state.selectedTab == 0,
                    onClick = { viewModel.dispatch(ChallengeHeroIntent.SelectTab(0)) },
                    text = {
                        Text(
                            text = stringResource(Res.string.challenge_hero_tab_challenger),
                            fontSize = 14.sp,
                            fontWeight = if (state.selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (state.selectedTab == 0) QuizPalette.Gold else QuizPalette.TextSecondary
                        )
                    }
                )
                Tab(
                    selected = state.selectedTab == 1,
                    onClick = { viewModel.dispatch(ChallengeHeroIntent.SelectTab(1)) },
                    text = {
                        Text(
                            text = stringResource(Res.string.challenge_hero_tab_target),
                            fontSize = 14.sp,
                            fontWeight = if (state.selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (state.selectedTab == 1) QuizPalette.Gold else QuizPalette.TextSecondary
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (challenges.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        emoji = "🎖️",
                        title = if (state.selectedTab == 0) {
                            stringResource(Res.string.challenge_hero_empty)
                        } else {
                            stringResource(Res.string.challenge_hero_empty_target)
                        },
                        subtitle = ""
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(challenges) { _, record ->
                        ChallengeRecordItem(record, user.id)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChallengeRecordItem(record: ChallengeRecord, currentUserId: String) {
    val isChallenger = record.challengerId == currentUserId
    val isWin = record.isWin
    val accent = if (isWin) QuizPalette.Success else QuizPalette.Danger

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(QuizRadii.md)
            .background(if (isWin) QuizPalette.Success.copy(alpha = 0.08f) else QuizPalette.Danger.copy(alpha = 0.08f))
            .border(1.dp, accent.copy(alpha = 0.45f), QuizRadii.md)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Result icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.18f))
                    .border(1.dp, accent.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isWin) "✓" else "✗",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = accent
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isChallenger) {
                        stringResource(Res.string.challenge_hero_challenged, record.targetName)
                    } else {
                        stringResource(Res.string.challenge_hero_challenged_by, record.challengerName)
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                QuizChip(
                    text = stringResource(Res.string.challenge_hero_level, record.level),
                    color = accent,
                    emoji = "⚡"
                )
            }

            // Scores
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${record.challengerScore} vs ${record.targetScore}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = accent
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = formatTimestamp(record.timestamp),
                    fontSize = 11.sp,
                    color = QuizPalette.TextMuted
                )
            }
        }
    }
}

@Composable
private fun formatTimestamp(timestamp: Long): String {
    val now = PlatformUtils.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 60_000 -> stringResource(Res.string.challenge_hero_just_now)
        diff < 3600_000 -> stringResource(Res.string.challenge_hero_minutes_ago, diff / 60_000)
        diff < 86400_000 -> stringResource(Res.string.challenge_hero_hours_ago, diff / 3600_000)
        else -> stringResource(Res.string.challenge_hero_days_ago, diff / 86400_000)
    }
}
