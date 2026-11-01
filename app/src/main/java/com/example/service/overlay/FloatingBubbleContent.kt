package com.example.service.overlay

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.MicRecordingGlow
import com.example.ui.theme.MicRecordingRed
import com.example.ui.theme.PrimaryDark
import com.example.ui.theme.SecondaryDark
import com.example.ui.theme.TextFlowTheme

@Composable
fun FloatingBubbleContent(
    bubbleSize: Dp = 56.dp,
    isListening: Boolean = false,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bubble_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.25f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bubble_scale"
    )

    TextFlowTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .size(bubbleSize + 16.dp)
                .testTag("floating_mic_bubble"),
            contentAlignment = Alignment.Center
        ) {
            // Pulsing halo when active/listening
            Box(
                modifier = Modifier
                    .size(bubbleSize + 8.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(
                        if (isListening) MicRecordingGlow
                        else PrimaryDark.copy(alpha = 0.25f)
                    )
            )

            // Inner vibrant button
            Box(
                modifier = Modifier
                    .size(bubbleSize)
                    .shadow(elevation = 10.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(
                        if (isListening) Brush.linearGradient(listOf(MicRecordingRed, Color(0xFFDC2626)))
                        else Brush.linearGradient(
                            listOf(
                                Color(0xFF4F46E5), // Indigo
                                Color(0xFF06B6D4)  // Cyan
                            )
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = 0.6f),
                                Color.White.copy(alpha = 0.1f)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "TextFlow Floating Microphone",
                    tint = Color.White,
                    modifier = Modifier.size(bubbleSize * 0.5f)
                )

                // Recording active badge dot
                if (isListening) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-4).dp, y = 4.dp)
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.5.dp, MicRecordingRed, CircleShape)
                    )
                }
            }
        }
    }
}
