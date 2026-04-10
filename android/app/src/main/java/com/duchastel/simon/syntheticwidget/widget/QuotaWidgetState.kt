package com.duchastel.simon.syntheticwidget.widget

import kotlinx.serialization.Serializable

@Serializable
data class QuotaData(
    // Five-hour limit data
    val fiveHourLimitMax: Int,
    val fiveHourLimitRemaining: Int,
    val fiveHourLimitTickPercent: Double,
    val fiveHourLimitNextTickAt: String? = null,
    // Weekly token limit data
    val weeklyCreditsMax: String,
    val weeklyCreditsRemaining: String,
    val weeklyCreditsNextRegen: String,
    val weeklyCreditsPercentRemaining: Double,
    val weeklyCreditsNextRegenAt: String? = null,
) {
    val fiveHourLimitUsed: Int
        get() = fiveHourLimitMax - fiveHourLimitRemaining

    val fiveHourLimitProgress: Float
        get() = if (fiveHourLimitMax > 0) fiveHourLimitRemaining.toFloat() / fiveHourLimitMax.toFloat() else 0f

    val fiveHourLimitPercent: Float
        get() = if (fiveHourLimitMax > 0) (fiveHourLimitRemaining.toFloat() / fiveHourLimitMax.toFloat()) * 100f else 0f

    val weeklyCreditsProgress: Float
        get() = (weeklyCreditsPercentRemaining / 100.0).toFloat()
}

@Serializable
data class QuotaWidgetState(
    val quotaData: QuotaData? = null,
    val isLoading: Boolean = false,
    val isClearBackground: Boolean = false,
)
