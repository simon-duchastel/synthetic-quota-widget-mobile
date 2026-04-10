package com.duchastel.simon.syntheticwidget.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
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
                // Five-hour limit data
                preferences[FIVE_HOUR_MAX] = quotaResponse.rollingFiveHourLimit.max
                preferences[FIVE_HOUR_REMAINING] = quotaResponse.rollingFiveHourLimit.remaining
                preferences[FIVE_HOUR_TICK_PERCENT] = quotaResponse.rollingFiveHourLimit.tickPercent
                if (quotaResponse.rollingFiveHourLimit.nextTickAt != null) {
                    preferences[FIVE_HOUR_NEXT_TICK_AT] = quotaResponse.rollingFiveHourLimit.nextTickAt
                } else {
                    preferences -= FIVE_HOUR_NEXT_TICK_AT
                }
                // Weekly token limit data
                preferences[WEEKLY_MAX_CREDITS] = quotaResponse.weeklyTokenLimit.maxCredits
                preferences[WEEKLY_REMAINING_CREDITS] = quotaResponse.weeklyTokenLimit.remainingCredits
                preferences[WEEKLY_NEXT_REGEN_CREDITS] = quotaResponse.weeklyTokenLimit.nextRegenCredits
                preferences[WEEKLY_PERCENT_REMAINING] = quotaResponse.weeklyTokenLimit.percentRemaining
                if (quotaResponse.weeklyTokenLimit.nextRegenAt != null) {
                    preferences[WEEKLY_NEXT_REGEN_AT] = quotaResponse.weeklyTokenLimit.nextRegenAt
                } else {
                    preferences -= WEEKLY_NEXT_REGEN_AT
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

// Five-hour limit keys
private val FIVE_HOUR_MAX = intPreferencesKey("five_hour_max")
private val FIVE_HOUR_REMAINING = intPreferencesKey("five_hour_remaining")
private val FIVE_HOUR_TICK_PERCENT = doublePreferencesKey("five_hour_tick_percent")
private val FIVE_HOUR_NEXT_TICK_AT = stringPreferencesKey("five_hour_next_tick_at")

// Weekly token limit keys
private val WEEKLY_MAX_CREDITS = stringPreferencesKey("weekly_max_credits")
private val WEEKLY_REMAINING_CREDITS = stringPreferencesKey("weekly_remaining_credits")
private val WEEKLY_NEXT_REGEN_CREDITS = stringPreferencesKey("weekly_next_regen_credits")
private val WEEKLY_PERCENT_REMAINING = doublePreferencesKey("weekly_percent_remaining")
private val WEEKLY_NEXT_REGEN_AT = stringPreferencesKey("weekly_next_regen_at")

// Other keys
private val IS_LOADING = booleanPreferencesKey("is_loading")
private val IS_CLEAR_BACKGROUND = booleanPreferencesKey("is_clear_background")

fun Preferences.toQuotaWidgetState(): QuotaWidgetState {
    // Required fields for five-hour limit
    val fiveHourMax = this[FIVE_HOUR_MAX]
    val fiveHourRemaining = this[FIVE_HOUR_REMAINING]
    val fiveHourTickPercent = this[FIVE_HOUR_TICK_PERCENT]

    // Required fields for weekly limit
    val weeklyMaxCredits = this[WEEKLY_MAX_CREDITS]
    val weeklyRemainingCredits = this[WEEKLY_REMAINING_CREDITS]
    val weeklyNextRegenCredits = this[WEEKLY_NEXT_REGEN_CREDITS]
    val weeklyPercentRemaining = this[WEEKLY_PERCENT_REMAINING]

    val quotaData = if (fiveHourMax != null && fiveHourRemaining != null && fiveHourTickPercent != null &&
        weeklyMaxCredits != null && weeklyRemainingCredits != null && weeklyNextRegenCredits != null && weeklyPercentRemaining != null) {
        QuotaData(
            fiveHourLimitMax = fiveHourMax,
            fiveHourLimitRemaining = fiveHourRemaining,
            fiveHourLimitTickPercent = fiveHourTickPercent,
            fiveHourLimitNextTickAt = this[FIVE_HOUR_NEXT_TICK_AT],
            weeklyCreditsMax = weeklyMaxCredits,
            weeklyCreditsRemaining = weeklyRemainingCredits,
            weeklyCreditsNextRegen = weeklyNextRegenCredits,
            weeklyCreditsPercentRemaining = weeklyPercentRemaining,
            weeklyCreditsNextRegenAt = this[WEEKLY_NEXT_REGEN_AT],
        )
    } else null

    return QuotaWidgetState(
        quotaData = quotaData,
        isLoading = this[IS_LOADING] ?: false,
        isClearBackground = this[IS_CLEAR_BACKGROUND] ?: false,
    )
}
