package com.example.competition.mvi

/**
 * Reusable MVI (Model-View-Intent) architecture primitives used by every feature.
 *
 * A feature is modelled as:
 *  - [MviState]   immutable snapshot of what the UI should render
 *  - [MviIntent]  one-way user intent dispatched to the ViewModel
 *  - [MviEffect]  one-shot side effects (navigation, toasts) consumed by the UI
 */
interface MviState

interface MviIntent

interface MviEffect