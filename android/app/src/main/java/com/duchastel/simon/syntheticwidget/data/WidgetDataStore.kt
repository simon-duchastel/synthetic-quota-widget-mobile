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

class WidgetDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val SUB_LIMIT = intPreferencesKey("sub_limit")
    private val SUB_REQUESTS = intPreferencesKey("sub_requests")
    private val TOOL_LIMIT = intPreferencesKey("tool_limit")
    private val TOOL_REQUESTS = intPreferencesKey("tool_requests")
    private val SUB_RENEWS_AT = stringPreferencesKey("sub_renews_at")
    private val TOOL_RENEWS_AT = stringPreferencesKey("tool_renews_at")
    private val IS_LOADING = booleanPreferencesKey("is_loading")

    suspend fun saveWidgetData(widgetData: QuotaWidgetState): QuotaWidgetState {
        dataStore.edit { preferences ->
            preferences[SUB_LIMIT] = widgetData.subscriptionLimit
            preferences[SUB_REQUESTS] = widgetData.subscriptionRequests
            preferences[TOOL_LIMIT] = widgetData.toolLimit
            preferences[TOOL_REQUESTS] = widgetData.toolRequests
            widgetData.subscriptionRenewsAt?.let { preferences[SUB_RENEWS_AT] = it }
            widgetData.toolRenewsAt?.let { preferences[TOOL_RENEWS_AT] = it }
            preferences[IS_LOADING] = widgetData.isLoading
        }
        return widgetData
    }

    suspend fun saveWidgetData(transform: suspend (QuotaWidgetState) -> QuotaWidgetState): QuotaWidgetState {
        return dataStore.updateData { preferences ->
            val currentData = QuotaWidgetState(
                subscriptionLimit = preferences[SUB_LIMIT] ?: 135,
                subscriptionRequests = preferences[SUB_REQUESTS] ?: 0,
                toolLimit = preferences[TOOL_LIMIT] ?: 500,
                toolRequests = preferences[TOOL_REQUESTS] ?: 34,
                subscriptionRenewsAt = preferences[SUB_RENEWS_AT],
                toolRenewsAt = preferences[TOOL_RENEWS_AT],
                isLoading = preferences[IS_LOADING] ?: false
            )
            transform(currentData)
        }.let {
            QuotaWidgetState(
                subscriptionLimit = it[SUB_LIMIT] ?: 135,
                subscriptionRequests = it[SUB_REQUESTS] ?: 0,
                toolLimit = it[TOOL_LIMIT] ?: 500,
                toolRequests = it[TOOL_REQUESTS] ?: 34,
                subscriptionRenewsAt = it[SUB_RENEWS_AT],
                toolRenewsAt = it[TOOL_RENEWS_AT],
                isLoading = it[IS_LOADING] ?: false
            )
        }
    }

    fun getWidgetData(): Flow<QuotaWidgetState> {
        return dataStore.data.map { preferences ->
            QuotaWidgetState(
                subscriptionLimit = preferences[SUB_LIMIT] ?: 135,
                subscriptionRequests = preferences[SUB_REQUESTS] ?: 0,
                toolLimit = preferences[TOOL_LIMIT] ?: 500,
                toolRequests = preferences[TOOL_REQUESTS] ?: 34,
                subscriptionRenewsAt = preferences[SUB_RENEWS_AT],
                toolRenewsAt = preferences[TOOL_RENEWS_AT],
                isLoading = preferences[IS_LOADING] ?: false
            )
        }
    }

    suspend fun saveFromResponse(response: QuotaResponse) {
        val widgetData = QuotaWidgetState(
            subscriptionLimit = response.subscription.limit,
            subscriptionRequests = response.subscription.requests,
            toolLimit = response.freeToolCalls.limit,
            toolRequests = response.freeToolCalls.requests,
            subscriptionRenewsAt = response.subscription.renewsAt,
            toolRenewsAt = response.freeToolCalls.renewsAt,
            isLoading = false
        )
        saveWidgetData(widgetData)
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
