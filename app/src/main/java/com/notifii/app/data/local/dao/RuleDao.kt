package com.notifii.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.notifii.app.data.local.entity.Rule
import kotlinx.coroutines.flow.Flow

@Dao
interface RuleDao {

    @Query("SELECT * FROM rules ORDER BY priority DESC, createdAt DESC")
    fun getAllRules(): Flow<List<Rule>>

    @Query("SELECT * FROM rules WHERE isEnabled = 1 ORDER BY priority DESC")
    fun getEnabledRules(): Flow<List<Rule>>

    @Query("SELECT * FROM rules WHERE isEnabled = 1 ORDER BY priority DESC")
    suspend fun getEnabledRulesSnapshot(): List<Rule>

    @Query("SELECT * FROM rules WHERE id = :ruleId")
    suspend fun getRuleById(ruleId: Long): Rule?

    @Query("SELECT * FROM rules WHERE id = :ruleId")
    fun observeRuleById(ruleId: Long): Flow<Rule?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: Rule): Long

    @Update
    suspend fun updateRule(rule: Rule)

    @Delete
    suspend fun deleteRule(rule: Rule)

    @Query("DELETE FROM rules WHERE id = :ruleId")
    suspend fun deleteRuleById(ruleId: Long)

    @Query("UPDATE rules SET isEnabled = :enabled, updatedAt = :timestamp WHERE id = :ruleId")
    suspend fun setRuleEnabled(ruleId: Long, enabled: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM rules WHERE isEnabled = 1")
    fun getActiveRuleCount(): Flow<Int>
}
