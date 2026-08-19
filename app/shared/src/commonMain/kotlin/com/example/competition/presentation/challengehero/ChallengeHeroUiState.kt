package com.example.competition.presentation.challengehero

import com.example.competition.model.ChallengeRecord
import com.example.competition.model.ChallengeStats
import com.example.competition.mvi.MviState

/**
 * State for the challenge hero screen (challenge history + stats).
 */
data class ChallengeHeroUiState(
    val selectedTab: Int = 0,
    val stats: ChallengeStats = ChallengeStats(userId = ""),
    val challenges: List<ChallengeRecord> = emptyList(),
) : MviState
