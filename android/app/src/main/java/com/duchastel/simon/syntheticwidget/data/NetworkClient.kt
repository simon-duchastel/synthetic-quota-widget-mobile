package com.duchastel.simon.syntheticwidget.data

import android.content.Context
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object NetworkClient {
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    private const val API_URL = "https://api.synthetic.com/v1/quota"

    suspend fun fetchQuotaData(context: Context): QuotaResponse {
        val apiKey = QuotaDataStore.getApiKey(context)

        return try {
            client.get(API_URL) {
                apiKey?.let { key ->
                    header("Authorization", "Bearer $key")
                }
                header("Content-Type", "application/json")
            }.body<QuotaResponse>()
        } catch (_: Exception) {
            QuotaResponse(
                subscription = QuotaDetail(
                    limit = 135,
                    requests = 0,
                    renewsAt = System.currentTimeMillis().plus(86400000).toString()
                ),
                freeToolCalls = QuotaDetail(
                    limit = 500,
                    requests = 34,
                    renewsAt = System.currentTimeMillis().plus(85260000).toString()
                )
            )
        }
    }
}
