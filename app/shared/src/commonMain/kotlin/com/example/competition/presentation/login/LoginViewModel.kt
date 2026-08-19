package com.example.competition.presentation.login

import com.example.competition.api.ApiClient
import com.example.competition.api.dto.AuthResponse
import com.example.competition.api.dto.UserDto
import com.example.competition.data.GamePreferences
import com.example.competition.data.UserManager
import com.example.competition.model.User
import com.example.competition.model.UserProfile
import com.example.competition.mvi.MviViewModel
import com.example.competition.repository.ModeRouter
import com.example.competition.repository.bridge.RepositoryBridge
import com.example.competition.util.LevelMapCodec

class LoginViewModel : MviViewModel<LoginUiState, LoginIntent, LoginEffect>(LoginUiState()) {

    override fun onIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.SetMode -> setState { it.copy(mode = intent.mode, error = null) }
            is LoginIntent.UpdateUsername -> setState { it.copy(username = intent.value, error = null) }
            is LoginIntent.UpdatePassword -> setState { it.copy(password = intent.value, error = null) }
            is LoginIntent.UpdateConfirmPassword -> setState { it.copy(confirmPassword = intent.value, error = null) }
            is LoginIntent.UpdateNickname -> setState { it.copy(nickname = intent.value, error = null) }
            LoginIntent.Submit -> submit()
        }
    }

    private fun submit() {
        val s = state.value
        if (s.isLoading) return

        if (s.mode == LoginMode.REGISTER && s.password != s.confirmPassword) {
            setState { it.copy(error = LoginError.PasswordMismatch) }
            return
        }

        setState { it.copy(isLoading = true, error = null) }
        launch {
            val result = if (s.mode == LoginMode.LOGIN) {
                performLogin(s.username, s.password)
            } else {
                performRegister(s.username, s.password, s.nickname)
            }
            result.onSuccess {
                setState { it.copy(isLoading = false) }
                emit(LoginEffect.LoginSuccess)
            }.onFailure { e ->
                setState { it.copy(isLoading = false, error = LoginError.Message(e.message ?: "操作失败")) }
            }
        }
    }

    private suspend fun performLogin(username: String, password: String): Result<Unit> {
        return if (ModeRouter.isOnline()) {
            ApiClient.login(username, password).map { authResp ->
                applyOnlineAuth(authResp)
            }
        } else {
            UserManager.login(username, password).map { }
        }
    }

    private suspend fun performRegister(username: String, password: String, nickname: String): Result<Unit> {
        return if (ModeRouter.isOnline()) {
            ApiClient.register(username, password, nickname).map { authResp ->
                applyOnlineAuth(authResp)
            }
        } else {
            UserManager.register(username, password, nickname).map { }
        }
    }

    private suspend fun applyOnlineAuth(authResp: AuthResponse) {
        ApiClient.setToken(authResp.token)
        val dto = authResp.user
        val user = dto.toUser()
        RepositoryBridge.getRemoteUser().loadFromRemote(dto)
        UserManager.setCurrentUser(user)

        ApiClient.getMe().onSuccess { me ->
            val profile = me.profile
            if (profile != null) {
                val userProfile = UserProfile(
                    user = user,
                    totalScore = profile.totalScore,
                    maxLevel = profile.maxLevel,
                    totalCorrectCount = profile.totalCorrectCount,
                    totalGamesPlayed = profile.totalGamesPlayed,
                    maxStreak = profile.maxStreak,
                    levelScores = LevelMapCodec.decode(profile.levelScores),
                    levelCorrectCounts = LevelMapCodec.decode(profile.levelCorrectCounts),
                    completedLevels = profile.completedLevels.toSet()
                )
                RepositoryBridge.getRemoteUser().loadProfile(userProfile)
                UserManager.setCurrentProfile(userProfile)
                GamePreferences.saveProgressFromProfile(userProfile)
            }
        }
    }

    private fun UserDto.toUser(): User {
        return User(
            id = id,
            username = username,
            passwordHash = "",
            nickname = nickname,
            avatarEmoji = avatarEmoji,
            createdAt = createdAt,
            lastLoginAt = lastLoginAt
        )
    }
}
