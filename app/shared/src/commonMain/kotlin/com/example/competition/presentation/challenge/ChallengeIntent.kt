package com.example.competition.presentation.challenge

import com.example.competition.model.LeaderboardEntry
import com.example.competition.mvi.MviEffect
import com.example.competition.mvi.MviIntent

sealed interface ChallengeIntent : MviIntent {
    data class SelectLevel(val level: Int) : ChallengeIntent
    data class StartChallenge(val target: LeaderboardEntry) : ChallengeIntent
    data object Back : ChallengeIntent
}

sealed interface ChallengeEffect : MviEffect {
    data class StartChallenge(val level: Int, val target: LeaderboardEntry) : ChallengeEffect
    data object Back : ChallengeEffect
}
