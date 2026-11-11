package com.notifii.app.service

import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.content.ContextCompat
import com.notifii.app.data.local.entity.NotificationLog
import com.notifii.app.domain.repository.NotificationLogRepository
import com.notifii.app.domain.usecase.EvaluateNotificationUseCase
import com.notifii.app.util.BurstDetector
import com.notifii.app.util.Constants
import com.notifii.app.util.NotificationParser
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotifiiListenerService : NotificationListenerService() {

    @Inject lateinit var evaluateNotificationUseCase: EvaluateNotificationUseCase
    @Inject lateinit var logRepository: NotificationLogRepository
    @Inject lateinit var burstDetector: BurstDetector

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn ?: return

        // Ignore our own notifications
        if (sbn.packageName == packageName) return

        val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
        val isScreenOff = powerManager?.isInteractive == false

        val captured = NotificationParser.parse(this, sbn, isScreenOff)

        // Record for burst detection
        burstDetector.recordNotification(captured.packageName, captured.timestamp)

        serviceScope.launch {
            val result = evaluateNotificationUseCase(captured)

            // Log notification to database
            logRepository.insertLog(
                NotificationLog(
                    packageName = captured.packageName,
                    appLabel = captured.appLabel,
                    title = captured.title,
                    text = captured.text,
                    sender = captured.sender,
                    timestamp = captured.timestamp,
                    matchedRuleId = result.matchedRule?.id,
                    matchedRuleLabel = result.matchedRule?.label ?: "",
                    didTriggerAlarm = result.shouldTrigger
                )
            )

            // Trigger alarm if rule matched
            if (result.shouldTrigger && result.matchedRule != null) {
                val rule = result.matchedRule
                val alarmIntent = Intent(this@NotifiiListenerService, AlarmService::class.java).apply {
                    action = Constants.ACTION_START_ALARM
                    putExtra(Constants.EXTRA_RULE_ID, rule.id)
                    putExtra(Constants.EXTRA_RULE_LABEL, rule.label)
                    putExtra(Constants.EXTRA_NOTIFICATION_TITLE, captured.title)
                    putExtra(Constants.EXTRA_NOTIFICATION_TEXT, captured.text)
                    putExtra(Constants.EXTRA_APP_PACKAGE, captured.packageName)
                    putExtra(Constants.EXTRA_ALARM_SOUND_URI, rule.alarmSoundUri)
                    putExtra(Constants.EXTRA_VIBRATION_PATTERN, rule.vibrationPattern)
                    putExtra(Constants.EXTRA_USE_TTS, rule.useTts)
                }
                ContextCompat.startForegroundService(this@NotifiiListenerService, alarmIntent)
            }
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}
