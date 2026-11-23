package com.eeseka.lynk.core.data.onboarding

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.eeseka.lynk.core.domain.onboarding.OnboardingStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreOnboardingStorage(
    private val dataStore: DataStore<Preferences>
) : OnboardingStorage {

    private companion object {
        val HAS_SEEN_ONBOARDING_KEY = booleanPreferencesKey("KEY_HAS_SEEN_ONBOARDING")
    }

    override val hasSeenOnboarding: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[HAS_SEEN_ONBOARDING_KEY] ?: false
    }

    override suspend fun setOnboardingCompleted() {
        dataStore.edit { preferences ->
            preferences[HAS_SEEN_ONBOARDING_KEY] = true
        }
    }
}
