package com.eeseka.lynk.testing.data

import com.eeseka.lynk.shared.domain.location.LastKnownLocationStorage
import com.eeseka.lynk.shared.domain.location.LocationCoordinates
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeLastKnownLocationStorage : LastKnownLocationStorage {
    private val lastKnownLocationFlow = MutableStateFlow<LocationCoordinates?>(null)
    override val lastKnownLocation: Flow<LocationCoordinates?> = lastKnownLocationFlow

    override suspend fun setLastKnownLocation(latitude: Double, longitude: Double) {
        lastKnownLocationFlow.value = LocationCoordinates(latitude = latitude, longitude = longitude)
    }
}
