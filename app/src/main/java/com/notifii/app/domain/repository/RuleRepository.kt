package com.notifii.app.domain.repository

import com.notifii.app.domain.model.AlertRule
import kotlinx.coroutines.flow.Flow

interface RuleRepository {
    fun getAllRules(): Flow<List<AlertRule>>
    fun getEnabledRules(): Flow<List<AlertRule>>
    suspend fun getEnabledRulesSnapshot(): List<AlertRule>
    suspend fun getRuleById(id: Long): AlertRule?
    fun observeRuleById(id: Long): Flow<AlertRule?>
    suspend fun insertRule(rule: AlertRule): Long
    suspend fun updateRule(rule: AlertRule)
    suspend fun deleteRule(id: Long)
    suspend fun setRuleEnabled(id: Long, enabled: Boolean)
    fun getActiveRuleCount(): Flow<Int>
}
