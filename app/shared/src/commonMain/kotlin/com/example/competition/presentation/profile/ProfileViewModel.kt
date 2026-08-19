package com.example.competition.presentation.profile

import com.example.competition.api.ApiClient
import com.example.competition.data.UserManager
import com.example.competition.mvi.MviViewModel
import com.example.competition.repository.ModeController
import com.example.competition.repository.ModeRouter

class ProfileViewModel : MviViewModel<ProfileUiState, ProfileIntent, ProfileEffect>(ProfileUiState()) {

    init {
        launch {
            ModeController.isSyncing.collect { syncing ->
                setState { it.copy(isSyncing = syncing) }
            }
        }
        launch {
            ModeController.events.collect { event ->
                setState {
                    when (event) {
                        ModeController.ModeEvent.NeedLogin ->
                            it.copy(syncError = ProfileSyncError.NeedLogin)
                        is ModeController.ModeEvent.Error ->
                            it.copy(syncError = ProfileSyncError.Message(event.message))
                        else -> it.copy(syncError = null)
                    }
                }
            }
        }
    }

    override fun onIntent(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.ToggleOnline -> toggleOnline(intent.online)
            is ProfileIntent.UpdateNickname -> updateNickname(intent.nickname)
            is ProfileIntent.UpdateAvatar -> updateAvatar(intent.emoji)
            is ProfileIntent.ChangePassword -> changePassword(intent.oldPassword, intent.newPassword)
            ProfileIntent.Logout -> logout()
            ProfileIntent.Back -> emit(ProfileEffect.Back)
        }
    }

    private fun toggleOnline(online: Boolean) {
        launch { ModeController.switchTo(online) }
    }

    private fun updateNickname(nickname: String) {
        launch {
            UserManager.updateProfile(nickname = nickname)
            if (ModeRouter.isOnline()) {
                ApiClient.updateProfile(nickname, null)
            }
            emit(ProfileEffect.ProfileUpdated)
        }
    }

    private fun updateAvatar(emoji: String) {
        launch {
            UserManager.updateProfile(avatarEmoji = emoji)
            if (ModeRouter.isOnline()) {
                ApiClient.updateProfile(null, emoji)
            }
            emit(ProfileEffect.ProfileUpdated)
        }
    }

    private fun changePassword(oldPassword: String, newPassword: String) {
        launch {
            val result = if (ModeRouter.isOnline()) {
                ApiClient.changePassword(oldPassword, newPassword)
            } else {
                UserManager.changePassword(oldPassword, newPassword)
            }
            emit(ProfileEffect.ChangePasswordResult(result))
        }
    }

    private fun logout() {
        if (ModeRouter.isOnline()) {
            ApiClient.setToken(null)
        }
        UserManager.logout()
        emit(ProfileEffect.Logout)
    }
}
