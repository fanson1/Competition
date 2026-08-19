package com.example.competition.presentation.app

import com.example.competition.mvi.MviEffect

sealed interface AppEffect : MviEffect {
    /** Ask the game feature to reload progress after login/logout. */
    data object ReloadGameProgress : AppEffect

    /** Ask the game feature to refresh the leaderboard from the server. */
    data object SyncLeaderboard : AppEffect
}
