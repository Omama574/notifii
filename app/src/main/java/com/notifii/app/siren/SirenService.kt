package com.notifii.app.siren

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.notifii.app.NotifiiApp
import com.notifii.app.R
import com.notifii.app.notifii
import com.notifii.app.service.Receivers
import java.util.Locale

/** Loops an alarm on the ALARM stream, getting louder, until the user handles it (or 10 minutes pass). */
class SirenService : Service(), SensorEventListener {

    private val handler = Handler(Looper.getMainLooper())
    private var player: MediaPlayer? = null
    private var tts: TextToSpeech? = null
    private var savedVolume = -1
    private var volume = START_VOLUME
    private var wasFaceUp = false

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= 31) getSystemService(VibratorManager::class.java)?.defaultVibrator
        else @Suppress("DEPRECATION") getSystemService(Vibrator::class.java)
    }
    private val sensors by lazy { getSystemService(SensorManager::class.java) }

    private val rampUp = object : Runnable {
        override fun run() {
            volume = (volume + 0.05f).coerceAtMost(1f)
            player?.setVolume(volume, volume)
            if (volume < 1f) handler.postDelayed(this, 700)
        }
    }
    private val timeout = Runnable { finishSiren() }

    override fun onBind(intent: Intent?) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Siren.ACTION_START -> SirenPayload.from(intent.extras)?.let { start(it) } ?: stopSelf()
            Siren.ACTION_MUTE -> silence()
            Siren.ACTION_STOP -> finishSiren()
            else -> stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun start(p: SirenPayload) {
        val alreadyRinging = SirenState.active.value != null && !SirenState.muted.value
        SirenState.active.value = p
        ServiceCompat.startForeground(this, NOTIFICATION_ID, notification(p), ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        Siren.clearFallback(this)
        if (!alreadyRinging) {
            SirenState.muted.value = false
            SirenState.startedAt.value = System.currentTimeMillis()
            ring()
            if (notifii.prefs.flipToSilence.value) listenForFlip()
        }
        speak(p)
        handler.removeCallbacks(timeout)
        handler.postDelayed(timeout, MAX_RING_MS)
    }

    private fun notification(p: SirenPayload) = NotificationCompat.Builder(this, NotifiiApp.CHANNEL_SIREN)
        .setSmallIcon(R.drawable.ic_stat_notifii)
        .setColor(ContextCompat.getColor(this, R.color.notifii_siren))
        .setContentTitle(p.title.ifBlank { p.appLabel })
        .setContentText(p.text)
        .setSubText(getString(R.string.siren_subtext, p.appLabel))
        .setCategory(NotificationCompat.CATEGORY_ALARM)
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .setOngoing(true)
        .setFullScreenIntent(Siren.screenIntent(this, p), true)
        .setContentIntent(Siren.screenIntent(this, p))
        .addAction(0, getString(R.string.stop), PendingIntent.getService(this, 1, Intent(this, SirenService::class.java).setAction(Siren.ACTION_STOP), PendingIntent.FLAG_IMMUTABLE))
        .addAction(0, getString(R.string.snooze), Receivers.snoozeIntent(this, p))
        .build()

    private fun ring() {
        val audio = getSystemService(AudioManager::class.java)
        if (savedVolume < 0) savedVolume = audio.getStreamVolume(AudioManager.STREAM_ALARM)
        runCatching { audio.setStreamVolume(AudioManager.STREAM_ALARM, audio.getStreamMaxVolume(AudioManager.STREAM_ALARM), 0) }

        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        player?.release()
        player = runCatching {
            MediaPlayer().apply {
                setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
                setDataSource(this@SirenService, uri)
                setWakeMode(this@SirenService, PowerManager.PARTIAL_WAKE_LOCK)
                isLooping = true
                prepare()
                volume = START_VOLUME
                setVolume(volume, volume)
                start()
            }
        }.getOrNull()
        handler.removeCallbacks(rampUp)
        handler.postDelayed(rampUp, 700)

        val pattern = longArrayOf(0, 400, 150, 400, 150, 800)
        vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
    }

    private fun speak(p: SirenPayload) {
        if (!notifii.prefs.readAloud.value) return
        val line = listOf(p.appLabel, p.title, p.text).filter { it.isNotBlank() }.joinToString(". ")
        val engine = tts
        if (engine != null) { engine.speak(line, TextToSpeech.QUEUE_FLUSH, null, "siren"); return }
        tts = TextToSpeech(this) { status ->
            val t = tts ?: return@TextToSpeech
            if (status != TextToSpeech.SUCCESS) return@TextToSpeech
            t.language = Locale.getDefault()
            t.setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build())
            t.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                // Dip the alarm so the words are clear, then bring it back.
                override fun onStart(id: String?) { handler.post { player?.setVolume(0.15f, 0.15f) } }
                override fun onDone(id: String?) { handler.post { player?.setVolume(volume, volume) } }
                @Deprecated("Deprecated in Java") override fun onError(id: String?) = onDone(id)
            })
            handler.postDelayed({ t.speak(line, TextToSpeech.QUEUE_FLUSH, null, "siren") }, 1200)
        }
    }

    private fun listenForFlip() {
        wasFaceUp = false
        sensors?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let { sensors?.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
    }

    override fun onSensorChanged(event: SensorEvent) {
        val z = event.values[2]
        if (z > 7f) wasFaceUp = true
        if (wasFaceUp && z < -7f) finishSiren() // turned face down
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    /** Stops the noise but keeps the screen, so the user can still read and reply. */
    private fun silence() {
        handler.removeCallbacks(rampUp)
        player?.runCatching { stop() }
        player?.release()
        player = null
        vibrator?.cancel()
        tts?.stop()
        sensors?.unregisterListener(this)
        restoreVolume()
        SirenState.muted.value = true
    }

    private fun restoreVolume() {
        if (savedVolume < 0) return
        runCatching { getSystemService(AudioManager::class.java).setStreamVolume(AudioManager.STREAM_ALARM, savedVolume, 0) }
        savedVolume = -1
    }

    private fun finishSiren() {
        silence()
        handler.removeCallbacks(timeout)
        SirenState.active.value = null
        SirenState.muted.value = false
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        silence()
        SirenState.active.value = null
        tts?.shutdown()
        tts = null
        super.onDestroy()
    }

    private companion object {
        const val NOTIFICATION_ID = 4400
        const val START_VOLUME = 0.3f
        const val MAX_RING_MS = 10 * 60_000L
    }
}
