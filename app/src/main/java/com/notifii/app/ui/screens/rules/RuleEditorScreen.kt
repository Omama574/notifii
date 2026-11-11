package com.notifii.app.ui.screens.rules

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notifii.app.ui.components.AppPickerDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleEditorScreen(
    onNavigateBack: () -> Unit,
    viewModel: RuleEditorViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showAppPicker by remember { mutableStateOf(false) }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.ruleId > 0) "Edit Rule" else "New Rule") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.saveRule() }) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = state.label,
                onValueChange = { viewModel.onLabelChange(it) },
                label = { Text("Rule Name / Label") },
                placeholder = { Text("e.g., Stripe Payment Alert") },
                modifier = Modifier.fillMaxWidth()
            )

            // Natural Language Sentence Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Trigger Recipe",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "When ${state.appLabel} sends a notification containing \"${state.keywords.ifBlank { "any keyword" }}\" then play loud alarm.",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // App Selection
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAppPicker = true },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PhoneAndroid, contentDescription = null)
                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text(text = "Target App", style = MaterialTheme.typography.labelSmall)
                            Text(text = state.appLabel, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(text = "Change", color = MaterialTheme.colorScheme.primary)
                }
            }

            // Keyword Matching
            OutlinedTextField(
                value = state.keywords,
                onValueChange = { viewModel.onKeywordsChange(it) },
                label = { Text("Keywords (comma separated)") },
                placeholder = { Text("e.g., paid, order, urgent, wire") },
                modifier = Modifier.fillMaxWidth()
            )

            // Exclude Keywords
            OutlinedTextField(
                value = state.excludeKeywords,
                onValueChange = { viewModel.onExcludeKeywordsChange(it) },
                label = { Text("Exclude Keywords (optional)") },
                placeholder = { Text("e.g., promo, digest, reminder") },
                modifier = Modifier.fillMaxWidth()
            )

            // Sender Filter
            OutlinedTextField(
                value = state.senderFilter,
                onValueChange = { viewModel.onSenderChange(it) },
                label = { Text("Sender Filter (optional)") },
                placeholder = { Text("e.g., Boss, +123456789") },
                modifier = Modifier.fillMaxWidth()
            )

            // Regex Pattern
            OutlinedTextField(
                value = state.regexPattern,
                onValueChange = { viewModel.onRegexChange(it) },
                label = { Text("Regex Pattern (advanced)") },
                placeholder = { Text("e.g., \\$[0-9]+(\\.[0-9]{2})?") },
                modifier = Modifier.fillMaxWidth()
            )

            // Action Toggles
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Speak notification text (TTS)", style = MaterialTheme.typography.bodyLarge)
                        Switch(
                            checked = state.useTts,
                            onCheckedChange = { viewModel.onTtsToggle(it) }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Trigger even when screen is on", style = MaterialTheme.typography.bodyLarge)
                        Switch(
                            checked = !state.triggerOnScreenOff,
                            onCheckedChange = { viewModel.onScreenOffToggle(!it) }
                        )
                    }
                }
            }

            Button(
                onClick = { viewModel.saveRule() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(text = "Save Alert Rule")
            }
        }
    }

    if (showAppPicker) {
        AppPickerDialog(
            onDismiss = { showAppPicker = false },
            onAppSelected = { app ->
                viewModel.onAppSelected(app.packageName, app.label)
                showAppPicker = false
            }
        )
    }
}
