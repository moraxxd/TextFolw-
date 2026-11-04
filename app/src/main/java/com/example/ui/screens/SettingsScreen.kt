package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import com.example.ui.theme.SuccessGreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.TextFlowApplication
import com.example.data.languages.SourceLanguage
import com.example.data.languages.TargetLanguage
import com.example.ui.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToModelManager: () -> Unit,
    onNavigateToTribalTesting: () -> Unit
) {
    val context = LocalContext.current
    val sourceLang by viewModel.sourceLanguage.collectAsState()
    val targetLang by viewModel.targetLanguage.collectAsState()
    val bubbleSize by viewModel.bubbleSize.collectAsState()
    val hapticEnabled by viewModel.hapticEnabled.collectAsState()
    val autoPunctuation by viewModel.autoPunctuation.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()

    val serverUrl by viewModel.serverUrl.collectAsState()
    val serverHealthState by viewModel.serverHealthState.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()

    var showSourceLangDialog by remember { mutableStateOf(false) }
    var showTargetLangDialog by remember { mutableStateOf(false) }
    var showBubbleSizeDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showServerUrlDialog by remember { mutableStateOf(false) }
    var tempServerUrl by remember(serverUrl) { mutableStateOf(serverUrl) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Section 1: Languages & Tribal Linguistics
            SettingsSectionHeader(title = "Languages & Tribal Support")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsRow(
                        icon = Icons.Default.Translate,
                        title = "Default Source Language",
                        subtitle = when (sourceLang) {
                            "gon" -> "गोण्डी (Gondi) - Tribal Central Dravidian"
                            "kfb" -> "कोलामी (Kolami) - Tribal Dravidian"
                            "hi" -> "Hindi (हिंदी)"
                            "mr" -> "Marathi (मराठी)"
                            "hinglish" -> "Hinglish (Mix)"
                            else -> "English (en-IN)"
                        },
                        onClick = { showSourceLangDialog = true }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    SettingsRow(
                        icon = Icons.Default.Translate,
                        title = "Default Output Language",
                        subtitle = if (targetLang == "mr") "मराठी (Marathi)" else "English (US / Global)",
                        onClick = { showTargetLangDialog = true }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    SettingsRow(
                        icon = Icons.Default.RecordVoiceOver,
                        title = "Tribal Language Native Evaluation",
                        subtitle = "Test Gondi & Kolami dialects and native accuracy",
                        onClick = onNavigateToTribalTesting
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    SettingsRow(
                        icon = Icons.Default.Download,
                        title = "Offline Language Models",
                        subtitle = "Manage Gondi, Kolami, and Hindi offline packs",
                        onClick = onNavigateToModelManager
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 2: Self-Hosted Server Configuration (Zero-Commercial AI)
            SettingsSectionHeader(title = "Self-Hosted Server (Zero-Commercial AI)")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsRow(
                        icon = Icons.Default.Cloud,
                        title = "Backend Server URL",
                        subtitle = serverUrl,
                        onClick = {
                            tempServerUrl = serverUrl
                            showServerUrlDialog = true
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Server Status",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = when (serverHealthState) {
                                    is MainViewModel.ServerHealthState.Idle -> if (isOnline) "Ready to test (Online)" else "Device is Offline (Internet required)"
                                    is MainViewModel.ServerHealthState.Checking -> "Connecting to self-hosted server..."
                                    is MainViewModel.ServerHealthState.Connected -> {
                                        val c = serverHealthState as MainViewModel.ServerHealthState.Connected
                                        "Connected: ${c.appName} (v${c.version}, ${c.device.uppercase()})"
                                    }
                                    is MainViewModel.ServerHealthState.Error -> {
                                        val e = serverHealthState as MainViewModel.ServerHealthState.Error
                                        "Error: ${e.message}"
                                    }
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = when (serverHealthState) {
                                    is MainViewModel.ServerHealthState.Connected -> SuccessGreen
                                    is MainViewModel.ServerHealthState.Error -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                        Button(
                            onClick = { viewModel.testServerConnection() },
                            modifier = Modifier.testTag("test_server_btn")
                        ) {
                            Text("Test")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 2: Direct Text Insertion (Keyboard IME)
            SettingsSectionHeader(title = "Text Insertion & Keyboard")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsRow(
                        icon = Icons.Default.Keyboard,
                        title = "TextFlow Keyboard Settings",
                        subtitle = "Enable TextFlow Keyboard to insert in 1 tap without pasting",
                        onClick = {
                            val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                            context.startActivity(intent)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 3: Floating Overlay & Controls
            SettingsSectionHeader(title = "Floating Overlay")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsRow(
                        icon = Icons.Default.Layers,
                        title = "Floating Bubble Size",
                        subtitle = when (bubbleSize) {
                            "small" -> "Compact (46dp)"
                            "large" -> "Large (66dp)"
                            else -> "Medium (56dp - Default)"
                        },
                        onClick = { showBubbleSizeDialog = true }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    SettingsRow(
                        icon = Icons.Default.RestartAlt,
                        title = "Reset Bubble Position",
                        subtitle = "Restore bubble to default screen coordinate",
                        onClick = {
                            CoroutineScope(Dispatchers.IO).launch {
                                TextFlowApplication.instance.preferencesRepository.saveBubblePosition(100, 300)
                            }
                            Toast.makeText(context, "Bubble position reset", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 4: Speech & Formatting
            SettingsSectionHeader(title = "Voice & Formatting")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsSwitchRow(
                        icon = Icons.Default.FormatQuote,
                        title = "Auto Capitalize & Punctuate",
                        subtitle = "Format English and Marathi sentences with capitalization and periods",
                        checked = autoPunctuation,
                        onCheckedChange = { viewModel.setAutoPunctuationPref(it) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    SettingsSwitchRow(
                        icon = Icons.Default.Vibration,
                        title = "Haptic Feedback",
                        subtitle = "Vibrate on mic start, stop, copy, and text insertion",
                        checked = hapticEnabled,
                        onCheckedChange = { viewModel.setHapticPref(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 5: Appearance
            SettingsSectionHeader(title = "Appearance")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                SettingsRow(
                    icon = Icons.Default.DarkMode,
                    title = "Theme",
                    subtitle = when (themeMode) {
                        "dark" -> "Dark Mode"
                        "light" -> "Light Mode"
                        else -> "System Default"
                    },
                    onClick = { showThemeDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 6: Privacy & Data
            SettingsSectionHeader(title = "Privacy & Data")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsRow(
                        icon = Icons.Default.Security,
                        title = "Privacy Policy & Zero-Tracking",
                        subtitle = "100% on-device tribal models • No cloud profiling",
                        onClick = { showPrivacyDialog = true }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    SettingsRow(
                        icon = Icons.Default.DeleteSweep,
                        title = "Clear Translation History",
                        subtitle = "Erase all saved history entries on this device",
                        onClick = { showClearHistoryDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Source Lang Dialog
    if (showSourceLangDialog) {
        val options = listOf(
            "gon" to "गोण्डी (Gondi) - Tribal Language",
            "kfb" to "कोलामी (Kolami) - Tribal Language",
            "hi" to "Hindi (हिंदी)",
            "mr" to "Marathi (मराठी)",
            "hinglish" to "Hinglish (Conversational Mix)",
            "en" to "English (Indian / International)"
        )
        AlertDialog(
            onDismissRequest = { showSourceLangDialog = false },
            title = { Text("Select Default Source Language") },
            text = {
                Column {
                    options.forEach { (code, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setSourceLang(code)
                                    showSourceLangDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = sourceLang == code,
                                onClick = {
                                    viewModel.setSourceLang(code)
                                    showSourceLangDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSourceLangDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Target Lang Dialog
    if (showTargetLangDialog) {
        val options = listOf(
            "en" to "English",
            "mr" to "मराठी (Marathi)"
        )
        AlertDialog(
            onDismissRequest = { showTargetLangDialog = false },
            title = { Text("Select Default Output Language") },
            text = {
                Column {
                    options.forEach { (code, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setTargetLang(code)
                                    showTargetLangDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = targetLang == code,
                                onClick = {
                                    viewModel.setTargetLang(code)
                                    showTargetLangDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showTargetLangDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Bubble Size Dialog
    if (showBubbleSizeDialog) {
        val options = listOf(
            "small" to "Small (46dp)",
            "medium" to "Medium (56dp - Recommended)",
            "large" to "Large (66dp)"
        )
        AlertDialog(
            onDismissRequest = { showBubbleSizeDialog = false },
            title = { Text("Floating Bubble Size") },
            text = {
                Column {
                    options.forEach { (code, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setBubbleSizePref(code)
                                    showBubbleSizeDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = bubbleSize == code,
                                onClick = {
                                    viewModel.setBubbleSizePref(code)
                                    showBubbleSizeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showBubbleSizeDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Theme Dialog
    if (showThemeDialog) {
        val options = listOf(
            "system" to "System Default",
            "dark" to "Dark Mode",
            "light" to "Light Mode"
        )
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Choose Theme") },
            text = {
                Column {
                    options.forEach { (code, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setThemeModePref(code)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = themeMode == code,
                                onClick = {
                                    viewModel.setThemeModePref(code)
                                    showThemeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showThemeDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Privacy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("TextFlow Tribal Privacy Guarantee") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "TextFlow Tribal Language Guarantee:\n\n" +
                                "1. Tribal Language Sovereignty: Gondi and Kolami speech and lexical data are processed 100% locally on your hardware.\n\n" +
                                "2. Zero Cloud Recordings: Microphones are only accessed during explicit user speech dictation.\n\n" +
                                "3. No Third-Party Cloud APIs: No data is sent to OpenAI, Gemini, ChatGPT, or external commercial translation APIs.\n\n" +
                                "4. Direct Keyboard Insertion: Uses Android's native InputMethodService to insert text directly into WhatsApp or Telegram without intercepting background messages.\n\n" +
                                "5. Full User Control: All local model packs and history entries can be inspected or deleted at any time.",
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showPrivacyDialog = false }) {
                    Text("Understood")
                }
            }
        )
    }

    // Clear History Dialog
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear All History?") },
            text = { Text("This will permanently remove all your saved translations from this device.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllHistory()
                        showClearHistoryDialog = false
                        Toast.makeText(context, "History erased", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Server URL Dialog
    if (showServerUrlDialog) {
        AlertDialog(
            onDismissRequest = { showServerUrlDialog = false },
            title = { Text("Configure Self-Hosted Server") },
            text = {
                Column {
                    Text(
                        text = "Enter the address of your private self-hosted TextFlow backend. Use 'http://10.0.2.2:8000/' for emulator, or your server's LAN IP / domain.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = tempServerUrl,
                        onValueChange = { tempServerUrl = it },
                        label = { Text("Server Base URL") },
                        placeholder = { Text("http://10.0.2.2:8000/") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = tempServerUrl.trim()
                        if (trimmed.isNotBlank()) {
                            viewModel.updateServerUrl(trimmed)
                            showServerUrlDialog = false
                            Toast.makeText(context, "Server URL saved", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Save & Connect")
                }
            },
            dismissButton = {
                TextButton(onClick = { showServerUrlDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
