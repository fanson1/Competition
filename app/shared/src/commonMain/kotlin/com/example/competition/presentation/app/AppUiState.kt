package com.example.competition.presentation.app

import com.example.competition.model.User
import com.example.competition.mvi.MviState

enum class Screen {
    MODE_SELECT, LOGIN, HOME, PROFILE, LEADERBOARD, CHALLENGE, CHALLENGE_HERO
}

/**
 * App-level navigation and session state.
 *
 * [backStack] holds the screens visited before [currentScreen] so the system
 * back gesture can return through the user's actual navigation history.
 */
data class AppUiState(
    val currentScreen: Screen = Screen.MODE_SELECT,
    val backStack: List<Screen> = emptyList(),
    val user: User? = null,
    val isLoggedIn: Boolean = false,
    val isInitialized: Boolean = false,
) : MviState {
    val canGoBack: Boolean get() = backStack.isNotEmpty()
}
