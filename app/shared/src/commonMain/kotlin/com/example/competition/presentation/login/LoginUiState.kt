package com.example.competition.presentation.login

import com.example.competition.mvi.MviState

enum class LoginMode { LOGIN, REGISTER }

sealed interface LoginError {
    data object PasswordMismatch : LoginError
    data class Message(val text: String) : LoginError
}

/**
 * Form and auth state for the login/register screen.
 */
data class LoginUiState(
    val mode: LoginMode = LoginMode.LOGIN,
    val username: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nickname: String = "",
    val isLoading: Boolean = false,
    val error: LoginError? = null,
) : MviState
