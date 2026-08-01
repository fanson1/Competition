package com.example.competition.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.competition.repository.bridge.RepositoryBridge
import com.example.competition.ui.theme.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import competition.app.shared.generated.resources.Res
import competition.app.shared.generated.resources.*

@Composable
fun LeaderboardScreen(
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf(stringResource(Res.string.leaderboard_tab_total)) + (1..10).map { stringResource(Res.string.leaderboard_tab_level, it) }

    var leaderboard by remember(selectedTab) { mutableStateOf<List<LeaderboardEntry>>(emptyList()) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(selectedTab) {
        val result = if (selectedTab == 0) {
            RepositoryBridge.leaderboard().getLeaderboard()
        } else {
            RepositoryBridge.leaderboard().getLeaderboard(level = selectedTab)
        }
        leaderboard = result
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0D1B3E), Color(0xFF1A2980))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.leaderboard_back),
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.clickable { onBack() }
                )
                Text(
                    text = stringResource(Res.string.leaderboard_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "",
                    modifier = Modifier.width(60.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = Gold,
                edgePadding = 0.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 13.sp,
                                color = if (selectedTab == index) Gold else Color.White.copy(alpha = 0.6f)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (leaderboard.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(Res.string.leaderboard_empty),
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(leaderboard) { index, entry ->
                        LeaderboardItem(
                            rank = index + 1,
                            entry = entry,
                            isTotalRanking = selectedTab == 0
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaderboardItem(rank: Int, entry: LeaderboardEntry, isTotalRanking: Boolean = false) {
    val bgColor = when (rank) {
        1 -> Gold.copy(alpha = 0.2f)
        2 -> Color(0xFFC0C0C0).copy(alpha = 0.2f)
        3 -> Color(0xFFCD7F32).copy(alpha = 0.2f)
        else -> Color.White.copy(alpha = 0.05f)
    }

    val rankColor = when (rank) {
        1 -> Gold
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> Color.White.copy(alpha = 0.6f)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank
            Text(
                text = when (rank) {
                    1 -> "🥇"
                    2 -> "🥈"
                    3 -> "🥉"
                    else -> "$rank"
                },
                fontSize = if (rank <= 3) 24.sp else 16.sp,
                fontWeight = FontWeight.Bold,
                color = rankColor,
                modifier = Modifier.width(40.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Gold.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = entry.avatarEmoji, fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.nickname,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = if (isTotalRanking) {
                        stringResource(Res.string.leaderboard_total_description, entry.correctCount, entry.level)
                    } else {
                        stringResource(Res.string.leaderboard_level_description, entry.level, entry.correctCount)
                    },
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            // Score
            Text(
                text = "${entry.score}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Gold
            )
        }
    }
}
