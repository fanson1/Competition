package com.example.competition.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.competition.repository.AppMode
import com.example.competition.repository.ModeController
import com.example.competition.repository.ModeRouter
import com.example.competition.ui.theme.QuizPalette
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import competition.app.shared.generated.resources.Res
import competition.app.shared.generated.resources.mode_need_login
import competition.app.shared.generated.resources.mode_offline
import competition.app.shared.generated.resources.mode_online
import competition.app.shared.generated.resources.mode_sync_failed
import competition.app.shared.generated.resources.mode_switched_offline
import competition.app.shared.generated.resources.mode_switched_online

/**
 * Tap-to-toggle online/offline pill. Reflects the current [ModeRouter] mode,
 * shows a spinner while [ModeController.isSyncing], and surfaces light
 * feedback (need-login / error / switched) through [snackbarHostState].
 */
@Composable
fun ModeChip(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState? = null
) {
    val scope = rememberCoroutineScope()
    val currentMode by ModeRouter.currentMode.collectAsState()
    val isSyncing by ModeController.isSyncing.collectAsState()
    val online = currentMode == AppMode.ONLINE

    val needLoginText = stringResource(Res.string.mode_need_login)
    val syncFailedText = stringResource(Res.string.mode_sync_failed)
    val switchedOnlineText = stringResource(Res.string.mode_switched_online)
    val switchedOfflineText = stringResource(Res.string.mode_switched_offline)

    LaunchedEffect(Unit) {
        ModeController.events.collect { event ->
            val message = when (event) {
                ModeController.ModeEvent.NeedLogin -> needLoginText
                is ModeController.ModeEvent.Error -> syncFailedText.format(event.message)
                ModeController.ModeEvent.SwitchedOnline -> switchedOnlineText
                ModeController.ModeEvent.SwitchedOffline -> switchedOfflineText
            }
            snackbarHostState?.showSnackbar(message)
        }
    }

    Box(contentAlignment = Alignment.Center) {
        QuizChip(
            text = stringResource(if (online) Res.string.mode_online else Res.string.mode_offline),
            color = if (online) QuizPalette.Success else QuizPalette.TextSecondary,
            emoji = if (online) "🛜" else "✈️",
            modifier = modifier.clickable(enabled = !isSyncing) {
                scope.launch { ModeController.switchTo(!online) }
            }
        )
        if (isSyncing) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = QuizPalette.Gold
            )
        }
    }
}
