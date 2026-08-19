package com.example.competition.presentation.challenge

import com.example.competition.model.LeaderboardEntry
import com.example.competition.mvi.MviState

/**
 * State for the challenge screen (pick a level and a target to beat).
 */
data class ChallengeUiState(
    val selectedLevel: Int = 1,
    val challengers: List<LeaderboardEntry> = emptyList(),
) : MviState
