package com.example.competition.presentation.leaderboard

import com.example.competition.model.LeaderboardEntry
import com.example.competition.mvi.MviState

/**
 * State for the leaderboard screen (tab 0 = total, 1..10 = per level).
 */
data class LeaderboardUiState(
    val selectedTab: Int = 0,
    val entries: List<LeaderboardEntry> = emptyList(),
    val isLoading: Boolean = false,
) : MviState
