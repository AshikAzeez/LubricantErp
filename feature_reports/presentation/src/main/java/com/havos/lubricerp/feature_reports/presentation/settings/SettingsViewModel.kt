package com.havos.lubricerp.feature_reports.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.havos.lubricerp.core.common.AppFlavor
import com.havos.lubricerp.core.database.SecureProfileStore
import com.havos.lubricerp.core.database.SecureSessionStore
import com.havos.lubricerp.feature_reports.domain.usecase.LogoutUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.ObserveProfileUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val secureSessionStore: SecureSessionStore,
    private val secureProfileStore: SecureProfileStore,
    private val appFlavor: AppFlavor,
    private val observeProfileUseCase: ObserveProfileUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(
        SettingsUiState(flavorDisplaySuffix = appFlavor.displaySuffix)
    )
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<SettingsEffect>()
    val effect: SharedFlow<SettingsEffect> = _effect.asSharedFlow()

    init {
        viewModelScope.launch {
            secureSessionStore.themeModeFlow.collect { mode ->
                _state.update { it.copy(selectedThemeMode = mode) }
            }
        }

        viewModelScope.launch {
            observeProfileUseCase().collect { profile ->
                _state.update {
                    it.copy(
                        profile = profile?.let { user ->
                            SettingsProfileUi(
                                fullName = user.fullName,
                                email = user.email,
                                branchId = user.branchId,
                                rolesText = user.roles.joinToString(", ")
                            )
                        }
                    )
                }
            }
        }
    }

    fun onIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.ThemeChanged -> {
                viewModelScope.launch {
                    secureSessionStore.setThemeMode(intent.mode)
                }
            }
            is SettingsIntent.LogoutClicked -> {
                viewModelScope.launch {
                    logoutUseCase()
                    _effect.emit(SettingsEffect.NavigateToLogin)
                }
            }
            SettingsIntent.ClearCacheClicked -> {
                _state.update { it.copy(showClearCacheDialog = true) }
            }
            SettingsIntent.DismissCacheDialog -> {
                _state.update { it.copy(showClearCacheDialog = false) }
            }
            SettingsIntent.ConfirmClearCache -> {
                viewModelScope.launch {
                    secureProfileStore.clearProfile()
                    _state.update { it.copy(showClearCacheDialog = false) }
                    _effect.emit(SettingsEffect.CacheCleared)
                }
            }
        }
    }
}
