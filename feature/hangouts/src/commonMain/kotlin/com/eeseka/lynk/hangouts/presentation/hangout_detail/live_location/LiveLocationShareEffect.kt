package com.eeseka.lynk.hangouts.presentation.hangout_detail.live_location

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

private val LIVE_LOCATION_SEND_INTERVAL = 15.seconds

@Composable
fun LiveLocationShareEffect(
    isSharing: Boolean,
    canShare: Boolean,
    latitude: Double?,
    longitude: Double?,
    onSharingChange: (Boolean) -> Unit,
    onLocationChange: (Double, Double) -> Unit
) {
    val currentLatitude by rememberUpdatedState(latitude)
    val currentLongitude by rememberUpdatedState(longitude)
    val currentOnSharingChange by rememberUpdatedState(onSharingChange)
    val currentOnLocationChange by rememberUpdatedState(onLocationChange)

    // Also the heartbeat: a phone standing still reports no new fix, but it must not look gone
    LaunchedEffect(isSharing, latitude != null) {
        if (!isSharing) return@LaunchedEffect

        while (true) {
            val sharedLatitude = currentLatitude
            val sharedLongitude = currentLongitude
            if (sharedLatitude != null && sharedLongitude != null) {
                currentOnLocationChange(sharedLatitude, sharedLongitude)
            }
            delay(LIVE_LOCATION_SEND_INTERVAL)
        }
    }

    LaunchedEffect(canShare) {
        if (!canShare) currentOnSharingChange(false)
    }

    // Sharing lasts only while this map is on screen
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { currentOnSharingChange(false) }
    DisposableEffect(Unit) {
        onDispose { currentOnSharingChange(false) }
    }
}
