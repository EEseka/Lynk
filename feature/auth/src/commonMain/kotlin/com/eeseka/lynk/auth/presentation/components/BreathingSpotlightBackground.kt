package com.eeseka.lynk.auth.presentation.components

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.eeseka.lynk.shared.design_system.theme.LynkTheme

@Composable
fun BreathingSpotlightBackground(modifier: Modifier = Modifier) {
    val primaryColor = MaterialTheme.colorScheme.primaryContainer
    val backgroundColor = MaterialTheme.colorScheme.background

    val transparentBackground = backgroundColor.copy(alpha = 0f)

    val infiniteTransition = rememberInfiniteTransition(label = "spotlight_breathing")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(color = backgroundColor)

        val width = size.width
        val height = size.height

        val centerX = width / 2f
        val centerY = -height * 0.1f

        val animatedRadius = (width * 0.8f) * pulseScale

        val scaleProgress = (pulseScale - 0.85f) / 0.3f

        val animatedAlpha = 0.4f + (scaleProgress * 0.2f)

        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    primaryColor.copy(alpha = animatedAlpha),
                    transparentBackground
                ),
                center = Offset(centerX, centerY),
                radius = animatedRadius
            )
        )
    }
}

@PreviewLightDark
@Composable
private fun BreathingSpotlightBackgroundPreview() {
    LynkTheme {
        BreathingSpotlightBackground(
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        )
    }
}