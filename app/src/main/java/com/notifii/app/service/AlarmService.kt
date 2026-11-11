package com.notifii.app.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.notifii.app.receiver.AlarmDismissReceiver
import com.notifii.app.ui.screens.alarm.AlarmActivity
import com.notifii.app.util.Constants
import com.notifii.app.util.FlipToSilenceDetector
import com.notifii.app.util.TtsManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AlarmService : Service() {

    @Inject lateinit var ttsManager: TtsManager
    @Inject lateinit var flipToSilenceDetector: FlipToSilenceDetector

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var originalVolume: Int = -1

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Constants.ACTION_START_ALARM -> {
                val ruleLabel = intent.getStringExtra(Constants.EXTRA_RULE_LABEL) ?: "Alert"
                val notifTitle = intent.getStringExtra(Constants.EXTRA_NOTIFICATION_TITLE) ?: ""
                val notifText = intent.getStringExtra(Constants.EXTRA_NOTIFICATION_TEXT) ?: ""
                val soundUriStr = intent.getStringExtra(Constants.EXTRA_ALARM_SOUND_URI)
                val vibPattern = intent.getStringExtra(Constants.EXTRA_VIBRATION_PATTERN) ?: "0,500,200,500"
                val useTts = intent.getBooleanExtra(Constants.EXTRA_USE_TTS, false)

                startForeground(
                    Constants.ALARM_NOTIFICATION_ID,
                    createAlarmNotification(ruleLabel, notifTitle, notifText)
                )

                playAlarm(soundUriStr)
                startVibration(vibPattern)

                if (useTts && notifText.isNotBlank()) {
                    ttsManager.speak("Alert for $ruleLabel. $notifText")
                }

                flipToSilenceDetector.startListening {
                    stopAlarm()
                }
            }
            Constants.ACTION_DISMISS_ALARM,
            Constants.ACTION_STOP_ALARM -> {
                stopAlarm()
            }
        }
        return START_NOT_STICKY
    }

    private fun playAlarm(soundUriStr: String?) {
        try {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVolume, 0)

            val soundUri = if (!soundUriStr.isNullOrBlank()) {
                Uri.parse(soundUriStr)
            } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }

            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, soundUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startVibration(patternStr: String) {
        try {
            val timings = patternStr.split(",")
                .mapNotNull { it.trim().toLongOrNull() }
                .toLongArray()
            val safeTimings = if (timings.isNotEmpty()) timings else longArrayOf(0, 500, 200, 500)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(safeTimings, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(safeTimings, 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createAlarmNotification(ruleLabel: String, title: String, text: String): Notification {
        val fullScreenIntent = Intent(this, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(Constants.EXTRA_RULE_LABEL, ruleLabel)
            putExtra(Constants.EXTRA_NOTIFICATION_TITLE, title)
            putExtra(Constants.EXTRA_NOTIFICATION_TEXT, text)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(this, AlarmDismissReceiver::class.java).apply {
            action = Constants.ACTION_DISMISS_ALARM
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID_ALARM)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("ALARM: $ruleLabel")
            .setContentText(if (title.isNotBlank()) "$title: $text" else text)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dismiss", dismissPendingIntent)
            .build()
    }

    private fun stopAlarm() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null

            vibrator?.cancel()
            ttsManager.stop()
            flipToSilenceDetector.stopListening()

            if (originalVolume >= 0) {
                val audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                audioManager?.setStreamVolume(AudioManager.STREAM_ALARM, originalVolume, 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    override fun onDestroy() {
        stopAlarm()
        super.onDestroy()
    }
}
