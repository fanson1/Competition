package com.example.competition.repository

import com.example.competition.api.ApiClient
import com.example.competition.data.UserManager
import com.example.competition.sync.SyncManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Single source of truth for switching between online and offline modes.
 * Decouples the switch logic (sync + login checks) from individual screens
 * so any screen (Home, Profile, …) can trigger a switch with light feedback.
 */
object ModeController {

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _events = MutableSharedFlow<ModeEvent>(extraBufferCapacity = 8)
    val events = _events.asSharedFlow()

    sealed interface ModeEvent {
        data object NeedLogin : ModeEvent
        data class Error(val message: String) : ModeEvent
        data object SwitchedOnline : ModeEvent
        data object SwitchedOffline : ModeEvent
    }

    suspend fun switchTo(online: Boolean) {
        if (online == ModeRouter.isOnline()) return
        if (online) {
            if (!UserManager.isLoggedIn()) {
                _events.tryEmit(ModeEvent.NeedLogin)
                return
            }
            _isSyncing.value = true
            try {
                SyncManager.syncToOnline()
                    .onSuccess {
                        ModeRouter.switch(AppMode.ONLINE)
                        _events.tryEmit(ModeEvent.SwitchedOnline)
                    }
                    .onFailure { e ->
                        _events.tryEmit(ModeEvent.Error(e.message ?: "同步失败"))
                    }
            } finally {
                _isSyncing.value = false
            }
        } else {
            ModeRouter.switch(AppMode.OFFLINE)
            _events.tryEmit(ModeEvent.SwitchedOffline)
        }
    }
}
