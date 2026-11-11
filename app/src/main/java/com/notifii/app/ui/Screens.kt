package com.notifii.app.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TipsAndUpdates
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.TipsAndUpdates
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.notifii.app.BuildConfig
import com.notifii.app.data.Rung
import com.notifii.app.siren.Siren
import com.notifii.app.siren.SirenPayload
import com.notifii.app.ui.theme.color
import kotlinx.coroutines.delay

// =====================================================================
// Onboarding: one screen, one ask
// =====================================================================

@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    val context = LocalContext.current
    LifecycleResumeEffect(Unit) { if (listenerEnabled(context)) onDone(); onPauseOrDispose {} }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(40.dp))
        LadderDemo()
        Spacer(Modifier.height(36.dp))
        Text("Every notification at the right volume", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(12.dp))
        Text(
            "Put each app or person on a rung: Siren for what you can’t miss, Later for what can wait, Hide for the noise.",
            style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))
        Promise(Icons.Outlined.Inventory2, "Nothing is ever lost: every notification is kept in your Vault.")
        Promise(Icons.Outlined.WifiOff, "No internet permission and no account. Nothing leaves this phone.")
        Spacer(Modifier.height(36.dp))
        Button(onClick = { openListenerSettings(context) }, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("Turn on Notifii", style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "Android will ask to let Notifii read notifications. That’s how it sorts them. You can change everything later.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun Promise(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(16.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}

/** The mental model in one picture: four rungs, each with an example, sliding in one after another. */
@Composable
private fun LadderDemo() {
    val examples = listOf(Rung.SIREN to "Mom, on-call alerts", Rung.RING to "Friends, work chat", Rung.LATER to "Shopping, news", Rung.HIDE to "Game rewards, promos")
    var shown by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) { while (shown < examples.size) { delay(220); shown++ } }
    Column(
        Modifier.semantics { contentDescription = "The ladder: Siren, Ring, Later and Hide" },
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        examples.forEachIndexed { i, (rung, example) ->
            val visible = i < shown
            val a by animateFloatAsState(if (visible) 1f else 0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow), label = "rung$i")
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = rung.color().copy(alpha = 0.14f),
                modifier = Modifier
                    .fillMaxWidth(1f - i * 0.12f)
                    .graphicsLayer { alpha = a; translationX = (1f - a) * -80f },
            ) {
                Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    RungDot(rung, 12.dp)
                    Spacer(Modifier.width(12.dp))
                    Text(rung.label, style = MaterialTheme.typography.titleMedium, color = rung.color())
                    Spacer(Modifier.width(12.dp))
                    Text(example, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

// =====================================================================
// Main: Now · Ladder · Vault
// =====================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(vm: NotifiiViewModel, sheets: Sheets, startTab: String, onSettings: () -> Unit) {
    var tab by rememberSaveable { mutableStateOf(startTab) }
    LaunchedEffect(startTab) { tab = startTab }
    val tabs = listOf(
        Triple("now", "Now", Icons.Outlined.TipsAndUpdates to Icons.Rounded.TipsAndUpdates),
        Triple("ladder", "Ladder", Icons.Outlined.Layers to Icons.Rounded.Layers),
        Triple("vault", "Vault", Icons.Outlined.Inventory2 to Icons.Rounded.Inventory2),
    )
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tabs.first { it.first == tab }.second, style = MaterialTheme.typography.headlineSmall) },
                actions = { IconButton(onClick = onSettings) { Icon(Icons.Outlined.Settings, contentDescription = "Settings") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
                tabs.forEach { (id, label, icons) ->
                    NavigationBarItem(
                        selected = tab == id,
                        onClick = { tab = id },
                        icon = { Icon(if (tab == id) icons.second else icons.first, contentDescription = null) },
                        label = { Text(label) },
                    )
                }
            }
        },
    ) { padding ->
        when (tab) {
            "ladder" -> LadderTab(vm, sheets, padding)
            "vault" -> VaultTab(vm, sheets, padding)
            else -> NowTab(vm, sheets, padding)
        }
    }
}

// =====================================================================
// Settings
// =====================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: NotifiiViewModel, sheets: Sheets, onBack: () -> Unit) {
    val context = LocalContext.current
    val pro by vm.prefs.isPro.collectAsState()
    val price by vm.billing.price.collectAsState()
    val readAloud by vm.prefs.readAloud.collectAsState()
    val flip by vm.prefs.flipToSilence.collectAsState()
    val keepDays by vm.prefs.keepDays.collectAsState()
    var confirmClear by remember { mutableStateOf(false) }
    val scroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scroll.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Settings") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back") } },
                scrollBehavior = scroll,
            )
        },
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = padding.calculateTopPadding() + 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Card(color = MaterialTheme.colorScheme.primaryContainer) {
                    if (pro) {
                        Text("Notifii Pro", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("Unlimited Siren and exceptions. Thank you for supporting an independent app.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    } else {
                        Text("Notifii Pro", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("Siren for everyone who matters, and unlimited exceptions. Pay once.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { sheets.current = Sheet.Paywall }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                            Text(if (price != null) "Get Pro · $price" else "Get Pro")
                        }
                    }
                }
            }
            item {
                Card {
                    Text("Siren", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(4.dp))
                    SettingSwitch("Read it aloud", "Says who it’s from and what it says.", readAloud) { vm.prefs.setReadAloud(it) }
                    SettingSwitch("Flip to silence", "Turn the phone face down to stop the alarm.", flip) { vm.prefs.setFlipToSilence(it) }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = { sheets.current = Sheet.SirenCheck }, modifier = Modifier.weight(1f)) { Text("Siren check") }
                        Button(onClick = {
                            Siren.start(context, SirenPayload(0, context.packageName, "Notifii", "This is a test Siren", "Flip the phone, press a volume key or tap Stop.", "You pressed Test Siren in Settings"))
                        }, modifier = Modifier.weight(1f)) { Text("Test Siren") }
                    }
                }
            }
            item {
                Card {
                    Text("Keep the Vault for", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(12.dp))
                    val options = listOf(30 to "30 days", 90 to "90 days", 0 to "Forever")
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                        options.forEachIndexed { i, (days, label) ->
                            SegmentedButton(selected = keepDays == days, onClick = { vm.prefs.setKeepDays(days) }, shape = SegmentedButtonDefaults.itemShape(i, options.size)) { Text(label) }
                        }
                    }
                }
            }
            item {
                Card {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Outlined.WifiOff, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("Private by design", style = MaterialTheme.typography.titleMedium)
                            Text("Notifii has no internet permission and no account. Your notifications never leave this phone, and they’re left out of cloud backups.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            item {
                Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
                    Row(Modifier.fillMaxWidth().clickable { confirmClear = true }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.DeleteForever, null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(16.dp))
                        Text("Empty the Vault", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            item {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Notifii ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Empty the Vault?") },
            text = { Text("Every saved notification is deleted for good. Your ladder and exceptions stay.") },
            confirmButton = { TextButton(onClick = { vm.clearVault(); confirmClear = false }) { Text("Empty") } },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun SettingSwitch(title: String, body: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().clickable { onChange(!checked) }.padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(16.dp))
        Switch(checked = checked, onCheckedChange = null)
    }
}
