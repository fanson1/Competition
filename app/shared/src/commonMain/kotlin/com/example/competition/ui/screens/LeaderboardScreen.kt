package com.example.competition.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
import com.example.competition.model.LeaderboardEntry
import com.example.competition.presentation.leaderboard.LeaderboardEffect
import com.example.competition.presentation.leaderboard.LeaderboardIntent
import com.example.competition.presentation.leaderboard.LeaderboardViewModel
import com.example.competition.ui.MviEffectCollector
import com.example.competition.ui.components.AnimatedCount
import com.example.competition.ui.components.EmptyState
import com.example.competition.ui.components.ScreenBackground
import com.example.competition.ui.components.ScreenHeader
import com.example.competition.ui.rememberViewModel
import com.example.competition.ui.theme.*
import org.jetbrains.compose.resources.stringResource
import competition.app.shared.generated.resources.Res
import competition.app.shared.generated.resources.*

@Composable
fun LeaderboardScreen(
    onBack: () -> Unit
) {
    val viewModel = rememberViewModel { LeaderboardViewModel() }
    val state by viewModel.state.collectAsState()

    MviEffectCollector(viewModel) { effect ->
        when (effect) {
            LeaderboardEffect.Back -> onBack()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.dispatch(LeaderboardIntent.Refresh)
    }

    val tabs = listOf(stringResource(Res.string.leaderboard_tab_total)) +
        (1..10).map { stringResource(Res.string.leaderboard_tab_level, it) }

    ScreenBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            ScreenHeader(
                backLabel = stringResource(Res.string.leaderboard_back),
                title = stringResource(Res.string.leaderboard_title),
                onBack = { viewModel.dispatch(LeaderboardIntent.Back) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ScrollableTabRow(
                selectedTabIndex = state.selectedTab,
                containerColor = Color.Transparent,
                contentColor = QuizPalette.Gold,
                edgePadding = 0.dp,
                indicator = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = state.selectedTab == index,
                        onClick = { viewModel.dispatch(LeaderboardIntent.SelectTab(index)) },
                        text = {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (state.selectedTab == index) FontWeight.Bold
                                else FontWeight.Normal,
                                color = if (state.selectedTab == index) QuizPalette.Gold
                                else QuizPalette.TextSecondary
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = QuizPalette.Gold, strokeWidth = 3.dp)
                    }
                }
                state.entries.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyState(
                            emoji = "🏆",
                            title = stringResource(Res.string.leaderboard_empty),
                            subtitle = stringResource(Res.string.challenge_empty)
                        )
                    }
                }
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        itemsIndexed(state.entries) { index, entry ->
                            LeaderboardItem(
                                rank = index + 1,
                                entry = entry,
                                isTotalRanking = state.selectedTab == 0
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaderboardItem(rank: Int, entry: LeaderboardEntry, isTotalRanking: Boolean = false) {
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
    val borderColor = when (rank) {
        1 -> QuizPalette.Gold.copy(alpha = 0.6f)
        2 -> Color(0xFFC0C0C0).copy(alpha = 0.5f)
        3 -> Color(0xFFCD7F32).copy(alpha = 0.5f)
        else -> QuizPalette.GlassBorder
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(QuizRadii.md)
            .background(
                when (rank) {
                    1 -> QuizPalette.Gold.copy(alpha = 0.12f)
                    2 -> Color(0xFFC0C0C0).copy(alpha = 0.10f)
                    3 -> Color(0xFFCD7F32).copy(alpha = 0.10f)
                    else -> QuizPalette.Glass
                }
            )
            .border(1.dp, borderColor, QuizRadii.md)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Rank badge
            Box(
                modifier = Modifier
                    .width(34.dp)
                    .height(34.dp)
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
                        fontSize = 18.sp
                    )
                } else {
                    Text(
                        text = "$rank",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = rankTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Avatar
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(QuizPalette.Gold.copy(alpha = 0.2f))
                    .border(1.dp, QuizPalette.Gold.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = entry.avatarEmoji, fontSize = 22.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.nickname,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                )
                Text(
                    text = if (isTotalRanking) {
                        stringResource(Res.string.leaderboard_total_description, entry.correctCount, entry.level)
                    } else {
                        stringResource(Res.string.leaderboard_level_description, entry.level, entry.correctCount)
                    },
                    fontSize = 12.sp,
                    color = QuizPalette.TextMuted
                )
            }

            // Score
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "⭐", fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(3.dp))
                    AnimatedCount(
                        target = entry.score,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = QuizPalette.Gold
                    )
                }
            }
        }
    }
}