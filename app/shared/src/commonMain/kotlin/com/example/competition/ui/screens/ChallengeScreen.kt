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
import com.example.competition.model.LeaderboardEntry
import com.example.competition.model.User
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
    var selectedLevel by remember { mutableStateOf(1) }

    val challengers = remember(selectedLevel) {
        UserManager.getLevelChallengers(selectedLevel)
    }

    val currentUserEntry = challengers.firstOrNull { it.userId == user.id }

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
                    text = stringResource(Res.string.challenge_back),
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.clickable { onBack() }
                )
                Text(
                    text = stringResource(Res.string.challenge_title),
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

            // Level selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                (1..10).forEach { level ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (selectedLevel == level) Gold else Color.White.copy(alpha = 0.1f)
                            )
                            .clickable { selectedLevel = level }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$level",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedLevel == level) DeepBlue else Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Current user's score
            if (currentUserEntry != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Gold.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🏆", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringResource(Res.string.challenge_my_score),
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                            Text(
                                text = stringResource(Res.string.challenge_score_format, currentUserEntry.score),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Gold
                            )
                        }
                    }
                }
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.05f)
                ) {
                    Text(
                        text = stringResource(Res.string.challenge_not_played),
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(Res.string.challenge_section_header, selectedLevel),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (challengers.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(Res.string.challenge_empty),
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(challengers) { index, entry ->
                        ChallengeLeaderboardItem(
                            rank = index + 1,
                            entry = entry,
                            currentUserId = user.id,
                            canChallenge = entry.userId != user.id && currentUserEntry != null && entry.score > currentUserEntry.score,
                            onChallenge = { onStartChallenge(selectedLevel, entry) }
                        )
                    }
                }
            }
        }
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
    val bgColor = when {
        isCurrentUser -> Gold.copy(alpha = 0.15f)
        rank <= 3 -> Color.White.copy(alpha = 0.08f)
        else -> Color.White.copy(alpha = 0.03f)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
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
                fontSize = if (rank <= 3) 20.sp else 14.sp,
                fontWeight = FontWeight.Bold,
                color = when (rank) {
                    1 -> Gold
                    2 -> Color(0xFFC0C0C0)
                    3 -> Color(0xFFCD7F32)
                    else -> Color.White.copy(alpha = 0.6f)
                },
                modifier = Modifier.width(36.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Gold.copy(alpha = 0.2f)),
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
                    color = if (isCurrentUser) Gold else Color.White
                )
                Text(
                    text = stringResource(Res.string.challenge_score_detail, entry.correctCount),
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }

            // Score and challenge button
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(Res.string.challenge_score_format, entry.score),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Gold
                )

                if (canChallenge) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        modifier = Modifier.clickable { onChallenge() },
                        shape = RoundedCornerShape(6.dp),
                        color = BrightRed.copy(alpha = 0.8f)
                    ) {
                        Text(
                            text = stringResource(Res.string.challenge_challenge_button),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
