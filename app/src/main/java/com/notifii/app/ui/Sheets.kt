package com.notifii.app.ui

import com.notifii.app.R
import androidx.compose.ui.res.stringResource
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.notifii.app.data.Rule
import com.notifii.app.data.Rung
import com.notifii.app.data.VaultEntry
import com.notifii.app.engine.largestAmount
import com.notifii.app.engine.suggestWords
import com.notifii.app.engine.trimAmount
import com.notifii.app.ui.theme.color
import kotlinx.coroutines.delay

sealed interface Sheet {
    data class Why(val entry: VaultEntry) : Sheet
    data class MakeException(val entry: VaultEntry) : Sheet
    data class Pick(val pkg: String, val person: String, val label: String) : Sheet
    data object Paywall : Sheet
    data object SirenCheck : Sheet
}

class Sheets { var current by mutableStateOf<Sheet?>(null) }

/** Every placement goes through here so the paywall and the Siren check appear exactly when relevant. */
fun placeAndFollowUp(vm: NotifiiViewModel, sheets: Sheets, context: Context, pkg: String, person: String, label: String, rung: Rung) {
    sheets.current = when (vm.place(pkg, person, label, rung)) {
        Outcome.NEEDS_PRO -> Sheet.Paywall
        Outcome.DONE -> if (rung == Rung.SIREN && sirenSetupMissing(context)) Sheet.SirenCheck else null
    }
}

fun sirenSetupMissing(context: Context): Boolean =
    sirenNeeds(context).any { !it.ok } || (Build.VERSION.SDK_INT >= 33 &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetHost(vm: NotifiiViewModel, sheets: Sheets) {
    val sheet = sheets.current ?: return
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = { sheets.current = null }, sheetState = state, containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding(),
        ) {
            when (sheet) {
                is Sheet.Why -> WhySheet(vm, sheets, sheet.entry)
                is Sheet.MakeException -> ExceptionSheet(vm, sheets, sheet.entry)
                is Sheet.Pick -> PickSheet(vm, sheets, sheet)
                Sheet.Paywall -> PaywallSheet(vm, sheets)
                Sheet.SirenCheck -> SirenCheckSheet(sheets)
            }
        }
    }
}

// ---------- Why? ----------

