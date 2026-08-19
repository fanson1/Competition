package com.example.competition.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.example.competition.mvi.MviEffect
import com.example.competition.mvi.MviIntent
import com.example.competition.mvi.MviState
import com.example.competition.mvi.MviViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * Reusable helpers for wiring MVI view models into Compose.
 */

/** Hosts an [MviViewModel] for the lifetime of the composition. */
@Composable
fun <S : MviState, I : MviIntent, E : MviEffect>
    rememberViewModel(create: (CoroutineScope) -> MviViewModel<S, I, E>): MviViewModel<S, I, E> {
    val scope = rememberCoroutineScope()
    val vm = remember { create(scope) }
    DisposableEffect(vm) {
        onDispose { }
    }
    return vm
}

/** Collects one-shot effects from a view model and forwards them to [onEffect]. */
@Composable
fun <E : MviEffect> MviEffectCollector(
    vm: MviViewModel<*, *, E>,
    onEffect: (E) -> Unit,
) {
    LaunchedEffect(vm) {
        vm.effects.collect { onEffect(it) }
    }
}