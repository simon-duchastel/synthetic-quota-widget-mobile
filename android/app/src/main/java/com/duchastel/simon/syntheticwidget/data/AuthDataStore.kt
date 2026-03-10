package com.duchastel.simon.syntheticwidget.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AuthDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val ENCRYPTED_API_KEY = stringPreferencesKey("encrypted_api_key")

    suspend fun saveApiKey(apiKey: String) {
        val encrypted = KeystoreManager.encryptApiKey(apiKey)
        dataStore.edit { preferences ->
            if (encrypted != null) {
                preferences[ENCRYPTED_API_KEY] = encrypted
            } else {
                preferences.remove(ENCRYPTED_API_KEY)
            }
        }
    }

    suspend fun getApiKey(): String? {
        val encrypted = dataStore.data.first()[ENCRYPTED_API_KEY]
        return encrypted?.let { KeystoreManager.decryptApiKey(it) }
    }

    fun hasApiKey(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[ENCRYPTED_API_KEY] != null
        }
    }

    suspend fun clearApiKey() {
        KeystoreManager.clearApiKey()
        dataStore.edit { preferences ->
            preferences.remove(ENCRYPTED_API_KEY)
        }
    }
}