@Composable
private fun WhySheet(vm: NotifiiViewModel, sheets: Sheets, e: VaultEntry) {
    val context = LocalContext.current
    val placements by vm.placements.collectAsState()
    val rules by vm.rules.collectAsState()
    val appRung = placements.firstOrNull { it.pkg == e.pkg && it.isApp }?.rung
    val personRung = e.sender?.let { s -> placements.firstOrNull { it.pkg == e.pkg && it.person.equals(s, true) }?.rung }

    Row(verticalAlignment = Alignment.CenterVertically) {
        AppIcon(e.pkg, 44.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(e.appLabel, style = MaterialTheme.typography.titleSmall)
            Text(shortTime(context, e.postedAt), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        RungChip(e.rung)
    }
    Spacer(Modifier.height(16.dp))
    if (e.title.isNotBlank()) Text(e.title, style = MaterialTheme.typography.titleMedium)
    if (e.text.isNotBlank()) Text(e.text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 6, overflow = TextOverflow.Ellipsis)
    Spacer(Modifier.height(16.dp))
    Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Outlined.Lightbulb, null, tint = e.rung.color())
            Spacer(Modifier.width(12.dp))
            Column {
                Text(stringResource(R.string.why_went_to, e.rung.label()), style = MaterialTheme.typography.titleSmall)
                Text(reasonText(context, e, rules), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
    Spacer(Modifier.height(24.dp))
    Text(stringResource(R.string.how_loud), style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(12.dp))
    Text(stringResource(R.string.everything_from, e.appLabel), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(6.dp))
    RungSelector(appRung) { placeAndFollowUp(vm, sheets, context, e.pkg, "", e.appLabel, it) }
    if (!e.sender.isNullOrBlank()) {
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.only_person, e.sender.orEmpty()), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(6.dp))
        RungSelector(personRung) { placeAndFollowUp(vm, sheets, context, e.pkg, e.sender, e.sender, it) }
    }
    Spacer(Modifier.height(24.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(onClick = { sheets.current = Sheet.MakeException(e) }, modifier = Modifier.weight(1f).height(52.dp)) { Text(stringResource(R.string.make_exception)) }
        Button(onClick = { openEntry(context, e); sheets.current = null }, modifier = Modifier.weight(1f).height(52.dp)) { Text(stringResource(R.string.open)) }
    }
}

@Composable
fun RungSelector(selected: Rung?, onSelect: (Rung) -> Unit) {
    val rungs = Rung.entries
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        rungs.forEachIndexed { i, r ->
            SegmentedButton(
                selected = selected == r,
                onClick = { onSelect(r) },
                shape = SegmentedButtonDefaults.itemShape(i, rungs.size),
                icon = { if (selected == r) Icon(Icons.Rounded.Check, null, Modifier.size(16.dp)) else RungDot(r, 8.dp) },
                colors = SegmentedButtonDefaults.colors(activeContainerColor = r.color().copy(alpha = 0.22f), activeContentColor = MaterialTheme.colorScheme.onSurface),
            ) { Text(r.label(), maxLines = 1) }
        }
    }
}

// ---------- Make an exception ----------

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExceptionSheet(vm: NotifiiViewModel, sheets: Sheets, e: VaultEntry) {
    val context = LocalContext.current
    val detectedAmount = remember(e) { largestAmount("${e.title} ${e.text}") }
    var useApp by remember { mutableStateOf(true) }
    var useSender by remember { mutableStateOf(false) }
    var words by remember { mutableStateOf("") }
    var amountOn by remember { mutableStateOf(false) }
    var amount by remember { mutableStateOf(detectedAmount?.let { trimAmount(kotlin.math.floor(it)) } ?: "20") }
    var burst by remember { mutableStateOf(false) }
    var rung by remember { mutableStateOf(Rung.SIREN) }
    val suggestions = remember(e) { suggestWords("${e.title} ${e.text}") }

    val rule = Rule(
        pkg = e.pkg.takeIf { useApp }, appLabel = e.appLabel.takeIf { useApp },
        sender = e.sender?.takeIf { useSender && it.isNotBlank() },
        words = words, minAmount = amount.toDoubleOrNull()?.takeIf { amountOn }, burst = burst, rung = rung,
    )
    val hasCondition = rule.pkg != null || rule.sender != null || rule.wordList.isNotEmpty() || rule.minAmount != null || rule.burst
    var preview by remember { mutableStateOf<Preview?>(null) }
    LaunchedEffect(rule) { delay(200); preview = if (hasCondition) vm.preview(rule) else null }

    Text(stringResource(R.string.make_exception), style = MaterialTheme.typography.headlineSmall)
    Text(stringResource(R.string.exc_intro), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(20.dp))

    Text(stringResource(R.string.exc_when), style = MaterialTheme.typography.titleSmall)
    Spacer(Modifier.height(8.dp))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = useApp, onClick = { useApp = !useApp }, label = { Text(stringResource(R.string.exc_from, e.appLabel)) }, leadingIcon = { AppIcon(e.pkg, 18.dp) })
        if (!e.sender.isNullOrBlank()) FilterChip(selected = useSender, onClick = { useSender = !useSender }, label = { Text(stringResource(R.string.exc_by, e.sender.orEmpty())) })
        FilterChip(selected = burst, onClick = { burst = !burst }, label = { Text(stringResource(R.string.exc_burst)) })
        if (detectedAmount != null || amountOn) FilterChip(selected = amountOn, onClick = { amountOn = !amountOn }, label = { Text(stringResource(R.string.exc_amount)) })
    }
    if (amountOn) {
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = amount, onValueChange = { v -> amount = v.filter { it.isDigit() || it == '.' } },
            label = { Text(stringResource(R.string.exc_min_amount)) }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
        )
    }
    Spacer(Modifier.height(12.dp))
    OutlinedTextField(
        value = words, onValueChange = { words = it },
        label = { Text(stringResource(R.string.exc_words)) }, placeholder = { Text(stringResource(R.string.exc_words_hint)) },
        singleLine = true, modifier = Modifier.fillMaxWidth(),
    )
    if (suggestions.isNotEmpty()) {
        Spacer(Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            suggestions.filter { s -> rule.wordList.none { it.equals(s, true) } }.forEach { s ->
                FilterChip(selected = false, onClick = { words = (rule.wordList + s).joinToString(", ") }, label = { Text(stringResource(R.string.exc_add_word, s)) })
            }
        }
    }
    Spacer(Modifier.height(20.dp))
    Text(stringResource(R.string.exc_put_on), style = MaterialTheme.typography.titleSmall)
    Spacer(Modifier.height(8.dp))
    RungSelector(rung) { rung = it }

    Spacer(Modifier.height(20.dp))
    Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(if (hasCondition) describe(context, rule) else stringResource(R.string.exc_pick_one), style = MaterialTheme.typography.titleSmall)
            val p = preview
            if (hasCondition && p != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    if (p.total == 0) stringResource(R.string.exc_vault_empty)
                    else stringResource(R.string.exc_preview, p.matched, p.total),
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                p.samples.forEach { s ->
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppIcon(s.pkg, 20.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("${s.title} ${s.text}".trim(), style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
    Spacer(Modifier.height(20.dp))
    Button(
        onClick = {
            sheets.current = when (vm.addRule(rule)) {
                Outcome.NEEDS_PRO -> Sheet.Paywall
                Outcome.DONE -> if (rung == Rung.SIREN && sirenSetupMissing(context)) Sheet.SirenCheck else null
            }
        },
        enabled = hasCondition,
        modifier = Modifier.fillMaxWidth().height(52.dp),
    ) { Text(stringResource(R.string.exc_save)) }
}

// ---------- Pick a rung (from the Ladder) ----------

@Composable
private fun PickSheet(vm: NotifiiViewModel, sheets: Sheets, s: Sheet.Pick) {
    val context = LocalContext.current
    val placements by vm.placements.collectAsState()
    val current = placements.firstOrNull { it.pkg == s.pkg && it.person == s.person }
    Row(verticalAlignment = Alignment.CenterVertically) {
        AppIcon(s.pkg, 44.dp)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(s.label, style = MaterialTheme.typography.titleLarge)
            Text(if (s.person.isEmpty()) stringResource(R.string.pick_app) else stringResource(R.string.pick_person), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    Spacer(Modifier.height(16.dp))
    Rung.entries.forEach { r ->
        Row(
            Modifier
                .fillMaxWidth()
                .clickable { placeAndFollowUp(vm, sheets, context, s.pkg, s.person, s.label, r) }
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(selected = (current?.rung ?: if (s.person.isEmpty()) Rung.RING else null) == r, onClick = null)
            Spacer(Modifier.width(12.dp))
            RungDot(r)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(r.label(), style = MaterialTheme.typography.titleMedium)
                Text(r.blurb(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
    if (current != null) {
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = { vm.unplace(current); sheets.current = null }) { Text(stringResource(R.string.pick_remove)) }
    }
}

// ---------- Pro ----------

@Composable
private fun PaywallSheet(vm: NotifiiViewModel, sheets: Sheets) {
    val context = LocalContext.current
    val price by vm.billing.price.collectAsState()
    val pro by vm.prefs.isPro.collectAsState()
    LaunchedEffect(pro) { if (pro) sheets.current = null }

    Surface(shape = RoundedCornerShape(20.dp), color = Rung.SIREN.color().copy(alpha = 0.18f), modifier = Modifier.size(64.dp)) {
        Icon(Icons.Rounded.NotificationsActive, null, tint = Rung.SIREN.color(), modifier = Modifier.padding(16.dp))
    }
    Spacer(Modifier.height(16.dp))
    Text(stringResource(R.string.pw_title), style = MaterialTheme.typography.headlineSmall)
    Spacer(Modifier.height(6.dp))
    Text(stringResource(R.string.pw_body), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(20.dp))
    listOf(
        stringResource(R.string.pw_1),
        stringResource(R.string.pw_2),
        stringResource(R.string.pw_3),
        stringResource(R.string.pw_4),
    ).forEach { line ->
        Row(Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Text(line, style = MaterialTheme.typography.bodyLarge)
        }
    }
    Spacer(Modifier.height(24.dp))
    Button(
        onClick = {
            if (!vm.billing.buy(context as Activity)) Toast.makeText(context, context.getString(R.string.play_unreachable), Toast.LENGTH_SHORT).show()
        },
        modifier = Modifier.fillMaxWidth().height(56.dp),
    ) { Text(price?.let { stringResource(R.string.get_pro_price, it) } ?: stringResource(R.string.get_pro), style = MaterialTheme.typography.titleMedium) }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        TextButton(onClick = { vm.billing.restore() }) { Text(stringResource(R.string.restore_purchase)) }
        TextButton(onClick = { sheets.current = null }) { Text(stringResource(R.string.not_now)) }
    }
}

// ---------- Siren check ----------

@Composable
fun SirenCheckSheet(sheets: Sheets?) {
    val context = LocalContext.current
    var tick by remember { mutableIntStateOf(0) }
    LifecycleResumeEffect(Unit) { tick++; onPauseOrDispose {} }
    val needs = remember(tick) { sirenNeeds(context) }
    var canNotify by remember(tick) {
        mutableStateOf(Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)
    }
    val ask = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { canNotify = it }

    Text(stringResource(R.string.sc_title), style = MaterialTheme.typography.headlineSmall)
    Text(stringResource(R.string.sc_body), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(16.dp))
    NeedRow(stringResource(R.string.sc_notify), stringResource(R.string.sc_notify_body), canNotify) { ask.launch(Manifest.permission.POST_NOTIFICATIONS) }
    needs.forEach { n -> NeedRow(n.title, n.why, n.ok) { n.fix?.let { runCatching { context.startActivity(it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } } } }
    if (sheets != null) {
        Spacer(Modifier.height(16.dp))
        Button(onClick = { sheets.current = null }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text(if (canNotify && needs.all { it.ok }) stringResource(R.string.sc_all_set) else stringResource(R.string.sc_later))
        }
    }
}

@Composable
private fun NeedRow(title: String, why: String, ok: Boolean, onFix: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(why, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(12.dp))
        if (ok) Icon(Icons.Rounded.CheckCircle, contentDescription = stringResource(R.string.done), tint = MaterialTheme.colorScheme.primary)
        else FilledTonalButton(onClick = onFix, colors = ButtonDefaults.filledTonalButtonColors()) { Text(stringResource(R.string.allow)) }
    }
}
