package com.example.competition.presentation.profile

import com.example.competition.mvi.MviEffect
import com.example.competition.mvi.MviIntent

sealed interface ProfileIntent : MviIntent {
    data class ToggleOnline(val online: Boolean) : ProfileIntent
    data class UpdateNickname(val nickname: String) : ProfileIntent
    data class UpdateAvatar(val emoji: String) : ProfileIntent
    data class ChangePassword(val oldPassword: String, val newPassword: String) : ProfileIntent
    data object Logout : ProfileIntent
    data object Back : ProfileIntent
}

sealed interface ProfileEffect : MviEffect {
    data object ProfileUpdated : ProfileEffect
    data class ChangePasswordResult(val result: Result<Unit>) : ProfileEffect
    data object Logout : ProfileEffect
    data object Back : ProfileEffect
}
