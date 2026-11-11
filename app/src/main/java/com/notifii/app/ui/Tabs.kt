package com.notifii.app.ui

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.notifii.app.data.Placement
import com.notifii.app.data.Rung
import com.notifii.app.data.VaultEntry
import com.notifii.app.engine.describe
import com.notifii.app.ui.theme.color

// =====================================================================
// Now: what needs you today
// =====================================================================

@Composable
fun NowTab(vm: NotifiiViewModel, sheets: Sheets, padding: PaddingValues) {
    val context = LocalContext.current
    val today by vm.today.collectAsState()
    val pending by vm.pending.collectAsState()
    val triage by vm.triage.collectAsState()
    var listening by remember { mutableStateOf(true) }
    LifecycleResumeEffect(Unit) { listening = listenerEnabled(context); onPauseOrDispose {} }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding() + 16.dp)) {
        if (!listening) item {
            Card(Modifier.padding(16.dp), color = MaterialTheme.colorScheme.errorContainer) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.PauseCircle, null, tint = MaterialTheme.colorScheme.onErrorContainer)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Notifii is paused", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onErrorContainer)
                        Text("Notification access is off, so nothing is being sorted.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                    FilledTonalButton(onClick = { openListenerSettings(context) }) { Text("Turn on") }
                }
            }
        }
        if (pending.isNotEmpty()) item {
            Card(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RungDot(Rung.LATER, 12.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("${pending.size} waiting for later", style = MaterialTheme.typography.titleMedium)
                        Text(pending.map { it.appLabel }.distinct().take(3).joinToString(", "), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    TextButton(onClick = { vm.deliverNow() }) { Text("Deliver now") }
                }
            }
        }
        if (triage.isNotEmpty()) {
            item { SectionLabel("Sort your loudest apps") }
            items(triage, key = { "triage-${it.pkg}" }) { app ->
                Card(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppIcon(app.pkg, 40.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(app.appLabel, style = MaterialTheme.typography.titleMedium)
                            Text("${app.count} notifications this week. How loud should it be?", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { vm.prefs.dismissTriage(app.pkg) }) { Icon(Icons.Rounded.Close, contentDescription = "Not now") }
                    }
                    Spacer(Modifier.height(12.dp))
                    RungSelector(null) { placeAndFollowUp(vm, sheets, context, app.pkg, "", app.appLabel, it) }
                }
            }
        }
        item { SectionLabel("Today") }
        val list = today
        when {
            list == null -> Unit
            list.isEmpty() -> item {
                EmptyState(Icons.Outlined.Inbox, "All quiet so far", "Notifications land here as they arrive, each with the rung it went to. Tap one to change how loud it is.")
            }
            else -> items(list, key = { it.id }) { e -> EntryRow(e) { sheets.current = Sheet.Why(e) } }
        }
    }
}

// =====================================================================
// Ladder: the whole configuration on one screen
// =====================================================================

