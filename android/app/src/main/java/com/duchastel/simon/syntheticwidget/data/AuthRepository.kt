package com.duchastel.simon.syntheticwidget.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface AuthRepository {
    fun getApiKey(): Flow<String?>
    suspend fun saveApiKey(apiKey: String)
    fun getMaskedApiKey(apiKey: String?): String
}

class AuthRepositoryImpl : AuthRepository {
    override fun getApiKey(): Flow<String?> {
        return flowOf(null)
    }

    override suspend fun saveApiKey(apiKey: String) {
    }

    override fun getMaskedApiKey(apiKey: String?): String {
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
