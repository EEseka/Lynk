package com.eeseka.lynk.hangouts.presentation.hangout_detail.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Crosshair
import com.composables.icons.lucide.Lucide
import org.maplibre.compose.overlay.MapOverlayScope
import org.maplibre.spatialk.geojson.Position

private val CORE_DIAMETER = 36.dp
private val HALO_WIDTH = 4.dp
private val ICON_SIZE = 16.dp
private val PULSE_DIAMETER = 108.dp
private const val PULSE_CYCLES = 3
private const val PULSE_DURATION_MILLIS = 2000

private val CORE_SCALE = CORE_DIAMETER / PULSE_DIAMETER

@Composable
fun MapOverlayScope.GroupCenterMapMarker(
    centerLatitude: Double,
    centerLongitude: Double
) {
    val centerPinColor = MaterialTheme.colorScheme.secondaryContainer
    val centerPinHaloColor = MaterialTheme.colorScheme.surface
    val centerPinIconColor = MaterialTheme.colorScheme.onSecondaryContainer
    val centerPulseColor = MaterialTheme.colorScheme.secondary

    val pulseProgress = remember { Animatable(1f) }

    LaunchedEffect(centerLatitude, centerLongitude) {
        repeat(PULSE_CYCLES) {
            pulseProgress.snapTo(0f)
            pulseProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(PULSE_DURATION_MILLIS, easing = LinearOutSlowInEasing)
            )
        }
    }

    Box(
        modifier = Modifier.placedAt(
            position = Position(longitude = centerLongitude, latitude = centerLatitude),
            alignment = Alignment.Center
        ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(PULSE_DIAMETER)
                .graphicsLayer {
                    val progress = pulseProgress.value
                    val scale = CORE_SCALE + (1f - CORE_SCALE) * progress
                    scaleX = scale
                    scaleY = scale
                    alpha = (1f - progress) * 0.5f
                }
                .clip(CircleShape)
                .background(centerPulseColor)
        )

        Box(
            modifier = Modifier
                .size(CORE_DIAMETER)
                .clip(CircleShape)
                .background(centerPinColor)
                .border(HALO_WIDTH, centerPinHaloColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Lucide.Crosshair,
                contentDescription = null,
                tint = centerPinIconColor,
                modifier = Modifier.size(ICON_SIZE)
            )
        }
    }
}
