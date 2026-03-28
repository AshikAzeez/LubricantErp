package com.havos.lubricerp.feature_reports.presentation.login

import com.havos.lubricerp.core.common.UiIntent
import com.havos.lubricerp.core.common.UiState

sealed interface LoginIntent : UiIntent {
    data class UsernameChanged(val value: String) : LoginIntent
    data class PasswordChanged(val value: String) : LoginIntent
    data class RememberMeChanged(val value: Boolean) : LoginIntent
    data object Submit : LoginIntent
}

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val rememberMe: Boolean = false,
    val usernameError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : UiState

sealed interface LoginEffect {
    data object NavigateToHome : LoginEffect
}

sealed interface LoginAction {
    data class UsernameChanged(val value: String) : LoginAction
    data class PasswordChanged(val value: String) : LoginAction
    data class RememberMeChanged(val value: Boolean) : LoginAction
    data object Submit : LoginAction
}
