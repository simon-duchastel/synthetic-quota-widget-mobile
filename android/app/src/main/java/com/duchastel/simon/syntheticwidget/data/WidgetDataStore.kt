package com.duchastel.simon.syntheticwidget.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.widgetDataStore: DataStore<Preferences> by preferencesDataStore(name = "quota_preferences")

object WidgetDataStore : DataStore<QuotaWidgetState> {
    private val SUB_LIMIT = intPreferencesKey("sub_limit")
    private val SUB_REQUESTS = intPreferencesKey("sub_requests")
    private val TOOL_LIMIT = intPreferencesKey("tool_limit")
    private val TOOL_REQUESTS = intPreferencesKey("tool_requests")
    private val SUB_RENEWS_AT = stringPreferencesKey("sub_renews_at")
    private val TOOL_RENEWS_AT = stringPreferencesKey("tool_renews_at")
    private val IS_LOADING = booleanPreferencesKey("is_loading")

    override val data: Flow<QuotaWidgetState>
        get() = throw IllegalStateException("Use getData(context) instead")

    override suspend fun updateData(transform: suspend (QuotaWidgetState) -> QuotaWidgetState): QuotaWidgetState {
        throw IllegalStateException("Use saveWidgetData(context, transform) instead")
    }

    suspend fun saveWidgetData(context: Context, widgetData: QuotaWidgetState): QuotaWidgetState {
        context.widgetDataStore.edit { preferences ->
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

    suspend fun saveWidgetData(context: Context, transform: suspend (QuotaWidgetState) -> QuotaWidgetState): QuotaWidgetState {
        val currentData = getWidgetData(context)
        val newData = transform(currentData)
        return saveWidgetData(context, newData)
    }

    suspend fun getWidgetData(context: Context): QuotaWidgetState {
        return context.widgetDataStore.data.map { preferences ->
            QuotaWidgetState(
                subscriptionLimit = preferences[SUB_LIMIT] ?: 135,
                subscriptionRequests = preferences[SUB_REQUESTS] ?: 0,
                toolLimit = preferences[TOOL_LIMIT] ?: 500,
                toolRequests = preferences[TOOL_REQUESTS] ?: 34,
                subscriptionRenewsAt = preferences[SUB_RENEWS_AT],
                toolRenewsAt = preferences[TOOL_RENEWS_AT],
                isLoading = preferences[IS_LOADING] ?: false
            )
        }.first()
    }

    fun getWidgetDataFlow(context: Context): Flow<QuotaWidgetState> {
        return context.widgetDataStore.data.map { preferences ->
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

    suspend fun saveFromResponse(context: Context, response: QuotaResponse) {
        val widgetData = QuotaWidgetState(
            subscriptionLimit = response.subscription.limit,
            subscriptionRequests = response.subscription.requests,
            toolLimit = response.freeToolCalls.limit,
            toolRequests = response.freeToolCalls.requests,
            subscriptionRenewsAt = response.subscription.renewsAt,
            toolRenewsAt = response.freeToolCalls.renewsAt,
            isLoading = false
        )
        saveWidgetData(context, widgetData)
    }

    suspend fun setLoading(context: Context, loading: Boolean) {
        val currentData = getWidgetData(context)
        saveWidgetData(context, currentData.copy(isLoading = loading))
    }

    fun isLoading(context: Context): Flow<Boolean> {
        return context.widgetDataStore.data.map { preferences ->
            preferences[IS_LOADING] ?: false
        }
    }
}
