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
    isLocationTurnedOn: () -> Boolean,
    onLocationTurnedOff: () -> Unit,
    onSharingChange: (Boolean) -> Unit,
    onLocationChange: (Double, Double) -> Unit
) {
    val currentLatitude by rememberUpdatedState(latitude)
    val currentLongitude by rememberUpdatedState(longitude)
    val currentIsLocationTurnedOn by rememberUpdatedState(isLocationTurnedOn)
    val currentOnLocationTurnedOff by rememberUpdatedState(onLocationTurnedOff)
    val currentOnSharingChange by rememberUpdatedState(onSharingChange)
    val currentOnLocationChange by rememberUpdatedState(onLocationChange)

    // Resends the last fix on a timer, since a phone standing still gets no new one and would look gone.
    // Stops sharing if location is turned off, or that old fix would keep going out as current.
    val hasFix = latitude != null && longitude != null
    LaunchedEffect(isSharing, hasFix) {
        if (!isSharing) return@LaunchedEffect

        while (true) {
            if (!currentIsLocationTurnedOn()) {
                currentOnLocationTurnedOff()
                return@LaunchedEffect
            }

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
