package com.eeseka.lynk.core.domain.location

import kotlin.time.Duration

interface DeviceLocation {
    suspend fun getLastKnownLocation(maxAge: Duration): LocationCoordinates?

    fun isLocationTurnedOn(): Boolean
}
