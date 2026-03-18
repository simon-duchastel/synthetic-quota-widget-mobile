package com.duchastel.simon.syntheticwidget.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class AuthDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val API_KEYS_DATA = stringPreferencesKey("api_keys_data")

    suspend fun saveApiKey(apiKey: ApiKeyEntry) {
        val currentKeys = getApiKeys().toMutableList()
        val existingIndex = currentKeys.indexOfFirst { it.id == apiKey.id }
        if (existingIndex >= 0) {
            currentKeys[existingIndex] = apiKey
        } else {
            currentKeys.add(apiKey)
        }
        saveApiKeys(currentKeys)
    }

    suspend fun deleteApiKey(id: String) {
        val currentKeys = getApiKeys().toMutableList()
        currentKeys.removeAll { it.id == id }
        saveApiKeys(currentKeys)
    }

    suspend fun getApiKey(id: String): ApiKeyEntry? {
        return getApiKeys().find { it.id == id }
    }

    suspend fun getApiKeys(): List<ApiKeyEntry> {
        val encrypted = dataStore.data.first()[API_KEYS_DATA]
        return if (encrypted != null) {
            try {
                val decrypted = KeystoreManager.decryptApiKey(encrypted)
                if (decrypted != null) {
                    Json.decodeFromString(decrypted)
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    fun getApiKeysFlow(): Flow<List<ApiKeyEntry>> {
        return dataStore.data.map { preferences ->
            val encrypted = preferences[API_KEYS_DATA]
            if (encrypted != null) {
                try {
                    val decrypted = KeystoreManager.decryptApiKey(encrypted)
                    if (decrypted != null) {
                        Json.decodeFromString(decrypted)
                    } else {
                        emptyList()
                    }
                } catch (e: Exception) {
                    emptyList()
                }
            } else {
                emptyList()
            }
        }
    }

    private suspend fun saveApiKeys(apiKeys: List<ApiKeyEntry>) {
        val json = Json.encodeToString(apiKeys)
        val encrypted = KeystoreManager.encryptApiKey(json)
        dataStore.edit { preferences ->
            if (encrypted != null) {
                preferences[API_KEYS_DATA] = encrypted
            } else {
                preferences.remove(API_KEYS_DATA)
            }
        }
    }

    suspend fun clearApiKeys() {
        KeystoreManager.clearApiKey()
        dataStore.edit { preferences ->
            preferences.remove(API_KEYS_DATA)
        }
    }
}
