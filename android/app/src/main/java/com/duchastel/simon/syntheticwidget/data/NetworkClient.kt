package com.duchastel.simon.syntheticwidget.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import javax.inject.Inject

class NetworkClient @Inject constructor(
    private val httpClient: HttpClient,
    private val authDataStore: AuthDataStore
) {
    companion object {
        private const val API_URL = "https://api.synthetic.new/v2/quotas"
    }

    suspend fun fetchQuotaData(): QuotaResponse {
        val apiKey = authDataStore.getApiKey()

        return httpClient.get(API_URL) {
            apiKey?.let { key ->
                header("Authorization", "Bearer $key")
            }
            header("Content-Type", "application/json")
        }.body<QuotaResponse>()
    }
}
