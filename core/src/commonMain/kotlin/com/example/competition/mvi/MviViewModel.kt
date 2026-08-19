package com.example.competition.mvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Reusable MVI (Model-View-Intent) base class powering every feature.
 *
 * Standard one-way data flow:
 *   UI --dispatch(intent)--> (pure reduce) --> StateFlow<S> --> UI
 *                          \--> Emits [MviEffect] for side effects
 *
 * Guidelines for subclasses:
 *  - Override [reduce] for synchronous, pure state transitions.
 *  - Override [onIntent] for intents that trigger async work, changing state
 *    via [setState] once the work completes.
 *  - Inject an app/service scope via [scope] so the view model is lifecycle-safe.
 *
 * @param S immutable UI state
 * @param I user intent (sealed interface)
 * @param E one-shot effect (sealed interface)
 * @param scope coroutine scope used for async work
 */
abstract class MviViewModel<S : MviState, I : MviIntent, E : MviEffect>(
    initialState: S,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob()),
) {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<E>(extraBufferCapacity = 8)
    val effects: SharedFlow<E> = _effects.asSharedFlow()

    /** Dispatches an [intent] into the pipeline. */
    fun dispatch(intent: I) {
        onIntent(intent)
    }

    /**
     * Entry point for every intent. Default behavior forwards synchronously to
     * [reduce]; override to orchestrate async side effects then apply state.
     */
    protected open fun onIntent(intent: I) {
        reduce(intent)
    }

    /**
     * Pure reducer — maps an intent to a new state or no-op. Most subclasses
     * override [onIntent] instead and call [setState] themselves.
     */
    protected open fun reduce(intent: I): Unit = Unit

    /** Applies a synchronous state transition. */
    protected fun setState(transform: (S) -> S) {
        _state.update(transform)
    }

    /** Replaces the state with [newState]. */
    protected fun setState(newState: S) {
        _state.value = newState
    }

    /** Emits a one-shot effect consumed by the view. */
    protected fun emit(effect: E) {
        _effects.tryEmit(effect)
    }

    /** Launches a coroutine in the injected [scope]. */
    protected fun launch(block: suspend CoroutineScope.() -> Unit): Job =
        scope.launch(block = block)
}