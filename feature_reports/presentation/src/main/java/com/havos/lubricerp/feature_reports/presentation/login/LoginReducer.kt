package com.havos.lubricerp.feature_reports.presentation.login

object LoginReducer {
    fun reduceForUsername(state: LoginUiState, value: String): LoginUiState {
        return state.copy(username = value, usernameError = null, errorMessage = null)
    }

    fun reduceForPassword(state: LoginUiState, value: String): LoginUiState {
        return state.copy(password = value, passwordError = null, errorMessage = null)
    }

    fun reduceForRememberMe(state: LoginUiState, value: Boolean): LoginUiState {
        return state.copy(rememberMe = value)
    }

    fun reduceForRememberedUsername(
        state: LoginUiState,
        username: String,
        rememberMe: Boolean
    ): LoginUiState {
        return state.copy(
            username = username,
            password = "",
            rememberMe = rememberMe,
            usernameError = null,
            passwordError = null
        )
    }

    fun reduceForUsernameValidation(state: LoginUiState, message: String?): LoginUiState {
        return state.copy(usernameError = message)
    }

    fun reduceForPasswordValidation(state: LoginUiState, message: String?): LoginUiState {
        return state.copy(passwordError = message)
    }

    fun reduceForLoading(state: LoginUiState): LoginUiState {
        return state.copy(isLoading = true, errorMessage = null, usernameError = null, passwordError = null)
    }

    fun reduceForError(state: LoginUiState, message: String): LoginUiState {
        return state.copy(isLoading = false, errorMessage = message)
    }

    fun reduceForSuccess(state: LoginUiState): LoginUiState {
        return state.copy(isLoading = false, errorMessage = null)
    }
}
