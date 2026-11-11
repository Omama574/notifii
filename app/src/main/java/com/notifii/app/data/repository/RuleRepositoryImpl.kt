package com.notifii.app.data.repository

import com.notifii.app.data.local.dao.RuleDao
import com.notifii.app.data.local.entity.Rule
import com.notifii.app.domain.model.AlertRule
import com.notifii.app.domain.repository.RuleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RuleRepositoryImpl @Inject constructor(
    private val ruleDao: RuleDao
) : RuleRepository {

    override fun getAllRules(): Flow<List<AlertRule>> =
        ruleDao.getAllRules().map { rules -> rules.map { it.toDomain() } }

    override fun getEnabledRules(): Flow<List<AlertRule>> =
        ruleDao.getEnabledRules().map { rules -> rules.map { it.toDomain() } }

    override suspend fun getEnabledRulesSnapshot(): List<AlertRule> =
        ruleDao.getEnabledRulesSnapshot().map { it.toDomain() }

    override suspend fun getRuleById(id: Long): AlertRule? =
        ruleDao.getRuleById(id)?.toDomain()

    override fun observeRuleById(id: Long): Flow<AlertRule?> =
        ruleDao.observeRuleById(id).map { it?.toDomain() }

    override suspend fun insertRule(rule: AlertRule): Long =
        ruleDao.insertRule(rule.toEntity())

    override suspend fun updateRule(rule: AlertRule) =
        ruleDao.updateRule(rule.toEntity().copy(updatedAt = System.currentTimeMillis()))

    override suspend fun deleteRule(id: Long) =
        ruleDao.deleteRuleById(id)

    override suspend fun setRuleEnabled(id: Long, enabled: Boolean) =
        ruleDao.setRuleEnabled(id, enabled)

    override fun getActiveRuleCount(): Flow<Int> =
        ruleDao.getActiveRuleCount()

    private fun Rule.toDomain(): AlertRule = AlertRule(
        id = id,
        appPackage = appPackage,
        label = label,
        keywords = keywords.split(",").filter { it.isNotBlank() },
        excludeKeywords = excludeKeywords.split(",").filter { it.isNotBlank() },
        regexPattern = regexPattern,
        senderFilter = senderFilter,
        burstCount = burstCount,
        burstWindowMs = burstWindowMs,
        scheduleStartHour = scheduleStartHour,
        scheduleStartMinute = scheduleStartMinute,
        scheduleEndHour = scheduleEndHour,
        scheduleEndMinute = scheduleEndMinute,
        scheduleDays = scheduleDays,
        isEnabled = isEnabled,
        alarmSoundUri = alarmSoundUri,
        vibrationPattern = vibrationPattern,
        useTts = useTts,
        cooldownMs = cooldownMs,
        priority = priority,
        triggerOnScreenOff = triggerOnScreenOff,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun AlertRule.toEntity(): Rule = Rule(
        id = id,
        appPackage = appPackage,
        label = label,
        keywords = keywords.joinToString(","),
        excludeKeywords = excludeKeywords.joinToString(","),
        regexPattern = regexPattern,
        senderFilter = senderFilter,
        burstCount = burstCount,
        burstWindowMs = burstWindowMs,
        scheduleStartHour = scheduleStartHour,
        scheduleStartMinute = scheduleStartMinute,
        scheduleEndHour = scheduleEndHour,
        scheduleEndMinute = scheduleEndMinute,
        scheduleDays = scheduleDays,
        isEnabled = isEnabled,
        alarmSoundUri = alarmSoundUri,
        vibrationPattern = vibrationPattern,
        useTts = useTts,
        cooldownMs = cooldownMs,
        priority = priority,
        triggerOnScreenOff = triggerOnScreenOff,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
