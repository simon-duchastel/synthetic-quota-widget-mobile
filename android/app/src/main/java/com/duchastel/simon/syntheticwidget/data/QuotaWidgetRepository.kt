package com.duchastel.simon.syntheticwidget.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.duchastel.simon.syntheticwidget.widget.QuotaData
import com.duchastel.simon.syntheticwidget.widget.QuotaWidget
import com.duchastel.simon.syntheticwidget.widget.QuotaWidgetState
import dagger.hilt.android.qualifiers.ApplicationContext
import okio.IOException
import javax.inject.Inject

class QuotaWidgetRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val networkClient: NetworkClient,
) {
    suspend fun getWidgetState(glanceWidgetId: GlanceId): QuotaWidgetState {
        val prefs = getAppWidgetState(
            context = context,
            definition = PreferencesGlanceStateDefinition,
            glanceId = glanceWidgetId
        )
        return prefs.toQuotaWidgetState()
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
                if (quotaResponse.subscription.renewsAt != null) {
                    preferences[SUB_RENEWS_AT] = quotaResponse.subscription.renewsAt
                } else {
                    preferences -= SUB_RENEWS_AT
                }
                if (quotaResponse.freeToolCalls.renewsAt != null) {
                    preferences[TOOL_RENEWS_AT] = quotaResponse.freeToolCalls.renewsAt
                } else {
                    preferences -= TOOL_RENEWS_AT
                }
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

    suspend fun setClearBackground(glanceWidgetId: GlanceId, isClearBackground: Boolean) {
        updateAppWidgetState(context, glanceWidgetId) { preferences ->
            preferences[IS_CLEAR_BACKGROUND] = isClearBackground
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
private val IS_CLEAR_BACKGROUND = booleanPreferencesKey("is_clear_background")

fun Preferences.toQuotaWidgetState(): QuotaWidgetState {
    // Required fields - if any are missing, we don't have valid quota data
    val subLimit = this[SUB_LIMIT]
    val subRequests = this[SUB_REQUESTS]
    val toolLimit = this[TOOL_LIMIT]
    val toolRequests = this[TOOL_REQUESTS]

    val quotaData = if (subLimit != null && subRequests != null && toolLimit != null && toolRequests != null) {
        QuotaData(
            subscriptionLimit = subLimit,
            subscriptionRequests = subRequests,
            toolLimit = toolLimit,
            toolRequests = toolRequests,
            subscriptionRenewsAt = this[SUB_RENEWS_AT],
            toolRenewsAt = this[TOOL_RENEWS_AT],
        )
    } else null

    return QuotaWidgetState(
        quotaData = quotaData,
        isLoading = this[IS_LOADING] ?: false,
        isClearBackground = this[IS_CLEAR_BACKGROUND] ?: false,
    )
}