package com.example.competition.presentation.login

import com.example.competition.mvi.MviEffect

sealed interface LoginEffect : MviEffect {
    /** Emitted after a successful local/remote login or registration. */
    data object LoginSuccess : LoginEffect
}
