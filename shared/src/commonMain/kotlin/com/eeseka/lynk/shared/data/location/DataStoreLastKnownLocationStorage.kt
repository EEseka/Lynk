package com.eeseka.lynk.shared.data.location

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import com.eeseka.lynk.shared.domain.location.LastKnownLocationStorage
import com.eeseka.lynk.shared.domain.location.LocationCoordinates
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreLastKnownLocationStorage(
    private val dataStore: DataStore<Preferences>
) : LastKnownLocationStorage {

    private val latitudeKey = doublePreferencesKey("KEY_LAST_KNOWN_LATITUDE")
    private val longitudeKey = doublePreferencesKey("KEY_LAST_KNOWN_LONGITUDE")

    override val lastKnownLocation: Flow<LocationCoordinates?> = dataStore.data.map { preferences ->
        val latitude = preferences[latitudeKey]
        val longitude = preferences[longitudeKey]

        if (latitude == null || longitude == null) {
            null
        } else {
            LocationCoordinates(latitude = latitude, longitude = longitude)
        }
    }

    override suspend fun setLastKnownLocation(latitude: Double, longitude: Double) {
        dataStore.edit { preferences ->
            preferences[latitudeKey] = latitude
            preferences[longitudeKey] = longitude
        }
    }
}
