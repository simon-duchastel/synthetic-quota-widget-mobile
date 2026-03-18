package com.duchastel.simon.syntheticwidget.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface AuthRepository {
    suspend fun saveApiKey(apiKey: ApiKeyEntry)
    suspend fun deleteApiKey(id: String)
    suspend fun getApiKey(id: String): ApiKeyEntry?
    suspend fun getApiKeys(): List<ApiKeyEntry>
    fun getApiKeysFlow(): Flow<List<ApiKeyEntry>>
}

class AuthRepositoryImpl @Inject constructor(
    private val authDataStore: AuthDataStore
) : AuthRepository {

    override suspend fun saveApiKey(apiKey: ApiKeyEntry) {
        authDataStore.saveApiKey(apiKey)
    }

    override suspend fun deleteApiKey(id: String) {
        authDataStore.deleteApiKey(id)
    }

    override suspend fun getApiKey(id: String): ApiKeyEntry? {
        return authDataStore.getApiKey(id)
    }

    override suspend fun getApiKeys(): List<ApiKeyEntry> {
        return authDataStore.getApiKeys()
    }

    override fun getApiKeysFlow(): Flow<List<ApiKeyEntry>> {
        return authDataStore.getApiKeysFlow()
    }
}

object ApiKeyMasker {
    fun mask(apiKey: String?): String {
        if (apiKey.isNullOrEmpty()) {
            return ""
        }

        if (!apiKey.startsWith("syn_")) {
            return "*".repeat(apiKey.length)
        }

        val prefix = "syn_"
        val suffixLength = 4
        val middleLength = apiKey.length - prefix.length - suffixLength

        return if (middleLength > 0) {
            prefix + "*".repeat(middleLength) + apiKey.takeLast(suffixLength)
        } else {
            apiKey
        }
    }
}
