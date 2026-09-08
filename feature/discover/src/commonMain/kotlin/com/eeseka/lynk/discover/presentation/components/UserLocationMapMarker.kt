package com.eeseka.lynk.discover.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import org.maplibre.compose.overlay.MapOverlayScope
import org.maplibre.spatialk.geojson.Position

private val userPinColor = Color(0xFF007AFF)
private val userPinHaloColor = Color.White

private val CORE_DIAMETER = 24.dp
private val HALO_WIDTH = 3.dp
private val PULSE_DIAMETER = 72.dp
private const val PULSE_CYCLES = 3
private const val PULSE_DURATION_MILLIS = 1600

private val CORE_SCALE = CORE_DIAMETER / PULSE_DIAMETER

@Composable
fun MapOverlayScope.UserLocationMapMarker(
    userLatitude: Double,
    userLongitude: Double,
    pulseKey: Int
) {
    val pulseProgress = remember { Animatable(1f) }

    LaunchedEffect(pulseKey) {
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
            position = Position(longitude = userLongitude, latitude = userLatitude),
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
                .background(userPinColor)
        )

        Box(
            modifier = Modifier
                .size(CORE_DIAMETER)
                .clip(CircleShape)
                .background(userPinColor)
                .border(HALO_WIDTH, userPinHaloColor, CircleShape)
        )
    }
}
