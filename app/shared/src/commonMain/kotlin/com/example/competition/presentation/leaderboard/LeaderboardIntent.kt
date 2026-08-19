package com.example.competition.presentation.leaderboard

import com.example.competition.mvi.MviEffect
import com.example.competition.mvi.MviIntent

sealed interface LeaderboardIntent : MviIntent {
    data class SelectTab(val tab: Int) : LeaderboardIntent
    data object Refresh : LeaderboardIntent
    data object Back : LeaderboardIntent
}

sealed interface LeaderboardEffect : MviEffect {
    data object Back : LeaderboardEffect
}
