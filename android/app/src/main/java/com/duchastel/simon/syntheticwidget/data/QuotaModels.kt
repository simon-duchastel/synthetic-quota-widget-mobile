package com.duchastel.simon.syntheticwidget.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeParseException

@Serializable
data class QuotaResponse(
    @SerialName("subscription")
    val subscription: QuotaDetail,
    @SerialName("freeToolCalls")
    val freeToolCalls: QuotaDetail,
)

@Serializable
data class QuotaDetail(
    @SerialName("limit")
    val limit: Int,
    @SerialName("requests")
    val requests: Int,
    @SerialName("renewsAt")
    val renewsAt: String? = null
)

@Serializable
data class QuotaWidgetState(
    val subscriptionLimit: Int = 135,
    val subscriptionRequests: Int = 0,
    val toolLimit: Int = 500,
    val toolRequests: Int = 34,
    val subscriptionRenewsAt: String? = null,
    val toolRenewsAt: String? = null,
    val isLoading: Boolean = false,
) {
    val subscriptionRemaining: Int
        get() = subscriptionLimit - subscriptionRequests

    val toolRemaining: Int
        get() = toolLimit - toolRequests

    val subscriptionProgress: Float
        get() = if (subscriptionLimit > 0) subscriptionRequests.toFloat() / subscriptionLimit.toFloat() else 0f

    val toolProgress: Float
        get() = if (toolLimit > 0) toolRequests.toFloat() / toolLimit.toFloat() else 0f
}

fun formatRenewalTime(isoTimestamp: String?): String? {
    if (isoTimestamp == null || isoTimestamp == "Never!") return null
    
    return try {
        val instant = Instant.parse(isoTimestamp)
        val zonedDateTime = instant.atZone(ZoneId.systemDefault())
        val hour = zonedDateTime.hour
        val minute = zonedDateTime.minute
        val amPm = if (hour < 12) "am" else "pm"
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        val minuteStr = if (minute < 10) "0$minute" else "$minute"
        "renews at $displayHour:$minuteStr$amPm"
    } catch (_: DateTimeParseException) {
        null
    }
}
