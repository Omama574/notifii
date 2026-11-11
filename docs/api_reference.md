# Notifii — Internal API & Interface Reference

> **Language:** Kotlin 2.0+ (Coroutines & Flows)  
> **Dependency Injection:** Hilt / Dagger  
> **Target SDK:** API 35  
> **Scope:** Clean Architecture Domain, Data, Services, Broadcasts, and Utilities

---

## 1. Domain Layer: Use Cases & Models

### 1.1 `EvaluateNotificationUseCase`
Core engine component responsible for evaluating captured notifications against all active user rules.

```kotlin
package com.notifii.app.domain.usecase

import com.notifii.app.domain.model.CapturedNotification
import com.notifii.app.domain.model.TriggerResult

interface EvaluateNotificationUseCase {
    /**
     * Evaluates an incoming notification against all enabled rules.
     * Checks package whitelist, inclusion/exclusion keywords, regex matching,
     * sender filtering, time schedules, cooldown, and burst thresholds.
     *
     * @param notification The standardized captured notification payload.
     * @return TriggerResult containing evaluation outcome (Matched, Ignored, InCooldown).
     */
    suspend operator fun invoke(notification: CapturedNotification): TriggerResult
}
```

#### Related Domain Models

```kotlin
package com.notifii.app.domain.model

data class CapturedNotification(
    val id: String,
    val packageName: String,
    val appName: String,
    val title: String?,
    val text: String?,
    val subText: String?,
    val sender: String?,
    val timestamp: Long = System.currentTimeMillis(),
    val isGroupConversation: Boolean = false,
    val hasDirectReply: Boolean = false,
    val replyActionIndex: Int = -1
)

sealed class TriggerResult {
    data class Matched(
        val rule: AlertRule,
        val notification: CapturedNotification,
        val extractedValue: Float? = null
    ) : TriggerResult()

    data class InCooldown(
        val rule: AlertRule,
        val remainingCooldownMs: Long
    ) : TriggerResult()

    object Ignored : TriggerResult()
}

data class AlertRule(
    val id: Int = 0,
    val appPackage: String,
    val label: String,
    val keywords: List<String> = emptyList(),
    val excludeKeywords: List<String> = emptyList(),
    val regexPattern: String? = null,
    val senderFilter: String? = null,
    val burstCount: Int = 1,
    val burstWindowMs: Long = 0L,
    val scheduleStartHour: Int = 0,
    val scheduleStartMinute: Int = 0,
    val scheduleEndHour: Int = 23,
    val scheduleEndMinute: Int = 59,
    val scheduleDaysBitmask: Int = 127, // All 7 days enabled
    val isEnabled: Boolean = true,
    val alarmSoundUri: String? = null,
    val vibrationPattern: String? = null,
    val useTts: Boolean = false,
    val cooldownMs: Long = 0L,
    val priority: Int = 0,
    val triggerOnScreenOff: Boolean = false,
    val bypassDnd: Boolean = true,
    val minExtractedAmount: Float = 0f,
    val amountRegex: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

### 1.2 `ManageRulesUseCase`
Handles rule creation, modification, validation, and tier limit enforcement.

```kotlin
package com.notifii.app.domain.usecase

import com.notifii.app.domain.model.AlertRule
import kotlinx.coroutines.flow.Flow

interface ManageRulesUseCase {
    fun getAllRules(): Flow<List<AlertRule>>
    fun getActiveRules(): Flow<List<AlertRule>>
    suspend fun getRuleById(id: Int): AlertRule?
    suspend fun saveRule(rule: AlertRule): Result<Int>
    suspend fun toggleRuleEnabled(id: Int, isEnabled: Boolean): Result<Unit>
    suspend fun deleteRule(id: Int): Result<Unit>
    suspend fun duplicateRule(id: Int): Result<Int>
    suspend fun canEnableMoreRules(): Boolean
}
```

### 1.3 `GetNotificationHistoryUseCase`
Retrieves and filters captured notification history logs.

```kotlin
package com.notifii.app.domain.usecase

import com.notifii.app.domain.model.NotificationLogEntry
import kotlinx.coroutines.flow.Flow

interface GetNotificationHistoryUseCase {
    fun getRecentHistory(limit: Int = 100): Flow<List<NotificationLogEntry>>
    fun searchHistory(query: String, packageFilter: String?): Flow<List<NotificationLogEntry>>
    suspend fun clearHistory(): Result<Unit>
    suspend fun deleteLogEntry(id: Long): Result<Unit>
}
```

---

## 2. Data Layer: Repository Interfaces

### 2.1 `RuleRepository`

```kotlin
package com.notifii.app.domain.repository

