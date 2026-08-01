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
import com.example.competition.data.UserManager
import com.example.competition.model.ChallengeRecord
import com.example.competition.model.ChallengeStats
import com.example.competition.model.User
import com.example.competition.ui.theme.*
import com.example.competition.PlatformUtils
import org.jetbrains.compose.resources.stringResource
import competition.app.shared.generated.resources.Res
import competition.app.shared.generated.resources.*

@Composable
fun ChallengeHeroScreen(
    user: User,
    onBack: () -> Unit
) {
    val stats = remember { UserManager.getChallengeStats(user.id) }
    var selectedTab by remember { mutableStateOf(0) }

    val challenges = remember(selectedTab) {
        if (selectedTab == 0) {
            UserManager.getChallengesAsChallenger(user.id)
        } else {
            UserManager.getChallengesAsTarget(user.id)
        }
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
                    text = stringResource(Res.string.challenge_hero_back),
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.clickable { onBack() }
                )
                Text(
                    text = stringResource(Res.string.challenge_hero_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "",
                    modifier = Modifier.width(60.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Stats card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.08f)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Gold.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = user.avatarEmoji, fontSize = 40.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = user.nickname,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (selectedTab == 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            HeroStatItem(stringResource(Res.string.challenge_hero_total), "${stats.totalChallenges}", LightBlue)
                            HeroStatItem(stringResource(Res.string.challenge_hero_wins), "${stats.wins}", CorrectGreen)
                            HeroStatItem(stringResource(Res.string.challenge_hero_losses), "${stats.losses}", WrongRed)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            HeroStatItem(stringResource(Res.string.challenge_hero_win_rate), "${(stats.winRate * 100).toInt()}%", Gold)
                            HeroStatItem(stringResource(Res.string.challenge_hero_score), "${stats.totalChallengeScore}", TimerOrange)
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            HeroStatItem(stringResource(Res.string.challenge_hero_total), "${stats.challengedTotal}", LightBlue)
                            HeroStatItem(stringResource(Res.string.challenge_hero_wins), "${stats.challengedWins}", CorrectGreen)
                            HeroStatItem(stringResource(Res.string.challenge_hero_losses), "${stats.challengedLosses}", WrongRed)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            HeroStatItem(stringResource(Res.string.challenge_hero_win_rate), "${(stats.challengedWinRate * 100).toInt()}%", Gold)
                            HeroStatItem(stringResource(Res.string.challenge_hero_score), "${stats.totalChallengeScore}", TimerOrange.copy(alpha = 0.3f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = Gold,
                edgePadding = 0.dp
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = stringResource(Res.string.challenge_hero_tab_challenger),
                            fontSize = 14.sp,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 0) Gold else Color.White.copy(alpha = 0.6f)
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = stringResource(Res.string.challenge_hero_tab_target),
                            fontSize = 14.sp,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 1) Gold else Color.White.copy(alpha = 0.6f)
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (challenges.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (selectedTab == 0) {
                            stringResource(Res.string.challenge_hero_empty)
                        } else {
                            stringResource(Res.string.challenge_hero_empty_target)
                        },
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(challenges) { index, record ->
                        ChallengeRecordItem(record, user.id)
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroStatItem(label: String, value: String, color: Color) {
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
private fun ChallengeRecordItem(record: ChallengeRecord, currentUserId: String) {
    val isChallenger = record.challengerId == currentUserId
    val bgColor = if (record.isWin) CorrectGreen.copy(alpha = 0.1f) else WrongRed.copy(alpha = 0.1f)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Result icon
            Text(
                text = if (record.isWin) "✓" else "✗",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (record.isWin) CorrectGreen else WrongRed,
                modifier = Modifier.width(32.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isChallenger) {
                        stringResource(Res.string.challenge_hero_challenged, record.targetName)
                    } else {
                        stringResource(Res.string.challenge_hero_challenged_by, record.challengerName)
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Text(
                    text = stringResource(Res.string.challenge_hero_level, record.level),
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }

            // Scores
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${record.challengerScore} vs ${record.targetScore}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (record.isWin) CorrectGreen else WrongRed
                )
                Text(
                    text = formatTimestamp(record.timestamp),
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.4f)
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
