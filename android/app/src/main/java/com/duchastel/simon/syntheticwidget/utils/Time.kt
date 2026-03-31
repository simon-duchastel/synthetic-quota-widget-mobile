package com.duchastel.simon.syntheticwidget.utils

import android.content.Context
import com.duchastel.simon.syntheticwidget.R
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeParseException

fun formatRenewalTime(context: Context, isoTimestamp: String?): String? {
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
        val timeStr = "$displayHour:$minuteStr$amPm"
        context.getString(R.string.renews_at, timeStr)
    } catch (_: DateTimeParseException) {
        null
    }
}
