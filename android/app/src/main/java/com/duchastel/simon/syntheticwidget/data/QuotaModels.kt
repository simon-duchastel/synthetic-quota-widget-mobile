package com.duchastel.simon.syntheticwidget.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuotaResponse(
    @SerialName("rollingFiveHourLimit")
    val rollingFiveHourLimit: RollingFiveHourLimit,
    @SerialName("weeklyTokenLimit")
    val weeklyTokenLimit: WeeklyTokenLimit,
)

@Serializable
data class RollingFiveHourLimit(
    @SerialName("nextTickAt")
    val nextTickAt: String? = null,
    @SerialName("tickPercent")
    val tickPercent: Double,
    @SerialName("remaining")
    val remaining: Int,
    @SerialName("max")
    val max: Int,
    @SerialName("limited")
    val limited: Boolean,
)

@Serializable
data class WeeklyTokenLimit(
    @SerialName("nextRegenAt")
    val nextRegenAt: String? = null,
    @SerialName("percentRemaining")
    val percentRemaining: Double,
    @SerialName("maxCredits")
    val maxCredits: String,
    @SerialName("remainingCredits")
    val remainingCredits: String,
    @SerialName("nextRegenCredits")
    val nextRegenCredits: String,
)
