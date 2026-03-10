package com.duchastel.simon.syntheticwidget.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class QuotaDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val SUB_LIMIT = intPreferencesKey("sub_limit")
    private val SUB_REQUESTS = intPreferencesKey("sub_requests")
    private val TOOL_LIMIT = intPreferencesKey("tool_limit")
    private val TOOL_REQUESTS = intPreferencesKey("tool_requests")
    private val SUB_RENEWS_AT = stringPreferencesKey("sub_renews_at")
    private val TOOL_RENEWS_AT = stringPreferencesKey("tool_renews_at")
    private val IS_LOADING = booleanPreferencesKey("is_loading")
    
    suspend fun saveQuotaData(quotaData: QuotaData): QuotaData {
        dataStore.edit { preferences ->
            preferences[SUB_LIMIT] = quotaData.subscriptionLimit
            preferences[SUB_REQUESTS] = quotaData.subscriptionRequests
            preferences[TOOL_LIMIT] = quotaData.toolLimit
            preferences[TOOL_REQUESTS] = quotaData.toolRequests
            quotaData.subscriptionRenewsAt?.let { preferences[SUB_RENEWS_AT] = it }
            quotaData.toolRenewsAt?.let { preferences[TOOL_RENEWS_AT] = it }
        }
        return quotaData
    }

    suspend fun saveQuotaData(transform: suspend (QuotaData) -> QuotaData): QuotaData {
        return dataStore.updateData { preferences ->
            val currentData = QuotaData(
                subscriptionLimit = preferences[SUB_LIMIT] ?: 135,
                subscriptionRequests = preferences[SUB_REQUESTS] ?: 0,
                toolLimit = preferences[TOOL_LIMIT] ?: 500,
                toolRequests = preferences[TOOL_REQUESTS] ?: 34,
                subscriptionRenewsAt = preferences[SUB_RENEWS_AT],
                toolRenewsAt = preferences[TOOL_RENEWS_AT]
            )

           preferences.toMutablePreferences().apply { transform(currentData) }
        }.let {
            QuotaData(
                subscriptionLimit = it[SUB_LIMIT] ?: 135,
                subscriptionRequests = it[SUB_REQUESTS] ?: 0,
                toolLimit = it[TOOL_LIMIT] ?: 500,
                toolRequests = it[TOOL_REQUESTS] ?: 34,
                subscriptionRenewsAt = it[SUB_RENEWS_AT],
                toolRenewsAt = it[TOOL_RENEWS_AT]
            )
        }
    }


    fun getQuotaData(): Flow<QuotaData> {
        return dataStore.data.map { preferences ->
            QuotaData(
                subscriptionLimit = preferences[SUB_LIMIT] ?: 135,
                subscriptionRequests = preferences[SUB_REQUESTS] ?: 0,
                toolLimit = preferences[TOOL_LIMIT] ?: 500,
                toolRequests = preferences[TOOL_REQUESTS] ?: 34,
                subscriptionRenewsAt = preferences[SUB_RENEWS_AT],
                toolRenewsAt = preferences[TOOL_RENEWS_AT]
            )
        }
    }
    
    suspend fun saveFromResponse(response: QuotaResponse) {
        val quotaData = QuotaData(
            subscriptionLimit = response.subscription.limit,
            subscriptionRequests = response.subscription.requests,
            toolLimit = response.freeToolCalls.limit,
            toolRequests = response.freeToolCalls.requests,
            subscriptionRenewsAt = response.subscription.renewsAt,
            toolRenewsAt = response.freeToolCalls.renewsAt
        )
        saveQuotaData(quotaData)
    }

    suspend fun setLoading(loading: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_LOADING] = loading
        }
    }

    fun isLoading(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[IS_LOADING] ?: false
        }
    }
}
