package com.notifii.app.ui

import com.notifii.app.R
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.text.format.DateFormat
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.toBitmap
import com.notifii.app.Live
import com.notifii.app.data.Rung
import com.notifii.app.data.VaultEntry
import com.notifii.app.service.ListenerService
import com.notifii.app.ui.theme.color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.ConcurrentHashMap

// ---------- Access & permissions ----------

fun listenerEnabled(context: Context) =
    NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)

fun openListenerSettings(context: Context) {
    val component = ComponentName(context, ListenerService::class.java).flattenToString()
    val direct = if (Build.VERSION.SDK_INT >= 30) Intent(Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS)
        .putExtra(Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME, component) else null
    val fallback = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
    runCatching { context.startActivity((direct ?: fallback).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
        .onFailure { runCatching { context.startActivity(fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } }
}

/** One thing Siren needs from Android, with how to fix it. */
data class SirenNeed(val title: String, val why: String, val ok: Boolean, val fix: Intent?)

fun sirenNeeds(context: Context): List<SirenNeed> {
    val nm = context.getSystemService(NotificationManager::class.java)
    val pkg = Uri.parse("package:${context.packageName}")
    val power = context.getSystemService(PowerManager::class.java)
    return buildList {
        add(SirenNeed(context.getString(R.string.sc_dnd), context.getString(R.string.sc_dnd_body), nm.isNotificationPolicyAccessGranted, Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)))
        if (Build.VERSION.SDK_INT >= 34) {
            add(SirenNeed(context.getString(R.string.sc_lock), context.getString(R.string.sc_lock_body), nm.canUseFullScreenIntent(), Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, pkg)))
        }
        @Suppress("BatteryLife")
        add(SirenNeed(context.getString(R.string.sc_battery), context.getString(R.string.sc_battery_body), power.isIgnoringBatteryOptimizations(context.packageName), Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, pkg)))
    }
}

/** Opens what the original notification would have opened, or the app if that's gone. */
fun openEntry(context: Context, e: VaultEntry) {
    val tap = Live.taps[e.id]
    val sent = tap != null && runCatching { tap.send() }.isSuccess
    if (!sent) context.packageManager.getLaunchIntentForPackage(e.pkg)?.let { runCatching { context.startActivity(it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } }
}

// ---------- App identity ----------

private val icons = ConcurrentHashMap<String, ImageBitmap>()

@Composable
fun AppIcon(pkg: String, size: Dp = 40.dp) {
    val context = LocalContext.current
    val icon by produceState(icons[pkg], pkg) {
        if (value == null) value = withContext(Dispatchers.IO) {
            runCatching { context.packageManager.getApplicationIcon(pkg).toBitmap(128, 128).asImageBitmap() }.getOrNull()?.also { icons[pkg] = it }
        }
    }
    val bitmap = icon
    if (bitmap != null) Image(bitmap, contentDescription = null, modifier = Modifier.size(size).clip(RoundedCornerShape(size * 0.28f)))
    else Box(Modifier.size(size).clip(RoundedCornerShape(size * 0.28f)).background(MaterialTheme.colorScheme.surfaceContainerHighest))
}

// ---------- Rungs ----------

@Composable
fun RungDot(rung: Rung, size: Dp = 10.dp) = Box(Modifier.size(size).clip(CircleShape).background(rung.color()))

@Composable
fun RungChip(rung: Rung) {
    Surface(shape = RoundedCornerShape(8.dp), color = rung.color().copy(alpha = 0.16f)) {
        Row(Modifier.padding(horizontal = 8.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
            RungDot(rung, 6.dp)
            Spacer(Modifier.width(5.dp))
            Text(rung.label(), style = MaterialTheme.typography.labelSmall, color = rung.color())
        }
    }
}

// ---------- Time ----------

fun shortTime(context: Context, ts: Long, now: Long = System.currentTimeMillis()): String {
    val diff = now - ts
    if (diff < 60_000) return context.getString(R.string.time_now)
    if (diff < 60 * 60_000) return context.getString(R.string.time_minutes, (diff / 60_000).toInt())
    val a = Calendar.getInstance().apply { timeInMillis = ts }
    val b = Calendar.getInstance().apply { timeInMillis = now }
    if (a.get(Calendar.YEAR) == b.get(Calendar.YEAR) && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)) return DateFormat.getTimeFormat(context).format(ts)
    return DateFormat.format("d MMM", ts).toString()
}

fun clockLabel(context: Context, minuteOfDay: Int): String {
    val cal = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, minuteOfDay / 60); set(Calendar.MINUTE, minuteOfDay % 60) }
    return DateFormat.getTimeFormat(context).format(cal.time)
}

fun startOfToday(): Long = Calendar.getInstance().apply {
    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
}.timeInMillis

fun dayLabel(context: Context, ts: Long): String {
    val start = startOfToday()
    return when {
        ts >= start -> context.getString(R.string.time_today)
        ts >= start - 86_400_000L -> context.getString(R.string.time_yesterday)
        else -> DateFormat.format("EEEE, d MMMM", ts).toString()
    }
}

// ---------- Empty state ----------

@Composable
fun EmptyState(icon: ImageVector, title: String, body: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 40.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerHigh, modifier = Modifier.size(80.dp)) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp)) }
        }
        Spacer(Modifier.height(20.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}
