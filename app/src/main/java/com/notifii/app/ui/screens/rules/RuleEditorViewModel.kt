package com.notifii.app.ui.screens.rules

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifii.app.domain.model.AlertRule
import com.notifii.app.domain.usecase.ManageRulesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RuleEditorState(
    val ruleId: Long = 0,
    val label: String = "",
    val appPackage: String = "",
    val appLabel: String = "Any App",
    val keywords: String = "",
    val excludeKeywords: String = "",
    val regexPattern: String = "",
    val senderFilter: String = "",
    val burstCount: Int = 0,
    val burstWindowMinutes: Int = 1,
    val scheduleStartHour: Int = -1,
    val scheduleStartMinute: Int = 0,
    val scheduleEndHour: Int = -1,
    val scheduleEndMinute: Int = 0,
    val scheduleDays: Int = 0x7F,
    val isEnabled: Boolean = true,
    val alarmSoundUri: String = "",
    val vibrationPattern: String = "0,500,200,500",
    val useTts: Boolean = false,
    val cooldownSeconds: Int = 0,
    val priority: Int = 0,
    val triggerOnScreenOff: Boolean = false,
    val isSaved: Boolean = false
)

@HiltViewModel
class RuleEditorViewModel @Inject constructor(
    private val manageRulesUseCase: ManageRulesUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val ruleId: Long = savedStateHandle.get<String>("ruleId")?.toLongOrNull() ?: 0L

    private val _uiState = MutableStateFlow(RuleEditorState(ruleId = ruleId))
    val uiState: StateFlow<RuleEditorState> = _uiState.asStateFlow()

    init {
        if (ruleId > 0) {
            loadRule(ruleId)
        }
    }

    private fun loadRule(id: Long) {
        viewModelScope.launch {
            val rule = manageRulesUseCase.getRuleById(id) ?: return@launch
            _uiState.update {
                it.copy(
                    ruleId = rule.id,
                    label = rule.label,
                    appPackage = rule.appPackage,
                    appLabel = if (rule.appPackage.isBlank()) "Any App" else rule.appPackage,
                    keywords = rule.keywords.joinToString(", "),
                    excludeKeywords = rule.excludeKeywords.joinToString(", "),
                    regexPattern = rule.regexPattern,
                    senderFilter = rule.senderFilter,
                    burstCount = rule.burstCount,
                    burstWindowMinutes = (rule.burstWindowMs / 60_000L).toInt(),
                    scheduleStartHour = rule.scheduleStartHour,
                    scheduleStartMinute = rule.scheduleStartMinute,
                    scheduleEndHour = rule.scheduleEndHour,
                    scheduleEndMinute = rule.scheduleEndMinute,
                    scheduleDays = rule.scheduleDays,
                    isEnabled = rule.isEnabled,
                    alarmSoundUri = rule.alarmSoundUri,
                    vibrationPattern = rule.vibrationPattern,
                    useTts = rule.useTts,
                    cooldownSeconds = (rule.cooldownMs / 1000L).toInt(),
                    priority = rule.priority,
                    triggerOnScreenOff = rule.triggerOnScreenOff
                )
            }
        }
    }

    fun onLabelChange(label: String) = _uiState.update { it.copy(label = label) }
    fun onAppSelected(packageName: String, appLabel: String) = _uiState.update { it.copy(appPackage = packageName, appLabel = appLabel) }
    fun onKeywordsChange(keywords: String) = _uiState.update { it.copy(keywords = keywords) }
    fun onExcludeKeywordsChange(exclude: String) = _uiState.update { it.copy(excludeKeywords = exclude) }
    fun onRegexChange(regex: String) = _uiState.update { it.copy(regexPattern = regex) }
    fun onSenderChange(sender: String) = _uiState.update { it.copy(senderFilter = sender) }
    fun onBurstCountChange(count: Int) = _uiState.update { it.copy(burstCount = count) }
    fun onBurstWindowChange(minutes: Int) = _uiState.update { it.copy(burstWindowMinutes = minutes) }
    fun onCooldownChange(seconds: Int) = _uiState.update { it.copy(cooldownSeconds = seconds) }
    fun onTtsToggle(enabled: Boolean) = _uiState.update { it.copy(useTts = enabled) }
    fun onScreenOffToggle(enabled: Boolean) = _uiState.update { it.copy(triggerOnScreenOff = enabled) }

    fun saveRule() {
        val state = _uiState.value
        val keywordsList = state.keywords.split(",").map { it.trim() }.filter { it.isNotBlank() }
        val excludeList = state.excludeKeywords.split(",").map { it.trim() }.filter { it.isNotBlank() }

        val rule = AlertRule(
            id = state.ruleId,
            appPackage = state.appPackage,
            label = state.label.ifBlank { "Alert Rule" },
            keywords = keywordsList,
            excludeKeywords = excludeList,
            regexPattern = state.regexPattern,
            senderFilter = state.senderFilter,
            burstCount = state.burstCount,
            burstWindowMs = state.burstWindowMinutes * 60_000L,
            scheduleStartHour = state.scheduleStartHour,
            scheduleStartMinute = state.scheduleStartMinute,
            scheduleEndHour = state.scheduleEndHour,
            scheduleEndMinute = state.scheduleEndMinute,
            scheduleDays = state.scheduleDays,
            isEnabled = state.isEnabled,
            alarmSoundUri = state.alarmSoundUri,
            vibrationPattern = state.vibrationPattern,
            useTts = state.useTts,
            cooldownMs = state.cooldownSeconds * 1000L,
            priority = state.priority,
            triggerOnScreenOff = state.triggerOnScreenOff,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        viewModelScope.launch {
            if (rule.id == 0L) {
                manageRulesUseCase.createRule(rule)
            } else {
                manageRulesUseCase.updateRule(rule)
            }
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
