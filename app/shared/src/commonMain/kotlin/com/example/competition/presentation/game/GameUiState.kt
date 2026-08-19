package com.example.competition.presentation.game

import com.example.competition.model.GameState
import com.example.competition.model.LeaderboardEntry
import com.example.competition.mvi.MviState

/**
 * UI state for the quiz/game feature.
 */
data class GameUiState(
    val game: GameState = GameState(),
    val isLoading: Boolean = true,
    val challengeTarget: LeaderboardEntry? = null,
) : MviState