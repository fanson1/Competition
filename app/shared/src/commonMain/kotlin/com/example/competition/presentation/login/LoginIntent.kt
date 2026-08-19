package com.example.competition.presentation.login

import com.example.competition.mvi.MviIntent

sealed interface LoginIntent : MviIntent {
    data class SetMode(val mode: LoginMode) : LoginIntent
    data class UpdateUsername(val value: String) : LoginIntent
    data class UpdatePassword(val value: String) : LoginIntent
    data class UpdateConfirmPassword(val value: String) : LoginIntent
    data class UpdateNickname(val value: String) : LoginIntent
    data object Submit : LoginIntent
}