import com.notifii.app.domain.model.AlertRule
import kotlinx.coroutines.flow.Flow

interface RuleRepository {
    fun observeAllRules(): Flow<List<AlertRule>>
    fun observeActiveRules(): Flow<List<AlertRule>>
    suspend fun getRuleById(id: Int): AlertRule?
    suspend fun insertOrUpdateRule(rule: AlertRule): Long
    suspend fun updateRuleStatus(id: Int, isEnabled: Boolean)
    suspend fun deleteRule(id: Int)
    suspend fun getActiveRuleCount(): Int
}
```

### 2.2 `NotificationLogRepository`

```kotlin
package com.notifii.app.domain.repository

import com.notifii.app.domain.model.CapturedNotification
import com.notifii.app.domain.model.NotificationLogEntry
import kotlinx.coroutines.flow.Flow

interface NotificationLogRepository {
    fun observeLogs(limit: Int): Flow<List<NotificationLogEntry>>
    fun searchLogs(query: String, packageName: String?): Flow<List<NotificationLogEntry>>
    suspend fun insertLog(notification: CapturedNotification, ruleId: Int?, triggered: Boolean, action: String?): Long
    suspend fun updateLogAction(logId: Long, actionTaken: String)
    suspend fun deleteOldLogs(retentionCutoffMs: Long)
    suspend fun clearAll()
}
```

### 2.3 `SettingsRepository`

```kotlin
package com.notifii.app.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun isProUser(): Flow<Boolean>
    suspend fun setProUser(isPro: Boolean)
    fun getDefaultAlarmTone(): Flow<String?>
    suspend fun setDefaultAlarmTone(uri: String?)
    fun isFlipToSilenceEnabled(): Flow<Boolean>
    suspend fun setFlipToSilenceEnabled(enabled: Boolean)
    fun isVolumeKeyDismissEnabled(): Flow<Boolean>
    suspend fun setVolumeKeyDismissEnabled(enabled: Boolean)
    fun getLogRetentionDays(): Flow<Int>
    suspend fun setLogRetentionDays(days: Int)
}
```

---

## 3. Data Layer: Room DAOs

### 3.1 `RuleDao`

```kotlin
package com.notifii.app.data.local.dao

import androidx.room.*
import com.notifii.app.data.local.entity.RuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RuleDao {
    @Query("SELECT * FROM rules ORDER BY priority DESC, updatedAt DESC")
    fun observeAllRules(): Flow<List<RuleEntity>>

    @Query("SELECT * FROM rules WHERE isEnabled = 1 ORDER BY priority DESC")
    fun observeActiveRules(): Flow<List<RuleEntity>>

    @Query("SELECT * FROM rules WHERE id = :id LIMIT 1")
    suspend fun getRuleById(id: Int): RuleEntity?

    @Query("SELECT COUNT(*) FROM rules WHERE isEnabled = 1")
    suspend fun getActiveRuleCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: RuleEntity): Long

    @Update
    suspend fun updateRule(rule: RuleEntity)

    @Query("UPDATE rules SET isEnabled = :isEnabled, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateStatus(id: Int, isEnabled: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM rules WHERE id = :id")
    suspend fun deleteRuleById(id: Int)
}
```

### 3.2 `NotificationLogDao`

```kotlin
package com.notifii.app.data.local.dao

