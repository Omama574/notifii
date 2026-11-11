package com.notifii.app.siren

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.notifii.app.Live
import com.notifii.app.ui.AppIcon
import com.notifii.app.ui.theme.NotifiiTheme
import com.notifii.app.ui.theme.SirenRed
import kotlinx.coroutines.delay

/** The full-screen alarm. Oversized controls, nothing to mis-tap, readable at arm's length in the dark. */
class SirenActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        if (android.os.Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        // Opened from the backup notification: the screen is visible now, so the full Siren may start.
        if (SirenState.active.value == null) SirenPayload.from(intent.extras)?.let { Siren.start(this, it) }
        setContent { NotifiiTheme(dark = true) { SirenScreen(onClose = { finish() }) } }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            // First press silences, second press dismisses.
            if (SirenState.muted.value) { Siren.stop(this); finish() } else Siren.mute(this)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}

private val QUICK_REPLIES = listOf("On it 👍", "Can’t talk, I’ll call you back", "Give me 5 minutes", "On my way")

@Composable
private fun SirenScreen(onClose: () -> Unit) {
    val context = LocalContext.current
    val payload by SirenState.active.collectAsState()
    val muted by SirenState.muted.collectAsState()
    val startedAt by SirenState.startedAt.collectAsState()
    val p = payload
    LaunchedEffect(p) { if (p == null) { delay(300); onClose() } }
    if (p == null) { Box(Modifier.fillMaxSize().background(Color(0xFF09090B))); return }

    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { now = System.currentTimeMillis(); delay(1000) } }
    var showWhy by remember { mutableStateOf(false) }
    var reply by remember { mutableStateOf("") }
    val replyAction = Live.replies[p.entryId]
    val seconds = ((now - startedAt) / 1000).coerceAtLeast(0)

    Box(Modifier.fillMaxSize().background(Color(0xFF09090B))) {
        if (!muted) PulseRings()
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppIcon(p.pkg, 44.dp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(p.appLabel, style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Text(
                        if (muted) "Silenced" else "Siren · ringing %d:%02d".format(seconds / 60, seconds % 60),
                        style = MaterialTheme.typography.labelLarge, color = if (muted) Color(0xFFA9ABB0) else SirenRed,
                    )
                }
                TextButton(onClick = { showWhy = !showWhy }) { Text("Why?", color = Color(0xFFA9ABB0)) }
            }
            if (showWhy) {
                Spacer(Modifier.height(12.dp))
                Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFF1A1A1E)) {
                    Text(p.reason, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFD5D6DA), modifier = Modifier.padding(14.dp))
                }
            }
            Spacer(Modifier.weight(1f))
            if (p.title.isNotBlank()) Text(p.title, style = MaterialTheme.typography.displaySmall, color = Color.White, fontWeight = FontWeight.Bold)
            if (p.text.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(p.text, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Normal), color = Color(0xFFE4E4E7), maxLines = 8)
            }
            Spacer(Modifier.weight(1f))
            if (replyAction != null) {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QUICK_REPLIES.forEach { q ->
                        SuggestionChip(
                            onClick = { if (sendReply(context, replyAction, q)) { Siren.stop(context); onClose() } },
                            label = { Text(q) },
                            colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0xFF1A1A1E), labelColor = Color.White),
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = reply, onValueChange = { reply = it },
                    placeholder = { Text("Reply") }, singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { if (reply.isNotBlank() && sendReply(context, replyAction, reply)) { Siren.stop(context); onClose() } }) {
                            Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "Send reply")
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(16.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { com.notifii.app.service.Receivers.snoozeIntent(context, p).send(); onClose() },
                    modifier = Modifier.weight(1f).height(64.dp),
                    shape = RoundedCornerShape(20.dp),
                ) { Text("Snooze 5 min", style = MaterialTheme.typography.titleMedium, color = Color.White) }
                Button(
                    onClick = { Siren.stop(context); onClose() },
                    modifier = Modifier.weight(1.4f).height(64.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SirenRed, contentColor = Color.White),
                ) { Text("Stop", style = MaterialTheme.typography.titleLarge) }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "Flip the phone face down or press a volume key to silence",
                style = MaterialTheme.typography.bodySmall, color = Color(0xFF8D8F95),
                modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

/** Three red rings expanding from the top: urgent, but calm enough to read under. */
@Composable
private fun PulseRings() {
    val t = rememberInfiniteTransition(label = "pulse")
    val phase by t.animateFloat(0f, 1f, infiniteRepeatable(tween(2400, easing = LinearEasing), RepeatMode.Restart), label = "phase")
    Canvas(Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height * 0.18f)
        val maxR = size.maxDimension * 0.7f
        repeat(3) { i ->
            val f = (phase + i / 3f) % 1f
            drawCircle(SirenRed.copy(alpha = (1f - f) * 0.35f), radius = maxR * f, center = center, style = Stroke(width = 3.dp.toPx()))
        }
    }
}

private fun sendReply(context: Context, action: Notification.Action, text: String): Boolean {
    val inputs = action.remoteInputs ?: return false
    val results = Bundle().apply { inputs.forEach { putCharSequence(it.resultKey, text) } }
    val intent = Intent()
    android.app.RemoteInput.addResultsToIntent(inputs, intent, results)
    val ok = runCatching { action.actionIntent.send(context, 0, intent) }.isSuccess
    Toast.makeText(context, if (ok) "Sent" else "Couldn’t send. Open the app to reply.", Toast.LENGTH_SHORT).show()
    return ok
}
