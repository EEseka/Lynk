package com.eeseka.lynk.core.data.location

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import com.eeseka.lynk.core.domain.location.LastKnownLocationStorage
import com.eeseka.lynk.core.domain.location.LocationCoordinates
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreLastKnownLocationStorage(
    private val dataStore: DataStore<Preferences>
) : LastKnownLocationStorage {

    private companion object {
        val LAST_KNOWN_LATITUDE_KEY = doublePreferencesKey("KEY_LAST_KNOWN_LATITUDE")
        val LAST_KNOWN_LONGITUDE_KEY = doublePreferencesKey("KEY_LAST_KNOWN_LONGITUDE")
    }

    override val lastKnownLocation: Flow<LocationCoordinates?> = dataStore.data.map { preferences ->
        val latitude = preferences[LAST_KNOWN_LATITUDE_KEY]
        val longitude = preferences[LAST_KNOWN_LONGITUDE_KEY]

        if (latitude == null || longitude == null) {
            null
        } else {
            LocationCoordinates(latitude = latitude, longitude = longitude)
        }
    }

    override suspend fun setLastKnownLocation(latitude: Double, longitude: Double) {
        dataStore.edit { preferences ->
            preferences[LAST_KNOWN_LATITUDE_KEY] = latitude
            preferences[LAST_KNOWN_LONGITUDE_KEY] = longitude
        }
    }
}
