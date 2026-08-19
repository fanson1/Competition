package com.example.competition.presentation.app

import com.example.competition.model.User
import com.example.competition.mvi.MviState

enum class Screen {
    MODE_SELECT, LOGIN, HOME, PROFILE, LEADERBOARD, CHALLENGE, CHALLENGE_HERO
}

/**
 * App-level navigation and session state.
 */
data class AppUiState(
    val currentScreen: Screen = Screen.MODE_SELECT,
    val user: User? = null,
    val isLoggedIn: Boolean = false,
    val isInitialized: Boolean = false,
) : MviState
