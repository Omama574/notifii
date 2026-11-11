package com.notifii.app.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FlipToSilenceDetector @Inject constructor(
    @ApplicationContext private val context: Context
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var onFlippedCallback: (() -> Unit)? = null
    private var isListening = false
    private var wasFaceUp = false

    fun startListening(onFlipped: () -> Unit) {
        if (isListening || accelerometer == null) return
        onFlippedCallback = onFlipped
        wasFaceUp = false
        isListening = true
        sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun stopListening() {
        if (!isListening) return
        sensorManager?.unregisterListener(this)
        isListening = false
        onFlippedCallback = null
        wasFaceUp = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val z = event.values[2]
            // z > 7 m/s^2 means face up
            if (z > 7.0f) {
                wasFaceUp = true
            }
            // z < -7 m/s^2 means screen turned facing down
            if (wasFaceUp && z < -7.0f) {
                onFlippedCallback?.invoke()
                stopListening()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}
