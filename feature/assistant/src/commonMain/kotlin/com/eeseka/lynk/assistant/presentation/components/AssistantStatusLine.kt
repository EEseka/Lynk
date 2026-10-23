package com.eeseka.lynk.assistant.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme

@Composable
fun AssistantStatusLine(
    title: String,
    modifier: Modifier = Modifier
) {
    val highlight = MaterialTheme.colorScheme.onSurface
    val sweep = rememberInfiniteTransition(label = "status_shimmer").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "status_shimmer_sweep"
    )

    AnimatedContent(
        targetState = title,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        modifier = modifier
    ) { currentTitle ->
        LynkText(
            text = currentTitle,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                // Offscreen so the sweep only lights the letters, not the space around them
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                    drawContent()
                    val bandWidth = size.width / 2
                    val bandStart = -bandWidth + (size.width + bandWidth) * sweep.value
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, highlight, Color.Transparent),
                            startX = bandStart,
                            endX = bandStart + bandWidth
                        ),
                        topLeft = Offset.Zero,
                        size = size,
                        blendMode = BlendMode.SrcAtop
                    )
                }
        )
    }
}

@PreviewLightDark
@Composable
private fun AssistantStatusLinePreview() {
    LynkTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            AssistantStatusLine(title = "Thinking…")
            AssistantStatusLine(title = "Looking for spots…")
        }
    }
}
