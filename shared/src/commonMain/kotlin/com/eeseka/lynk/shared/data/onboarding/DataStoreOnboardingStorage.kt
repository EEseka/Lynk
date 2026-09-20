package com.eeseka.lynk.shared.data.onboarding

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.eeseka.lynk.shared.domain.onboarding.OnboardingStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreOnboardingStorage(
    private val dataStore: DataStore<Preferences>
) : OnboardingStorage {

    private val hasSeenOnboardingKey = booleanPreferencesKey("KEY_HAS_SEEN_ONBOARDING")

    override val hasSeenOnboarding: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[hasSeenOnboardingKey] ?: false
    }

    override suspend fun setOnboardingCompleted() {
        dataStore.edit { preferences ->
            preferences[hasSeenOnboardingKey] = true
        }
    }
}
