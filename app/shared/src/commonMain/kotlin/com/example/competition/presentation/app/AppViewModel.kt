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
            AppIntent.ModeSelectionComplete -> navigateTo(Screen.LOGIN, clearStack = true)
            AppIntent.LoginSuccess -> onLoginSuccess()
            AppIntent.RefreshUser -> setState { it.copy(user = UserManager.getCurrentUser()) }
            AppIntent.Logout -> logout()
            is AppIntent.Navigate -> navigateTo(intent.screen)
            AppIntent.Back -> goBack()
            AppIntent.GoHome -> navigateTo(Screen.HOME, clearStack = true)
        }
    }

    private fun navigateTo(screen: Screen, clearStack: Boolean = false) {
        setState {
            if (screen == it.currentScreen) return@setState it
            it.copy(
                backStack = if (clearStack) emptyList() else it.backStack + it.currentScreen,
                currentScreen = screen
            )
        }
    }

    private fun goBack() {
        setState {
            if (it.backStack.isEmpty()) it
            else it.copy(
                currentScreen = it.backStack.last(),
                backStack = it.backStack.dropLast(1)
            )
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
                                    val restoredUser = UserManager.getCurrentUser()
                                    setState { it.copy(user = restoredUser, isLoggedIn = true) }
                                    GamePreferences.setUserId(restoredUser?.id)
                                    ApiClient.getMe().onSuccess { me ->
                                        val profile = me.profile
                                        val sessionUser = UserManager.getCurrentUser()
                                        if (profile != null && sessionUser != null) {
                                            val userProfile = UserProfile(
                                                user = sessionUser,
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

            setState { it.copy(currentScreen = screen, backStack = emptyList(), isInitialized = true) }
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
        setState { it.copy(currentScreen = Screen.HOME, backStack = emptyList()) }
    }

    private fun logout() {
        UserManager.logout()
        GamePreferences.setUserId(null)
        emit(AppEffect.ReloadGameProgress)
        setState {
            it.copy(
                user = null,
                isLoggedIn = false,
                currentScreen = Screen.LOGIN,
                backStack = emptyList()
            )
        }
    }
}