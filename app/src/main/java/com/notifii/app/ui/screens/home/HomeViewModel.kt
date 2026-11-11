package com.notifii.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifii.app.data.local.entity.NotificationLog
import com.notifii.app.domain.model.AlertRule
import com.notifii.app.domain.repository.NotificationLogRepository
import com.notifii.app.domain.repository.RuleRepository
import com.notifii.app.domain.repository.SettingsRepository
import com.notifii.app.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val activeRuleCount: Int = 0,
    val totalAlertCount: Int = 0,
    val isMasterEnabled: Boolean = true,
    val recentAlerts: List<NotificationLog> = emptyList(),
    val topRules: List<AlertRule> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val ruleRepository: RuleRepository,
    private val logRepository: NotificationLogRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _isMasterEnabled = MutableStateFlow(true)

    init {
        viewModelScope.launch {
            _isMasterEnabled.value = settingsRepository.getBoolean(Constants.PREF_KEY_MASTER_ENABLED, true)
        }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        ruleRepository.getActiveRuleCount(),
        logRepository.getTotalAlertCount(),
        logRepository.getRecentAlerts(5),
        ruleRepository.getAllRules(),
        _isMasterEnabled
    ) { activeRules, totalAlerts, recentAlerts, allRules, masterEnabled ->
        HomeUiState(
            activeRuleCount = activeRules,
            totalAlertCount = totalAlerts,
            isMasterEnabled = masterEnabled,
            recentAlerts = recentAlerts,
            topRules = allRules.take(3)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun toggleMasterSwitch(enabled: Boolean) {
        viewModelScope.launch {
            _isMasterEnabled.value = enabled
            settingsRepository.setBoolean(Constants.PREF_KEY_MASTER_ENABLED, enabled)
        }
    }

    fun toggleRule(ruleId: Long, enabled: Boolean) {
        viewModelScope.launch {
            ruleRepository.setRuleEnabled(ruleId, enabled)
        }
    }
}
