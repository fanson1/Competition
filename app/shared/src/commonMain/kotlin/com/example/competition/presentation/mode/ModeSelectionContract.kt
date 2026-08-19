package com.example.competition.presentation.mode

import com.example.competition.mvi.MviEffect
import com.example.competition.mvi.MviIntent
import com.example.competition.mvi.MviState
import com.example.competition.repository.AppMode

/**
 * State for the initial online/offline mode selection screen.
 */
data class ModeSelectionUiState(
    val selectedMode: AppMode? = null,
) : MviState

sealed interface ModeSelectionIntent : MviIntent {
    data class SelectMode(val mode: AppMode) : ModeSelectionIntent
    data object Confirm : ModeSelectionIntent
}

sealed interface ModeSelectionEffect : MviEffect {
    /** Mode has been persisted; navigate to login. */
    data object Complete : ModeSelectionEffect
}
