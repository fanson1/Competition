package com.example.competition.presentation.leaderboard

import com.example.competition.mvi.MviViewModel
import com.example.competition.repository.bridge.RepositoryBridge

class LeaderboardViewModel : MviViewModel<LeaderboardUiState, LeaderboardIntent, LeaderboardEffect>(LeaderboardUiState()) {

    override fun onIntent(intent: LeaderboardIntent) {
        when (intent) {
            is LeaderboardIntent.SelectTab -> {
                setState { it.copy(selectedTab = intent.tab) }
                load(intent.tab)
            }
            LeaderboardIntent.Refresh -> load(state.value.selectedTab)
            LeaderboardIntent.Back -> emit(LeaderboardEffect.Back)
        }
    }

    private fun load(tab: Int) {
        launch {
            setState { it.copy(isLoading = true) }
            val entries = try {
                if (tab == 0) {
                    RepositoryBridge.leaderboard().getLeaderboard()
                } else {
                    RepositoryBridge.leaderboard().getLeaderboard(level = tab)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
            setState { it.copy(entries = entries, isLoading = false) }
        }
    }
}
