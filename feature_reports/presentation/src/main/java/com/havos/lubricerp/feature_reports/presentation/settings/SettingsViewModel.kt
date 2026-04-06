package com.havos.lubricerp.feature_reports.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.havos.lubricerp.core.database.SecureSessionStore
import com.havos.lubricerp.feature_reports.domain.usecase.ObserveProfileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val secureSessionStore: SecureSessionStore,
    private val observeProfileUseCase: ObserveProfileUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

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
        }
    }
}
