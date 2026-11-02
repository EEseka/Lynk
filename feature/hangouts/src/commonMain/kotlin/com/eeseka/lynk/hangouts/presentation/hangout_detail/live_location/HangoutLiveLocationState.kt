package com.eeseka.lynk.hangouts.presentation.hangout_detail.live_location

import androidx.compose.runtime.Immutable
import com.eeseka.lynk.hangouts.presentation.hangout_detail.live_location.model.LiveLocationUi
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf

@Immutable
data class HangoutLiveLocationState(
    val liveLocations: ImmutableMap<String, LiveLocationUi> = persistentMapOf(), // userId -> where they are
    val isSharingLocation: Boolean = false
)
