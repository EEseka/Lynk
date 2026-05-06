package com.eeseka.lynk.discover.presentation.components

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource

@Composable
fun UserLocationMapMarker(
    userLatitude: Double,
    userLongitude: Double
) {
    val infiniteTransition = rememberInfiniteTransition(label = "user_pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 36f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_radius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    val userLocationSource = rememberGeoJsonSource(
        data = GeoJsonData.JsonString(
            """{ "type": "Feature", "geometry": { "type": "Point", "coordinates": [$userLongitude, $userLatitude] } }"""
        )
    )

    val userPinColor = Color(0xFF007AFF)
    val userPinHaloColor = Color.White

    CircleLayer(
        id = "user-location-pulse",
        source = userLocationSource,
        color = const(userPinColor.copy(alpha = pulseAlpha)),
        radius = const(pulseRadius.dp),
        strokeColor = const(Color.Transparent),
        strokeWidth = const(0.dp)
    )

    CircleLayer(
        id = "user-location-core",
        source = userLocationSource,
        color = const(userPinColor),
        radius = const(8.dp),
        strokeColor = const(userPinHaloColor),
        strokeWidth = const(3.dp)
    )
}