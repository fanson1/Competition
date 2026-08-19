package com.example.competition.presentation.profile

import com.example.competition.mvi.MviState

sealed interface ProfileSyncError {
    data object NeedLogin : ProfileSyncError
    data class Message(val text: String) : ProfileSyncError
}

/**
 * State for profile actions (mode switch syncing, dialogs stay local to the UI).
 */
data class ProfileUiState(
    val isSyncing: Boolean = false,
    val syncError: ProfileSyncError? = null,
) : MviState
