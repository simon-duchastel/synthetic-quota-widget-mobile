package com.duchastel.simon.syntheticwidget.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_preferences")

object AuthDataStore {
    private val ENCRYPTED_API_KEY = stringPreferencesKey("encrypted_api_key")

    suspend fun saveApiKey(context: Context, apiKey: String) {
        val encrypted = KeystoreManager.encryptApiKey(apiKey)
        context.authDataStore.edit { preferences ->
            if (encrypted != null) {
                preferences[ENCRYPTED_API_KEY] = encrypted
            } else {
                preferences.remove(ENCRYPTED_API_KEY)
            }
        }
    }

    suspend fun getApiKey(context: Context): String? {
        val encrypted = context.authDataStore.data.first()[ENCRYPTED_API_KEY]
        return encrypted?.let { KeystoreManager.decryptApiKey(it) }
    }

    fun hasApiKey(context: Context): Flow<Boolean> {
        return context.authDataStore.data.map { preferences ->
            preferences[ENCRYPTED_API_KEY] != null
        }
    }

    suspend fun clearApiKey(context: Context) {
        KeystoreManager.clearApiKey()
        context.authDataStore.edit { preferences ->
            preferences.remove(ENCRYPTED_API_KEY)
        }
    }
}
