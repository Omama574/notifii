package com.notifii.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifii.app.domain.repository.SettingsRepository
import com.notifii.app.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val dndOverride: Boolean = true,
    val maxVolume: Boolean = true,
    val flipToSilence: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val dnd = settingsRepository.getBoolean(Constants.PREF_KEY_DND_OVERRIDE, true)
            val vol = settingsRepository.getBoolean(Constants.PREF_KEY_MAX_VOLUME, true)
            val flip = settingsRepository.getBoolean(Constants.PREF_KEY_FLIP_TO_SILENCE, true)
            _uiState.value = SettingsUiState(
                dndOverride = dnd,
                maxVolume = vol,
                flipToSilence = flip
            )
        }
    }

    fun setDndOverride(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setBoolean(Constants.PREF_KEY_DND_OVERRIDE, enabled)
            _uiState.update { it.copy(dndOverride = enabled) }
        }
    }

    fun setMaxVolume(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setBoolean(Constants.PREF_KEY_MAX_VOLUME, enabled)
            _uiState.update { it.copy(maxVolume = enabled) }
        }
    }

    fun setFlipToSilence(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setBoolean(Constants.PREF_KEY_FLIP_TO_SILENCE, enabled)
            _uiState.update { it.copy(flipToSilence = enabled) }
        }
    }
}
