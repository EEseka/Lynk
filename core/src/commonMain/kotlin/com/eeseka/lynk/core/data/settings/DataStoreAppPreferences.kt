package com.eeseka.lynk.core.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.eeseka.lynk.core.domain.settings.AppPreferences
import com.eeseka.lynk.core.domain.settings.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreAppPreferences(
    private val dataStore: DataStore<Preferences>
) : AppPreferences {

    private companion object {
        val THEME_PREFERENCE_KEY = stringPreferencesKey("KEY_THEME_PREFERENCE")
        val PURE_BLACK_ENABLED_KEY = booleanPreferencesKey("KEY_PURE_BLACK_ENABLED")
        val PUSH_NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("KEY_PUSH_NOTIFICATIONS_ENABLED")
        val HAPTICS_ENABLED_KEY = booleanPreferencesKey("KEY_HAPTICS_ENABLED")
    }

    override val theme: Flow<AppTheme> = dataStore.data.map { preferences ->
        val currentPreference = preferences[THEME_PREFERENCE_KEY] ?: AppTheme.SYSTEM.name
        try {
            AppTheme.valueOf(currentPreference)
        } catch (_: Exception) {
            AppTheme.SYSTEM
        }
    }

    override val isPureBlackEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PURE_BLACK_ENABLED_KEY] ?: false
    }

    override val arePushNotificationsEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PUSH_NOTIFICATIONS_ENABLED_KEY] ?: true
    }

    override val areHapticsEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[HAPTICS_ENABLED_KEY] ?: true
    }

    override suspend fun setTheme(theme: AppTheme) {
        dataStore.edit { preferences ->
            preferences[THEME_PREFERENCE_KEY] = theme.name
        }
    }

    override suspend fun setPureBlackEnabled(isEnabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PURE_BLACK_ENABLED_KEY] = isEnabled
        }
    }

    override suspend fun setPushNotificationsEnabled(isEnabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PUSH_NOTIFICATIONS_ENABLED_KEY] = isEnabled
        }
    }

    override suspend fun setHapticsEnabled(isEnabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[HAPTICS_ENABLED_KEY] = isEnabled
        }
    }
}
