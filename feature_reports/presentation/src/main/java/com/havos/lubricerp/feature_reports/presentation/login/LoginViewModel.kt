package com.havos.lubricerp.feature_reports.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.core.network.AppEnvironment
import com.havos.lubricerp.core.network.ResolvedNetworkConfig
import com.havos.lubricerp.feature_reports.domain.usecase.LoginUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.ObserveRememberMeEnabledUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.ObserveRememberedUsernameUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    observeRememberedUsernameUseCase: ObserveRememberedUsernameUseCase,
    observeRememberMeEnabledUseCase: ObserveRememberMeEnabledUseCase,
    private val networkConfig: ResolvedNetworkConfig
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<LoginEffect>()
    val effect: SharedFlow<LoginEffect> = _effect.asSharedFlow()

    init {
        if (networkConfig.environment == AppEnvironment.TEST || networkConfig.environment == AppEnvironment.STAGE) {
            _state.update {
                it.copy(
                    username = DEFAULT_TEST_EMAIL,
                    password = DEFAULT_TEST_PASSWORD,
                    usernameError = null,
                    passwordError = null,
                    errorMessage = null
                )
            }
        } else {
            viewModelScope.launch {
                combine(
                    observeRememberedUsernameUseCase(),
                    observeRememberMeEnabledUseCase()
                ) { username, rememberMe ->
                    username to rememberMe
                }.collect { (username, rememberMe) ->
                    _state.update {
                        LoginReducer.reduceForRememberedUsername(it, username, rememberMe)
                    }
                }
            }
        }
    }

    fun onIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.UsernameChanged -> _state.update {
                LoginReducer
                    .reduceForUsername(it, intent.value)
                    .let { updated -> LoginReducer.reduceForUsernameValidation(updated, validateUsername(intent.value)) }
            }

            is LoginIntent.PasswordChanged -> _state.update {
                LoginReducer
                    .reduceForPassword(it, intent.value)
                    .let { updated -> LoginReducer.reduceForPasswordValidation(updated, validatePassword(intent.value)) }
            }

            is LoginIntent.RememberMeChanged -> _state.update {
                LoginReducer.reduceForRememberMe(
                    it,
                    intent.value
                )
            }

            LoginIntent.Submit -> doLogin()
        }
    }

    private fun doLogin() {
        val current = _state.value
        if (current.isLoading) return

        val usernameError = validateUsername(current.username)
        val passwordError = validatePassword(current.password)
        if (usernameError != null || passwordError != null) {
            _state.update {
                LoginReducer.reduceForUsernameValidation(it, usernameError)
                    .let { updated -> LoginReducer.reduceForPasswordValidation(updated, passwordError) }
                    .copy(errorMessage = "Please correct highlighted fields")
            }
            return
        }

        val normalizedIdentity = normalizeIdentity(current.username)
        viewModelScope.launch {
            _state.update { LoginReducer.reduceForLoading(it) }
            when (val result = loginUseCase(normalizedIdentity, current.password, current.rememberMe)) {
                is ResultState.Success -> {
                    _state.update { LoginReducer.reduceForSuccess(it) }
                    _effect.emit(LoginEffect.NavigateToHome)
                }

                is ResultState.Error -> {
                    _state.update { LoginReducer.reduceForError(it, result.message) }
                }

                ResultState.Loading -> _state.update { LoginReducer.reduceForLoading(it) }
            }
        }
    }

    private fun validateUsername(value: String): String? {
        val trimmed = value.trim()
        if (trimmed.isBlank()) return "Email or phone is required"
        return if (trimmed.contains("@")) {
            val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
            if (!emailRegex.matches(trimmed)) "Enter a valid email address" else null
        } else {
            val phoneRegex = Regex("^\\+?[0-9]{7,15}$")
            if (!phoneRegex.matches(trimmed)) "Enter a valid phone number" else null
        }
    }

    private fun validatePassword(value: String): String? {
        if (value.isBlank()) return "Password is required"
        if (value.length < 3) return "Password must be at least 3 characters"
        return null
    }

    private fun normalizeIdentity(value: String): String {
        val trimmed = value.trim()
        return if (trimmed.contains("@")) trimmed.lowercase() else trimmed
    }

    private companion object {
        const val DEFAULT_TEST_EMAIL = "admin@baseoils.com"
        const val DEFAULT_TEST_PASSWORD = "Admin@123"
    }
}
