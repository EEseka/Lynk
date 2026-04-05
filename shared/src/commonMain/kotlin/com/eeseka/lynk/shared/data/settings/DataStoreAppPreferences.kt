package com.eeseka.lynk.shared.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.eeseka.lynk.shared.domain.settings.AppPreferences
import com.eeseka.lynk.shared.domain.settings.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreAppPreferences(
    private val dataStore: DataStore<Preferences>
) : AppPreferences {

    private val themePreferenceKey = stringPreferencesKey("KEY_THEME_PREFERENCE")
    private val hasSeenOnboardingKey = booleanPreferencesKey("KEY_HAS_SEEN_ONBOARDING")

    override val theme: Flow<AppTheme> = dataStore.data.map { preferences ->
        val currentPreference = preferences[themePreferenceKey] ?: AppTheme.SYSTEM.name
        try {
            AppTheme.valueOf(currentPreference)
        } catch (_: Exception) {
            AppTheme.SYSTEM
        }
    }

    override val hasSeenOnboarding: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[hasSeenOnboardingKey] ?: false
    }

    override suspend fun setTheme(theme: AppTheme) {
        dataStore.edit { preferences ->
            preferences[themePreferenceKey] = theme.name
        }
    }

    override suspend fun setOnboardingCompleted() {
        dataStore.edit { preferences ->
            preferences[hasSeenOnboardingKey] = true
        }
    }
}