package com.example.competition.presentation.app

import com.example.competition.api.ApiClient
import com.example.competition.data.GamePreferences
import com.example.competition.data.UserManager
import com.example.competition.model.UserProfile
import com.example.competition.mvi.MviViewModel
import com.example.competition.repository.AppMode
import com.example.competition.repository.ModeRouter
import com.example.competition.repository.bridge.RepositoryBridge
import com.example.competition.util.LevelMapCodec

/**
 * Owns the app-level startup, session and navigation state.
 */
class AppViewModel : MviViewModel<AppUiState, AppIntent, AppEffect>(AppUiState()) {

    override fun onIntent(intent: AppIntent) {
        when (intent) {
            AppIntent.Init -> init()
            AppIntent.ModeSelectionComplete -> setState { it.copy(currentScreen = Screen.LOGIN) }
            AppIntent.LoginSuccess -> onLoginSuccess()
            AppIntent.RefreshUser -> setState { it.copy(user = UserManager.getCurrentUser()) }
            AppIntent.Logout -> logout()
            is AppIntent.Navigate -> setState { it.copy(currentScreen = intent.screen) }
        }
    }

    private fun init() {
        launch {
            UserManager.init()
            ApiClient.init()
            ModeRouter.init()

            var screen = Screen.MODE_SELECT

            if (ModeRouter.isModeSelected()) {
                when (ModeRouter.getMode()) {
                    AppMode.OFFLINE -> {
                        // 恢复本地会话（若有），直接进入 HOME
                        val user = UserManager.getCurrentUser()
                        setState { it.copy(user = user, isLoggedIn = user != null) }
                        if (user != null) {
                            GamePreferences.setUserId(user.id)
                            emit(AppEffect.ReloadGameProgress)
                        }
                        screen = Screen.HOME
                    }
                    AppMode.ONLINE -> {
                        if (!ApiClient.isAuthenticated()) {
                            // 无登录态 → 让用户重新选模式 / 登录
                            screen = Screen.MODE_SELECT
                        } else {
                            val verify = ApiClient.verifyToken()
                            val vr = verify.getOrNull()
                            when {
                                vr != null && vr.valid && vr.user != null -> {
                                    // 服务端可达 + token 有效 → 载入远程数据，进 HOME
                                    val remoteUser = RepositoryBridge.getRemoteUser()
                                    remoteUser.loadFromRemote(vr.user)
                                    setState { it.copy(user = UserManager.getCurrentUser(), isLoggedIn = true) }
                                    GamePreferences.setUserId(state.value.user?.id)
                                    ApiClient.getMe().onSuccess { me ->
                                        val profile = me.profile
                                        if (profile != null) {
                                            val userProfile = UserProfile(
                                                user = UserManager.getCurrentUser()!!,
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
                                    emit(AppEffect.ReloadGameProgress)
                                    screen = Screen.HOME
                                }
                                vr != null && !vr.valid -> {
                                    // server 在但 token 失效 → 进入登录页重新获取
                                    screen = Screen.LOGIN
                                }
                                verify.isFailure -> {
                                    // 服务端未启动 / 不可达 → 返回模式选择页
                                    screen = Screen.MODE_SELECT
                                }
                            }
                        }
                    }
                }
            }

            setState { it.copy(currentScreen = screen, isInitialized = true) }
        }
    }

    private fun onLoginSuccess() {
        val user = UserManager.getCurrentUser()
        setState { it.copy(user = user, isLoggedIn = true) }
        GamePreferences.setUserId(user?.id)
        emit(AppEffect.ReloadGameProgress)
        if (ModeRouter.isOnline()) {
            emit(AppEffect.SyncLeaderboard)
        }
        setState { it.copy(currentScreen = Screen.HOME) }
    }

    private fun logout() {
        UserManager.logout()
        GamePreferences.setUserId(null)
        emit(AppEffect.ReloadGameProgress)
        setState { it.copy(user = null, isLoggedIn = false, currentScreen = Screen.LOGIN) }
    }
}