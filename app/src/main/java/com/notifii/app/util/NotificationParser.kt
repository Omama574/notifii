package com.notifii.app.util

import android.app.Notification
import android.content.Context
import android.content.pm.PackageManager
import android.service.notification.StatusBarNotification
import com.notifii.app.domain.model.CapturedNotification

object NotificationParser {

    fun parse(context: Context, sbn: StatusBarNotification, isScreenOff: Boolean = false): CapturedNotification {
        val packageName = sbn.packageName ?: ""
        val extras = sbn.notification?.extras

        val title = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString()
            ?: extras?.getString(Notification.EXTRA_TITLE)
            ?: ""

        val text = extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            ?: extras?.getString(Notification.EXTRA_TEXT)
            ?: extras?.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            ?: ""

        val subText = extras?.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString() ?: ""
        val infoText = extras?.getCharSequence(Notification.EXTRA_INFO_TEXT)?.toString() ?: ""
        val summaryText = extras?.getCharSequence(Notification.EXTRA_SUMMARY_TEXT)?.toString() ?: ""

        val fullText = buildString {
            append(text)
            if (subText.isNotBlank()) append(" ").append(subText)
            if (infoText.isNotBlank()) append(" ").append(infoText)
            if (summaryText.isNotBlank()) append(" ").append(summaryText)
        }.trim()

        val sender = extras?.getCharSequence(Notification.EXTRA_MESSAGING_PERSON)?.toString()
            ?: extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString()
            ?: ""

        val appLabel = try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }

        return CapturedNotification(
            packageName = packageName,
            appLabel = appLabel,
            title = title,
            text = fullText,
            sender = sender,
            timestamp = sbn.postTime,
            isScreenOff = isScreenOff
        )
    }
}
