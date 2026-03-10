package com.duchastel.simon.syntheticwidget.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import com.duchastel.simon.syntheticwidget.widget.QuotaWidget
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    companion object {
        val SUB_LIMIT = intPreferencesKey("sub_limit")
        val SUB_REQUESTS = intPreferencesKey("sub_requests")
        val TOOL_LIMIT = intPreferencesKey("tool_limit")
        val TOOL_REQUESTS = intPreferencesKey("tool_requests")
        val SUB_RENEWS_AT = stringPreferencesKey("sub_renews_at")
        val TOOL_RENEWS_AT = stringPreferencesKey("tool_renews_at")
        val IS_LOADING = booleanPreferencesKey("is_loading")
        val WIDGET_ID = stringPreferencesKey("widget_id")
    }

    suspend fun updateWidgetState(state: QuotaWidgetState) {
        updateAppWidgetState(context, QuotaWidget::class.java) { prefs ->
            prefs.toMutablePreferences().apply {
                this[SUB_LIMIT] = state.subscriptionLimit
                this[SUB_REQUESTS] = state.subscriptionRequests
                this[TOOL_LIMIT] = state.toolLimit
                this[TOOL_REQUESTS] = state.toolRequests
                state.subscriptionRenewsAt?.let { this[SUB_RENEWS_AT] = it }
                state.toolRenewsAt?.let { this[TOOL_RENEWS_AT] = it }
                this[IS_LOADING] = state.isLoading
            }
        }
        // Update all widgets to reflect the new state
        QuotaWidget().updateAll(context)
    }

    suspend fun updateQuotaData(
        subscriptionLimit: Int,
        subscriptionRequests: Int,
        toolLimit: Int,
        toolRequests: Int,
        subscriptionRenewsAt: String? = null,
        toolRenewsAt: String? = null
    ) {
        updateWidgetState(
            QuotaWidgetState(
                subscriptionLimit = subscriptionLimit,
                subscriptionRequests = subscriptionRequests,
                toolLimit = toolLimit,
                toolRequests = toolRequests,
                subscriptionRenewsAt = subscriptionRenewsAt,
                toolRenewsAt = toolRenewsAt,
                isLoading = false
            )
        )
    }

    suspend fun setLoading(isLoading: Boolean) {
        updateAppWidgetState(context, QuotaWidget::class.java) { prefs ->
            prefs.toMutablePreferences().apply {
                this[IS_LOADING] = isLoading
            }
        }
        // Update all widgets to reflect the loading state
        QuotaWidget().updateAll(context)
    }
}
