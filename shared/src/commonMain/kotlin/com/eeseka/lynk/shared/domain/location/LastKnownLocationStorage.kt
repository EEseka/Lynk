package com.eeseka.lynk.shared.domain.location

import kotlinx.coroutines.flow.Flow

interface LastKnownLocationStorage {
    val lastKnownLocation: Flow<LocationCoordinates?>

    suspend fun setLastKnownLocation(latitude: Double, longitude: Double)
}
