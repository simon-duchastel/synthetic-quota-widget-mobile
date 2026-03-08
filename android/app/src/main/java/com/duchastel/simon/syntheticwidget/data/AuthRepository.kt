package com.duchastel.simon.syntheticwidget.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface AuthRepository {
    suspend fun saveApiKey(apiKey: String)
    fun getMaskedApiKey(): Flow<String>
}

class AuthRepositoryImpl : AuthRepository {
    
    private fun getApiKey(): Flow<String?> {
        return flowOf(null)
    }

    override suspend fun saveApiKey(apiKey: String) {
    }

    override fun getMaskedApiKey(): Flow<String> {
        return flowOf("")
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
