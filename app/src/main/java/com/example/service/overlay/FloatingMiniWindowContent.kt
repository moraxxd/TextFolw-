package com.example.service.overlay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.languages.SourceLanguage
import com.example.data.languages.TargetLanguage
import com.example.ui.theme.MicRecordingGlow
import com.example.ui.theme.MicRecordingRed
import com.example.ui.theme.PrimaryDark
import com.example.ui.theme.SecondaryDark
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextFlowTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FloatingMiniWindowContent(
    isListening: Boolean,
    soundLevel: Float,
    statusText: String,
    recognizedText: String,
    translatedText: String,
    sourceLanguage: String,
    targetLanguage: String,
    isModelDownloaded: Boolean,
    isTranslating: Boolean,
    onToggleListening: () -> Unit,
    onSourceLangChange: (String) -> Unit,
    onTargetLangChange: (String) -> Unit,
    onTranslatedTextEdited: (String) -> Unit,
    onInsertText: (String) -> Unit,
    onCopyText: (String) -> Unit,
    onShareText: (String) -> Unit,
    onClearAll: () -> Unit,
    onMinimize: () -> Unit,
    onClose: () -> Unit,
    onDownloadModel: () -> Unit,
    onRequestImeFocus: () -> Unit
) {
    var copiedConfirmation by remember { mutableStateOf(false) }
    var insertedConfirmation by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val currentSource = SourceLanguage.fromCode(sourceLanguage)
    val currentTarget = TargetLanguage.fromCode(targetLanguage)

    TextFlowTheme(darkTheme = true) {
        Surface(
            modifier = Modifier
                .widthIn(max = 390.dp)
                .fillMaxWidth(0.96f)
                .shadow(elevation = 16.dp, shape = RoundedCornerShape(24.dp))
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            PrimaryDark.copy(alpha = 0.5f),
                            SecondaryDark.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .clip(RoundedCornerShape(24.dp))
                .testTag("floating_mini_window"),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(14.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Bar with Brand & Window Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(PrimaryDark, SecondaryDark))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "TextFlow Logo",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "TextFlow",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (currentSource.isTribal) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(MaterialTheme.colorScheme.tertiary)
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "TRIBAL",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "Tribal Voice-to-Text & Translate",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Window Controls: Minimize & Close
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onMinimize,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("btn_minimize_overlay")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Minimize to bubble",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("btn_close_overlay")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close overlay",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Source Languages (Scrollable row with tribal indicators)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Speak In (Source):",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (currentSource.isTribal) {
                            Text(
                                text = currentSource.region,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Tribal languages first
                        LanguageChip(
                            label = "गोण्डी (Gondi)",
                            selected = sourceLanguage == "gon",
                            isTribalBadge = true,
                            onClick = { onSourceLangChange("gon") }
                        )
                        LanguageChip(
                            label = "कोलामी (Kolami)",
                            selected = sourceLanguage == "kfb",
                            isTribalBadge = true,
                            onClick = { onSourceLangChange("kfb") }
                        )
                        LanguageChip(
                            label = "हिन्दी (Hindi)",
                            selected = sourceLanguage == "hi",
                            isTribalBadge = false,
                            onClick = { onSourceLangChange("hi") }
                        )
                        LanguageChip(
                            label = "मराठी (Marathi)",
                            selected = sourceLanguage == "mr",
                            isTribalBadge = false,
                            onClick = { onSourceLangChange("mr") }
                        )
                        LanguageChip(
                            label = "English",
                            selected = sourceLanguage == "en",
                            isTribalBadge = false,
                            onClick = { onSourceLangChange("en") }
                        )
                        LanguageChip(
                            label = "Hinglish",
                            selected = sourceLanguage == "hinglish",
                            isTribalBadge = false,
                            onClick = { onSourceLangChange("hinglish") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Target Language Selector (English or Marathi)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Translate To (Output):",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        LanguageChip(
                            label = "English",
                            selected = targetLanguage == "en",
                            isTribalBadge = false,
                            onClick = { onTargetLangChange("en") }
                        )
                        LanguageChip(
                            label = "मराठी (Marathi)",
                            selected = targetLanguage == "mr",
                            isTribalBadge = false,
                            onClick = { onTargetLangChange("mr") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Microphone & Live Audio Waveform Section
                RecordingSection(
                    isListening = isListening,
                    soundLevel = soundLevel,
                    statusText = statusText,
                    onToggleListening = onToggleListening
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Offline Model Warning Banner if missing
                if (!isModelDownloaded && (sourceLanguage == "hi" || sourceLanguage == "hinglish" || targetLanguage == "hi")) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Offline Model Missing",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Text(
                                    text = "Download language pack for offline conversion",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                            Button(
                                onClick = onDownloadModel,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                                modifier = Modifier.height(32.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Download", fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Recognized Speech Preview Box
                if (recognizedText.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Recognized (${currentSource.displayName}):",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${recognizedText.split(" ").filter { it.isNotBlank() }.size} words",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = recognizedText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Converted Output Box (Editable)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Converted (${currentTarget.displayName}):",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.secondary
                        )

                        if (isTranslating) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Translating...",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = translatedText,
                        onValueChange = onTranslatedTextEdited,
                        placeholder = {
                            Text(
                                text = "Converted text appears here. Tap to edit or insert...",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(88.dp)
                            .testTag("translated_text_input")
                            .clickable { onRequestImeFocus() },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.secondary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Action Buttons: Insert Text, Copy Text, Share, Clear
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val textToActOn = if (translatedText.isNotBlank()) translatedText else recognizedText
                    val hasText = textToActOn.isNotBlank()

                    // 1. Insert Text Button (Direct to active chat via InputMethodService)
                    Button(
                        onClick = {
                            if (hasText) {
                                onInsertText(textToActOn)
                                insertedConfirmation = true
                                coroutineScope.launch {
                                    delay(2000)
                                    insertedConfirmation = false
                                }
                            }
                        },
                        enabled = hasText,
                        modifier = Modifier
                            .weight(1.1f)
                            .height(44.dp)
                            .testTag("btn_insert_text"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (insertedConfirmation) SuccessGreen else MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = if (insertedConfirmation) Icons.Default.Check else Icons.Default.Input,
                            contentDescription = "Insert text",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (insertedConfirmation) "Inserted!" else "Insert",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    // 2. Copy Text Button (Copies to ClipboardManager)
                    Button(
                        onClick = {
                            if (hasText) {
                                onCopyText(textToActOn)
                                copiedConfirmation = true
                                coroutineScope.launch {
                                    delay(2000)
                                    copiedConfirmation = false
                                }
                            }
                        },
                        enabled = hasText,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("btn_copy_final_text"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (copiedConfirmation) SuccessGreen else MaterialTheme.colorScheme.secondary,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = if (copiedConfirmation) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = "Copy text",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (copiedConfirmation) "Copied!" else "Copy",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    // 3. Share Button
                    OutlinedButton(
                        onClick = {
                            if (hasText) {
                                onShareText(textToActOn)
                            }
                        },
                        enabled = hasText,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("btn_share_text"),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // 4. Clear Button
                    OutlinedButton(
                        onClick = onClearAll,
                        enabled = hasText,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("btn_clear_text"),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Clear",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LanguageChip(
    label: String,
    selected: Boolean,
    isTribalBadge: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (selected) {
                    if (isTribalBadge) MaterialTheme.colorScheme.tertiary
                    else MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) {
                if (isTribalBadge) Color.White
                else MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Composable
fun RecordingSection(
    isListening: Boolean,
    soundLevel: Float,
    statusText: String,
    onToggleListening: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.25f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isListening) MicRecordingRed.copy(alpha = 0.12f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Big Animated Mic Button
            Box(
                modifier = Modifier.size(76.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isListening) {
                    Box(
                        modifier = Modifier
                            .size(74.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(MicRecordingGlow)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(CircleShape)
                        .background(
                            if (isListening) Brush.linearGradient(listOf(MicRecordingRed, Color(0xFFDC2626)))
                            else Brush.linearGradient(listOf(PrimaryDark, SecondaryDark))
                        )
                        .clickable(onClick = onToggleListening)
                        .testTag("btn_overlay_mic_toggle"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicOff,
                        contentDescription = if (isListening) "Stop Recording" else "Start Recording",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Status message
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isListening) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (isListening) MicRecordingRed else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Audio Waveform Visualizer
            AudioWaveform(
                isListening = isListening,
                soundLevel = soundLevel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
            )
        }
    }
}

@Composable
fun AudioWaveform(
    isListening: Boolean,
    soundLevel: Float,
    modifier: Modifier = Modifier
) {
    val barCount = 14
    val heights = remember {
        listOf(0.2f, 0.4f, 0.7f, 0.5f, 0.9f, 0.6f, 1.0f, 0.8f, 0.5f, 0.7f, 0.4f, 0.6f, 0.3f, 0.2f)
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until barCount) {
            val base = heights[i % heights.size]
            val animatedHeight by animateFloatAsState(
                targetValue = if (isListening) {
                    ((base * 0.4f) + (soundLevel * base * 0.6f)).coerceIn(0.15f, 1.0f)
                } else {
                    0.15f
                },
                animationSpec = tween(durationMillis = 100),
                label = "bar_$i"
            )

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxWidth(0.04f)
                    .height((24 * animatedHeight).dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (isListening) {
                            if (i % 2 == 0) MicRecordingRed else SecondaryDark
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        }
                    )
            )
        }
    }
}
