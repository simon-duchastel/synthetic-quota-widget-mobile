package com.duchastel.simon.syntheticwidget.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import com.duchastel.simon.syntheticwidget.widget.QuotaWidget
import dagger.hilt.android.qualifiers.ApplicationContext
import okio.IOException
import javax.inject.Inject

class QuotaWidgetRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val networkClient: NetworkClient,
) {
    suspend fun getWidgetState(glanceWidgetId: GlanceId): QuotaWidgetState {
        return getAppWidgetState(
            context = context,
            definition = PreferencesGlanceStateDefinition,
            glanceId = glanceWidgetId
        ).toQuotaWidgetState()
    }

    suspend fun refreshData(glanceWidgetId: GlanceId): Boolean {
        // Fetch data from API
        return try {
            setIsLoading(glanceWidgetId, isLoading = true)

            val quotaResponse = networkClient.fetchQuotaData()

            updateAppWidgetState(context, glanceWidgetId) { preferences ->
                preferences[IS_LOADING] = false
                preferences[SUB_LIMIT] = quotaResponse.subscription.limit
                preferences[SUB_REQUESTS] = quotaResponse.subscription.requests
                preferences[TOOL_LIMIT] = quotaResponse.freeToolCalls.limit
                preferences[TOOL_REQUESTS] = quotaResponse.freeToolCalls.requests
                preferences[SUB_RENEWS_AT] = quotaResponse.subscription.renewsAt ?: "Never!"
                preferences[TOOL_RENEWS_AT] = quotaResponse.freeToolCalls.renewsAt ?: "Never!"
            }
            QuotaWidget().update(context, glanceWidgetId)

            true
        } catch (_: IOException) {
            setIsLoading(glanceWidgetId, isLoading = false)
            false
        }
    }

    suspend fun setIsLoading(glanceWidgetId: GlanceId, isLoading: Boolean) {
        updateAppWidgetState(context, glanceWidgetId) { preferences ->
            preferences[IS_LOADING] = isLoading
        }
        QuotaWidget().update(context, glanceWidgetId)
    }
}

private val SUB_LIMIT = intPreferencesKey("sub_limit")
private val SUB_REQUESTS = intPreferencesKey("sub_requests")
private val TOOL_LIMIT = intPreferencesKey("tool_limit")
private val TOOL_REQUESTS = intPreferencesKey("tool_requests")
private val SUB_RENEWS_AT = stringPreferencesKey("sub_renews_at")
private val TOOL_RENEWS_AT = stringPreferencesKey("tool_renews_at")
private val IS_LOADING = booleanPreferencesKey("is_loading")

fun Preferences.toQuotaWidgetState(): QuotaWidgetState {
    return QuotaWidgetState(
        subscriptionLimit = this[SUB_LIMIT] ?: 135,
        subscriptionRequests = this[SUB_REQUESTS] ?: 0,
        toolLimit = this[TOOL_LIMIT] ?: 500,
        toolRequests = this[TOOL_REQUESTS] ?: 34,
        subscriptionRenewsAt = this[SUB_RENEWS_AT],
        toolRenewsAt = this[TOOL_RENEWS_AT],
        isLoading = this[IS_LOADING] ?: false,
    )
}