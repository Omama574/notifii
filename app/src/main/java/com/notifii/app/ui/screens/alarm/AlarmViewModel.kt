package com.notifii.app.ui.screens.alarm

import androidx.lifecycle.ViewModel
import com.notifii.app.util.FlipToSilenceDetector
import com.notifii.app.util.TtsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val ttsManager: TtsManager,
    private val flipToSilenceDetector: FlipToSilenceDetector
) : ViewModel() {

    fun stopAlarm() {
        ttsManager.stop()
        flipToSilenceDetector.stopListening()
    }
}
