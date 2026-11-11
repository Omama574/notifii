package com.notifii.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.notifii.app.ui.NotifiiRoot
import com.notifii.app.ui.theme.NotifiiTheme
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {
    private val tab = MutableStateFlow("now")

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        intent?.getStringExtra(NotifiiApp.EXTRA_TAB)?.let { tab.value = it }
        notifii.billing.connect()
        setContent { NotifiiTheme { NotifiiRoot(tab) } }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.getStringExtra(NotifiiApp.EXTRA_TAB)?.let { tab.value = it }
    }
}
