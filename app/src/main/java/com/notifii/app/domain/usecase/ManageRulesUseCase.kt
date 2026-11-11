package com.notifii.app.domain.usecase

import com.notifii.app.domain.model.AlertRule
import com.notifii.app.domain.repository.RuleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ManageRulesUseCase @Inject constructor(
    private val ruleRepository: RuleRepository
) {

    fun getAllRules(): Flow<List<AlertRule>> =
        ruleRepository.getAllRules()

    fun getEnabledRules(): Flow<List<AlertRule>> =
        ruleRepository.getEnabledRules()

    suspend fun getRuleById(id: Long): AlertRule? =
        ruleRepository.getRuleById(id)

    fun observeRuleById(id: Long): Flow<AlertRule?> =
        ruleRepository.observeRuleById(id)

    suspend fun createRule(rule: AlertRule): Long =
        ruleRepository.insertRule(rule)

    suspend fun updateRule(rule: AlertRule) =
        ruleRepository.updateRule(rule)

    suspend fun deleteRule(id: Long) =
        ruleRepository.deleteRule(id)

    suspend fun toggleRule(id: Long, enabled: Boolean) =
        ruleRepository.setRuleEnabled(id, enabled)

    fun getActiveRuleCount(): Flow<Int> =
        ruleRepository.getActiveRuleCount()
}
