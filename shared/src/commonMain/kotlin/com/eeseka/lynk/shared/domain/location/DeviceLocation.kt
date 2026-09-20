package com.eeseka.lynk.shared.domain.location

import kotlin.time.Duration

interface DeviceLocation {
    suspend fun getLastKnownLocation(maxAge: Duration): LocationCoordinates?

    fun isLocationTurnedOn(): Boolean
}