import androidx.room.*
import com.notifii.app.data.local.entity.NotificationLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationLogDao {
    @Query("SELECT * FROM notification_logs ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecentLogs(limit: Int): Flow<List<NotificationLogEntity>>

    @Query("""
        SELECT * FROM notification_logs 
        WHERE (:packageFilter IS NULL OR appPackage = :packageFilter)
        AND (:query = '' OR title LIKE '%' || :query || '%' OR text LIKE '%' || :query || '%')
        ORDER BY timestamp DESC
    """)
    fun searchLogs(query: String, packageFilter: String?): Flow<List<NotificationLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: NotificationLogEntity): Long

    @Query("UPDATE notification_logs SET triggerActionTaken = :action WHERE id = :id")
    suspend fun updateActionTaken(id: Long, action: String)

    @Query("DELETE FROM notification_logs WHERE timestamp < :cutoffTimestamp")
    suspend fun deleteOlderThan(cutoffTimestamp: Long)

    @Query("DELETE FROM notification_logs")
    suspend fun clearAllLogs()
}
```

---

## 4. Service Contracts & Intents

### 4.1 `AlarmService` Interface Contracts

Foreground service running continuous audio playback and sensor hooks.

#### Intent Actions
| Action Constant | Value String | Description |
|---|---|---|
| `ACTION_TRIGGER_ALARM` | `"com.notifii.app.action.TRIGGER_ALARM"` | Starts alarm playback with notification context |
| `ACTION_DISMISS_ALARM` | `"com.notifii.app.action.DISMISS_ALARM"` | Stops playback, releases wakelock, tears down service |
| `ACTION_SNOOZE_ALARM` | `"com.notifii.app.action.SNOOZE_ALARM"` | Mutes audio, schedules snooze re-trigger in N minutes |
| `ACTION_DIRECT_REPLY` | `"com.notifii.app.action.DIRECT_REPLY"` | Receives reply text via RemoteInput and delivers to source app |

#### Intent Extras
| Key Constant | Type | Description |
|---|---|---|
| `EXTRA_RULE_ID` | `Int` | Database ID of the matched `AlertRule` |
| `EXTRA_NOTIFICATION_TITLE` | `String` | Captured title for display and TTS |
| `EXTRA_NOTIFICATION_TEXT` | `String` | Captured body for display and TTS |
| `EXTRA_PACKAGE_NAME` | `String` | Originating package identifier |
| `EXTRA_PENDING_REPLY_INTENT` | `PendingIntent` | Wrapped RemoteInput target for inline reply |
| `EXTRA_REPLY_TEXT` | `String` | Outgoing response string to deliver |

---

## 5. Broadcast Receivers & System Listeners

### 5.1 `BootCompletedReceiver`
- **Class:** `com.notifii.app.service.BootCompletedReceiver`
- **Intent Filter:** `android.intent.action.BOOT_COMPLETED`, `android.intent.action.MY_PACKAGE_REPLACED`
- **Contract:** Wakes upon system boot to verify `NotifiiListenerService` status. If unbound or stopped, triggers silent background re-registration.

### 5.2 `AlarmActionReceiver`
- **Class:** `com.notifii.app.service.AlarmActionReceiver`
- **Contract:** Handles notification button interactions (`Dismiss`, `Snooze`) directly from the Android ongoing notification shade without launching the full-screen activity.

---

## 6. Utility Class APIs

### 6.1 `RemoteInputSender`
Encapsulates replying to third-party notifications without waking their full UI.

```kotlin
package com.notifii.app.util

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.RemoteInput

object RemoteInputSender {
    /**
     * Dispatches an inline text reply to a captured RemoteInput PendingIntent.
     *
     * @param context Android Application Context
     * @param replyPendingIntent Source PendingIntent extracted from Notification Action
     * @param remoteInput The RemoteInput specification extracted from Action
     * @param replyText The text message to send
     * @return Boolean true if dispatch succeeded
     */
    fun sendDirectReply(
        context: Context,
        replyPendingIntent: PendingIntent,
        remoteInput: android.app.RemoteInput,
        replyText: String
    ): Boolean
}
```

### 6.2 `AmountExtractor`
Parses financial or order totals from unstructured notification text.

```kotlin
package com.notifii.app.util

object AmountExtractor {
    /**
     * Extracts numerical currency amount using regular expression capture groups.
     * Example: Matches "$24.50" or "£19.99" and extracts Float value 24.50.
     *
     * @param text Unstructured notification content
     * @param customRegex Optional custom regex pattern, or default currency regex
     * @return Extracted Float value, or null if no match found
     */
    fun extractAmount(text: String, customRegex: String? = null): Float?
}
```

### 6.3 `BurstTracker`
In-memory sliding window queue managing burst detection across packages.

```kotlin
package com.notifii.app.util

class BurstTracker {
    /**
     * Records a notification arrival event and evaluates if burst threshold is met.
     *
     * @param packageKey Target package identifier
     * @param thresholdCount Number of notifications needed to trigger
     * @param windowMs Time duration window in milliseconds
     * @return Boolean true if burst condition is fulfilled
     */
    fun recordAndCheckBurst(packageKey: String, thresholdCount: Int, windowMs: Long): Boolean
}
```