@Composable
fun LadderTab(vm: NotifiiViewModel, sheets: Sheets, padding: PaddingValues) {
    val context = LocalContext.current
    val placements by vm.placements.collectAsState()
    val rules by vm.rules.collectAsState()
    val nightOn by vm.prefs.nightOn.collectAsState()
    val nightStart by vm.prefs.nightStart.collectAsState()
    val nightEnd by vm.prefs.nightEnd.collectAsState()

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = padding.calculateTopPadding() + 8.dp, bottom = padding.calculateBottomPadding() + 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Rung.entries.forEach { rung ->
            item(key = rung.name) {
                Card {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RungDot(rung, 14.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(rung.label, style = MaterialTheme.typography.titleLarge)
                            Text(rung.blurb, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    val here = placements.filter { it.rung == rung }
                    if (here.isEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (rung == Rung.RING) "Every app you haven’t sorted rings normally." else "Nothing here yet. Tap a notification in Now to move it.",
                            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    here.forEach { p -> PlacementRow(p) { sheets.current = Sheet.Pick(p.pkg, p.person, p.label) } }
                }
            }
        }
        item(key = "rules") {
            Card {
                Text("Exceptions", style = MaterialTheme.typography.titleLarge)
                Text("Specific notifications that skip their app’s rung. The loudest match wins.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (rules.isEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text("Open any notification and choose “Make an exception”.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                rules.forEach { r ->
                    Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        RungDot(r.rung)
                        Spacer(Modifier.width(12.dp))
                        Text(describe(r), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        IconButton(onClick = { vm.deleteRule(r) }) { Icon(Icons.Rounded.Close, contentDescription = "Delete exception") }
                    }
                }
            }
        }
        item(key = "night") {
            Card {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Bedtime, null, tint = Rung.LATER.color())
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Night", style = MaterialTheme.typography.titleLarge)
                        Text("Ring waits for morning. Siren still rings.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = nightOn, onCheckedChange = { vm.prefs.setNightOn(it) })
                }
                if (nightOn) {
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TimeButton("From", nightStart) { vm.prefs.setNightStart(it) }
                        TimeButton("Until", nightEnd) { vm.prefs.setNightEnd(it) }
                    }
                }
            }
        }
        item(key = "digest") {
            Row(Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Schedule, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Later is delivered at ${clockLabel(context, 12 * 60)} and ${clockLabel(context, 18 * 60)}" + if (nightOn) ", and when Night ends." else ".",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PlacementRow(p: Placement, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(p.pkg, 36.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(p.label, style = MaterialTheme.typography.bodyLarge)
            if (!p.isApp) Text("Person", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Rounded.ChevronRight, contentDescription = "Change rung", tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun TimeButton(label: String, minute: Int, onPick: (Int) -> Unit) {
    val context = LocalContext.current
    FilledTonalButton(onClick = {
        TimePickerDialog(context, { _, h, m -> onPick(h * 60 + m) }, minute / 60, minute % 60, android.text.format.DateFormat.is24HourFormat(context)).show()
    }) { Text("$label ${clockLabel(context, minute)}") }
}

// =====================================================================
// Vault: everything, searchable, nothing lost
// =====================================================================

@Composable
fun VaultTab(vm: NotifiiViewModel, sheets: Sheets, padding: PaddingValues) {
    val query by vm.vaultQuery.collectAsState()
    val filter by vm.vaultFilter.collectAsState()
    val entries by vm.vault.collectAsState()

    Column(Modifier.fillMaxSize().padding(top = padding.calculateTopPadding())) {
        OutlinedTextField(
            value = query, onValueChange = { vm.vaultQuery.value = it },
            placeholder = { Text("Search every notification") },
            leadingIcon = { Icon(Icons.Rounded.Search, null) },
            trailingIcon = { if (query.isNotEmpty()) IconButton(onClick = { vm.vaultQuery.value = "" }) { Icon(Icons.Rounded.Close, contentDescription = "Clear search") } },
            singleLine = true, shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        )
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = filter == null, onClick = { vm.vaultFilter.value = null }, label = { Text("All") })
            Rung.entries.forEach { r ->
                FilterChip(selected = filter == r, onClick = { vm.vaultFilter.value = if (filter == r) null else r }, label = { Text(r.label) }, leadingIcon = { RungDot(r, 8.dp) })
            }
        }
        val list = entries
        when {
            list == null -> Unit
            list.isEmpty() && query.isBlank() && filter == null -> EmptyState(Icons.Outlined.Inventory2, "Your Vault is empty", "Every notification is kept here, even the ones you hide, so nothing is ever really lost.")
            list.isEmpty() -> EmptyState(Icons.Outlined.SearchOff, "No matches", "Try another word or rung.")
            else -> LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = padding.calculateBottomPadding() + 16.dp)) {
                var lastDay = ""
                list.forEach { e ->
                    val day = dayLabel(e.postedAt)
                    if (day != lastDay) { lastDay = day; item(key = "day-$day-${e.id}") { SectionLabel(day) } }
                    item(key = e.id) { EntryRow(e) { sheets.current = Sheet.Why(e) } }
                }
            }
        }
    }
}

// =====================================================================
// Shared pieces
// =====================================================================

@Composable
fun EntryRow(e: VaultEntry, onClick: () -> Unit) {
    val context = LocalContext.current
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        AppIcon(e.pkg, 40.dp)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(e.title.ifBlank { e.appLabel }, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                Text(shortTime(context, e.postedAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (e.text.isNotBlank()) Text(e.text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                RungChip(e.rung)
                Spacer(Modifier.width(8.dp))
                Text(e.appLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
        }
    }
}

@Composable
fun SectionLabel(text: String) =
    Text(text, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 4.dp))

@Composable
fun Card(modifier: Modifier = Modifier, color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surfaceContainerLow, content: @Composable () -> Unit) {
    Surface(shape = RoundedCornerShape(24.dp), color = color, modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) { content() }
    }
}
