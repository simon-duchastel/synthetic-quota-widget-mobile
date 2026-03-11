package com.duchastel.simon.syntheticwidget.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

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
    }
}
