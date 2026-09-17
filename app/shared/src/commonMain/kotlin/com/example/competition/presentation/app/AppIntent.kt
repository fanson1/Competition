package com.example.competition.presentation.app

import com.example.competition.mvi.MviIntent

sealed interface AppIntent : MviIntent {
    /** Runs startup: init managers, mode restore, auto-login. */
    data object Init : AppIntent

    /** User finished the initial mode selection. */
    data object ModeSelectionComplete : AppIntent

    /** A successful login/register happened (local or remote). */
    data object LoginSuccess : AppIntent

    /** Re-read the current user from [com.example.competition.data.UserManager]. */
    data object RefreshUser : AppIntent

    /** Logs the current user out and returns to the login screen. */
    data object Logout : AppIntent

    /** Navigates forward to [screen], pushing the current screen onto the back stack. */
    data class Navigate(val screen: Screen) : AppIntent

    /** Pops the back stack, returning to the previously visited screen. */
    data object Back : AppIntent

    /** Navigates to the home screen, clearing navigation history. */
    data object GoHome : AppIntent
}
