package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.TextFlowApplication
import com.example.data.languages.SourceLanguage
import com.example.data.languages.TargetLanguage
import com.example.data.translation.ModelDownloadState
import com.example.data.translation.tribal.TribalDownloadStatus
import com.example.ui.MainViewModel
import com.example.ui.theme.PrimaryDark
import com.example.ui.theme.SecondaryDark
import com.example.ui.theme.SuccessGreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslationModelScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val modelState by viewModel.modelDownloadState.collectAsState()

    val tribalManager = remember { TextFlowApplication.instance.translationManager.tribalModelManager }
    val gondiInstalled by tribalManager.isGondiInstalled.collectAsState(initial = true)
    val kolamiInstalled by tribalManager.isKolamiInstalled.collectAsState(initial = true)
    val gondiStatus by tribalManager.gondiStatus.collectAsState()
    val kolamiStatus by tribalManager.kolamiStatus.collectAsState()

    var testInput by remember { mutableStateOf("सेवाल, कुशियता मन्ता?") }
    var testSourceLang by remember { mutableStateOf("gon") }
    var testTargetLang by remember { mutableStateOf("en") }
    var testResult by remember { mutableStateOf("") }
    var isTestingTranslation by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Language Model Manager", fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Storage Overview Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Offline On-Device Language Packs",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            Text(
                text = "Models run 100% locally on your phone without cloud services or external APIs.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Gondi Tribal Language Pack Card
            LanguagePackCard(
                title = "Gondi Language Pack",
                subtitle = "Central Dravidian (Vidarbha, MP, Telangana)",
                sizeMb = 18,
                isInstalled = gondiInstalled,
                downloadStatus = gondiStatus,
                isTribal = true,
                onDownload = {
                    tribalManager.downloadGondiPack(
                        onSuccess = { Toast.makeText(context, "Gondi Pack ready!", Toast.LENGTH_SHORT).show() },
                        onError = { Toast.makeText(context, "Download failed: $it", Toast.LENGTH_SHORT).show() }
                    )
                },
                onDelete = {
                    tribalManager.deleteGondiPack()
                    Toast.makeText(context, "Gondi Pack removed", Toast.LENGTH_SHORT).show()
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Kolami Tribal Language Pack Card
            LanguagePackCard(
                title = "Kolami Language Pack",
                subtitle = "Kolam Tribe (Yavatmal, Adilabad)",
                sizeMb = 14,
                isInstalled = kolamiInstalled,
                downloadStatus = kolamiStatus,
                isTribal = true,
                onDownload = {
                    tribalManager.downloadKolamiPack(
                        onSuccess = { Toast.makeText(context, "Kolami Pack ready!", Toast.LENGTH_SHORT).show() },
                        onError = { Toast.makeText(context, "Download failed: $it", Toast.LENGTH_SHORT).show() }
                    )
                },
                onDelete = {
                    tribalManager.deleteKolamiPack()
                    Toast.makeText(context, "Kolami Pack removed", Toast.LENGTH_SHORT).show()
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Hindi-English Indic Model Card
            LanguagePackCard(
                title = "Hindi <-> English Offline Pack",
                subtitle = "Standard Hindi machine translation pack",
                sizeMb = 30,
                isInstalled = modelState is ModelDownloadState.Downloaded,
                downloadStatus = when (modelState) {
                    is ModelDownloadState.Downloaded -> TribalDownloadStatus.Installed
                    is ModelDownloadState.Downloading -> TribalDownloadStatus.Downloading(0.5f)
                    is ModelDownloadState.Error -> TribalDownloadStatus.Error((modelState as ModelDownloadState.Error).error)
                    else -> TribalDownloadStatus.Idle
                },
                isTribal = false,
                onDownload = {
                    viewModel.downloadOfflineModel(
                        onSuccess = { Toast.makeText(context, "Hindi model ready!", Toast.LENGTH_SHORT).show() },
                        onError = { Toast.makeText(context, "Download failed: $it", Toast.LENGTH_SHORT).show() }
                    )
                },
                onDelete = {
                    viewModel.deleteOfflineModel()
                    Toast.makeText(context, "Hindi model deleted", Toast.LENGTH_SHORT).show()
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Interactive Test Bench
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Offline Translation Sandbox",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Verify translation directly across Gondi, Kolami, Marathi and English",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Preset Quick Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PresetChip("सेवाल (Gondi)") {
                            testInput = "सेवाल, कुशियता मन्ता?"
                            testSourceLang = "gon"
                        }
                        PresetChip("राम-राम (Kolami)") {
                            testInput = "राम-राम, आम् कोलामी पाटा ककडा"
                            testSourceLang = "kfb"
                        }
                        PresetChip("गातो तिनका") {
                            testInput = "येर सीम, गातो तिनका"
                            testSourceLang = "gon"
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = testInput,
                        onValueChange = { testInput = it },
                        label = { Text("Input Phrase") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Source & Target selector in sandbox
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            FilterChip(
                                selected = testSourceLang == "gon",
                                onClick = { testSourceLang = "gon" },
                                label = { Text("Gondi", fontSize = 10.sp) }
                            )
                            FilterChip(
                                selected = testSourceLang == "kfb",
                                onClick = { testSourceLang = "kfb" },
                                label = { Text("Kolami", fontSize = 10.sp) }
                            )
                            FilterChip(
                                selected = testSourceLang == "hi",
                                onClick = { testSourceLang = "hi" },
                                label = { Text("Hindi", fontSize = 10.sp) }
                            )
                        }

                        Text("->", fontWeight = FontWeight.Bold)

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            FilterChip(
                                selected = testTargetLang == "en",
                                onClick = { testTargetLang = "en" },
                                label = { Text("English", fontSize = 10.sp) }
                            )
                            FilterChip(
                                selected = testTargetLang == "mr",
                                onClick = { testTargetLang = "mr" },
                                label = { Text("Marathi", fontSize = 10.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            isTestingTranslation = true
                            scope.launch {
                                val result = TextFlowApplication.instance.translationManager.translateText(
                                    inputText = testInput,
                                    sourceLang = testSourceLang,
                                    targetLang = testTargetLang
                                )
                                result.onSuccess {
                                    testResult = it
                                    isTestingTranslation = false
                                }.onFailure {
                                    testResult = "Status: ${it.localizedMessage}"
                                    isTestingTranslation = false
                                }
                            }
                        },
                        enabled = !isTestingTranslation && testInput.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isTestingTranslation) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Translating Offline...")
                        } else {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Translate Offline")
                        }
                    }

                    if (testResult.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Translation Result (${if (testTargetLang == "mr") "Marathi" else "English"}):",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = testResult,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Privacy Notice
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tribal Language Sovereignty & Privacy",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Gondi and Kolami translations are processed strictly on your local hardware using open-source linguistic models. No audio or text leaves your device, respecting community data privacy.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun LanguagePackCard(
    title: String,
    subtitle: String,
    sizeMb: Int,
    isInstalled: Boolean,
    downloadStatus: TribalDownloadStatus,
    isTribal: Boolean,
    onDownload: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isInstalled) SuccessGreen.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (isInstalled) SuccessGreen.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isInstalled) Icons.Default.CheckCircle else Icons.Default.Download,
                            contentDescription = null,
                            tint = if (isInstalled) SuccessGreen else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            if (isTribal) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.tertiary)
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text("TRIBAL", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                        Text(
                            text = "$subtitle • ~$sizeMb MB",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (isInstalled) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    Button(
                        onClick = onDownload,
                        modifier = Modifier.height(34.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Download", fontSize = 11.sp)
                    }
                }
            }

            if (downloadStatus is TribalDownloadStatus.Downloading) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { downloadStatus.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(CircleShape)
                )
            }
        }
    }
}

@Composable
private fun PresetChip(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}
