package com.duchastel.simon.syntheticwidget.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.state.updateAppWidgetState
import com.duchastel.simon.syntheticwidget.widget.QuotaWidget
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WidgetDataStore @Inject constructor(
    val dataStore: androidx.datastore.core.DataStore<Preferences>
) {
    companion object {
        val SUB_LIMIT = intPreferencesKey("sub_limit")
        val SUB_REQUESTS = intPreferencesKey("sub_requests")
        val TOOL_LIMIT = intPreferencesKey("tool_limit")
        val TOOL_REQUESTS = intPreferencesKey("tool_requests")
        val SUB_RENEWS_AT = stringPreferencesKey("sub_renews_at")
        val TOOL_RENEWS_AT = stringPreferencesKey("tool_renews_at")
        val IS_LOADING = booleanPreferencesKey("is_loading")
    }

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

    suspend fun setLoadingInWidget(context: Context, loading: Boolean) {
        updateAppWidgetState(context, QuotaWidget()) { preferences ->
            preferences[IS_LOADING] = loading
        }
    }

    suspend fun saveToWidgetState(context: Context, widgetData: QuotaWidgetState) {
        updateAppWidgetState(context, QuotaWidget()) { preferences ->
            preferences[SUB_LIMIT] = widgetData.subscriptionLimit
            preferences[SUB_REQUESTS] = widgetData.subscriptionRequests
            preferences[TOOL_LIMIT] = widgetData.toolLimit
            preferences[TOOL_REQUESTS] = widgetData.toolRequests
            widgetData.subscriptionRenewsAt?.let { preferences[SUB_RENEWS_AT] = it }
            widgetData.toolRenewsAt?.let { preferences[TOOL_RENEWS_AT] = it }
            preferences[IS_LOADING] = widgetData.isLoading
        }
    }

    fun isLoading(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[IS_LOADING] ?: false
        }
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetDataStoreEntryPoint {
    fun widgetDataStore(): WidgetDataStore
}

fun getWidgetDataStore(context: Context): WidgetDataStore {
    val entryPoint = EntryPointAccessors.fromApplication(
        context.applicationContext,
        WidgetDataStoreEntryPoint::class.java
    )
    return entryPoint.widgetDataStore()
}
