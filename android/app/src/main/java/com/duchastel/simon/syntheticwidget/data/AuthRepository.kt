package com.duchastel.simon.syntheticwidget.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

interface AuthRepository {
    suspend fun saveApiKey(apiKey: String)
    suspend fun getApiKey(): String?
    fun getMaskedApiKey(): Flow<String>
}

class AuthRepositoryImpl @Inject constructor(
    private val authDataStore: AuthDataStore
) : AuthRepository {

    override suspend fun saveApiKey(apiKey: String) {
        authDataStore.saveApiKey(apiKey)
    }

    override suspend fun getApiKey(): String? {
        return authDataStore.getApiKey()
    }

    override fun getMaskedApiKey(): Flow<String> {
        return flow {
            val apiKey = authDataStore.getApiKey()
            emit(ApiKeyMasker.mask(apiKey))
        }
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
