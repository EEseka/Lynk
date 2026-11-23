package com.eeseka.lynk.core.data.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.eeseka.lynk.core.data.auth.dto.AuthInfoSerializable
import com.eeseka.lynk.core.data.auth.mappers.toDomain
import com.eeseka.lynk.core.data.auth.mappers.toSerializable
import com.eeseka.lynk.core.domain.auth.model.AuthInfo
import com.eeseka.lynk.core.domain.auth.SessionStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class DataStoreSessionStorage(
    private val dataStore: DataStore<Preferences>
) : SessionStorage {

    private companion object {
        val AUTH_INFO_KEY = stringPreferencesKey("KEY_AUTH_INFO")
    }

    private val json = Json {
        ignoreUnknownKeys = true
    }

    override fun observeAuthInfo(): Flow<AuthInfo?> {
        return dataStore.data.map { preferences ->
            val serializedJson = preferences[AUTH_INFO_KEY]
            serializedJson?.let {
                try {
                    json.decodeFromString<AuthInfoSerializable>(it).toDomain()
                } catch (_: Exception) {
                    null
                }
            }
        }
    }

    override suspend fun set(info: AuthInfo?) {
        if (info == null) {
            dataStore.edit {
                it.remove(AUTH_INFO_KEY)
            }
            return
        }

        val serialized = json.encodeToString(info.toSerializable())
        dataStore.edit { prefs ->
            prefs[AUTH_INFO_KEY] = serialized
        }
    }
}