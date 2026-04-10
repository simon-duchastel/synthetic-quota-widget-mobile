package com.duchastel.simon.syntheticwidget.ui.widget

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duchastel.simon.syntheticwidget.R
import com.duchastel.simon.syntheticwidget.utils.formatTimeUntil
import com.duchastel.simon.syntheticwidget.widget.QuotaWidgetState

@Composable
fun QuotaWidgetScreen(
    quotaWidgetState: QuotaWidgetState,
    onRefreshClick: () -> Unit = {}
) {
    val isDarkTheme = isSystemInDarkTheme()

    QuotaWidgetContent(
        quotaWidgetState = quotaWidgetState,
        isDarkTheme = isDarkTheme,
        onRefreshClick = onRefreshClick
    )
}

@Composable
fun QuotaWidgetContent(
    quotaWidgetState: QuotaWidgetState,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    onRefreshClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val quotaData = quotaWidgetState.quotaData
    val isInitialized = quotaData != null
    val isClearBackground = quotaWidgetState.isClearBackground

    // Compute derived values - use 0 if not initialized
    val fiveHourProgress = if (isInitialized) {
        quotaData.fiveHourLimitProgress
    } else 0f

    val weeklyProgress = if (isInitialized) {
        quotaData.weeklyCreditsProgress
    } else 0f

    val backgroundColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color(0xFFF5F5F5)
    val textColorPrimary = if (isDarkTheme) Color(0xFFF3F4F6) else Color(0xFF1F2937)
    val textColorSecondary = if (isDarkTheme) Color(0xFFD1D5DB) else Color(0xFF374151)
    val textColorTertiary = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)

    // Grey colors for uninitialized state
    val greyBarColor = Color(0xFF9CA3AF)
    val greyBackgroundColor = Color(0xFFE5E7EB)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isClearBackground) {
                Color.Transparent
            } else {
                backgroundColor
            })
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            // Five-Hour Limit Section (Green theme, grey if uninitialized)
            // Shows: "Remaining five-hour requests: <progress bar> XX.X%"
            // and "Regenerates X% (X requests) in X minutes"
            QuotaBar(
                title = stringResource(R.string.five_hour_title),
                percent = if (isInitialized) quotaData.fiveHourLimitPercent else null,
                progress = fiveHourProgress,
                barColor = if (isInitialized) Color(0xFF10B981) else greyBarColor,
                backgroundColor = if (isInitialized) Color(0xFFA7F3D0) else greyBackgroundColor,
                isDarkTheme = isDarkTheme,
                renewalText = if (isInitialized) {
                    val timeUntil = formatTimeUntil(quotaData.fiveHourLimitNextTickAt)
                    if (timeUntil != null) {
                        val tickPercent = (quotaData.fiveHourLimitTickPercent * 100).toInt()
                        val tickAmount = (quotaData.fiveHourLimitMax * quotaData.fiveHourLimitTickPercent).toInt()
                        "Regenerates $tickPercent% (${tickAmount} requests) in $timeUntil"
                    } else {
                        ""
                    }
                } else {
                    ""
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Weekly Credits Section (Blue theme, grey if uninitialized)
            // Shows: "Remaining weekly credits: <progress bar> XX.X%"
            // and "Regenerates X% ($X.XX) in X minutes/hours"
            QuotaBar(
                title = stringResource(R.string.weekly_title),
                percent = if (isInitialized) quotaData.weeklyCreditsPercentRemaining.toFloat() else null,
                progress = weeklyProgress,
                barColor = if (isInitialized) Color(0xFF3B82F6) else greyBarColor,
                backgroundColor = if (isInitialized) Color(0xFF93C5FD) else greyBackgroundColor,
                isDarkTheme = isDarkTheme,
                renewalText = if (isInitialized) {
                    val timeUntil = formatTimeUntil(quotaData.weeklyCreditsNextRegenAt)
                    if (timeUntil != null) {
                        // Calculate regen percent based on nextRegenCredits / maxCredits
                        val maxCreditsValue = quotaData.weeklyCreditsMax.replace("$", "").toDoubleOrNull() ?: 0.0
                        val nextRegenValue = quotaData.weeklyCreditsNextRegen.replace("$", "").toDoubleOrNull() ?: 0.0
                        val regenPercent = if (maxCreditsValue > 0) (nextRegenValue / maxCreditsValue * 100).toInt() else 0
                        "Regenerates $regenPercent% (${quotaData.weeklyCreditsNextRegen}) in $timeUntil"
                    } else {
                        ""
                    }
                } else {
                    ""
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Refresh button - only show when initialized
            if (isInitialized) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
                ) {
                    if (quotaWidgetState.isLoading) {
                        // Show loading text
                        Text(
                            text = stringResource(R.string.loading),
                            modifier = Modifier.width(56.dp),
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                color = textColorPrimary
                            )
                        )
                    } else {
                        // Show refresh button with increased tap target
                        Box(
                            modifier = Modifier
                                .clickable { onRefreshClick() }
                                .padding(12.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_refresh),
                                contentDescription = stringResource(R.string.refresh),
                                modifier = Modifier.size(24.dp),
                                tint = textColorTertiary
                            )
                        }
                    }
                }
            }
        }

        // Loading overlay button - show when not initialized (centered over the bars)
        if (!isInitialized) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_refresh),
                    contentDescription = stringResource(R.string.load_data),
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { onRefreshClick() },
                    tint = textColorTertiary
                )
            }
        }
    }
}

@Composable
fun QuotaBar(
    title: String,
    percent: Float?,
    progress: Float,
    barColor: Color,
    backgroundColor: Color,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    renewalText: String? = null
) {
    val textColorPrimary = if (isDarkTheme) Color(0xFFF3F4F6) else Color(0xFF1F2937)
    val textColorSecondary = if (isDarkTheme) Color(0xFFD1D5DB) else Color(0xFF374151)
    val textColorTertiary = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val percentTextColor = if (percent != null) textColorPrimary else textColorTertiary

    Column(modifier = Modifier.fillMaxWidth()) {
        // Title row with percent
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Title with fixed width for alignment
            Text(
                text = title,
                modifier = Modifier.width(64.dp),
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColorSecondary
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Progress bar container
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(backgroundColor)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .background(barColor)
                        .clip(RoundedCornerShape(4.dp))
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Percent display with fixed width for alignment - show ?% when null
            val percentText = if (percent != null) {
                stringResource(R.string.percent_format, percent)
            } else {
                "?%"
            }
            Text(
                text = percentText,
                modifier = Modifier.width(56.dp),
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = percentTextColor,
                    textAlign = TextAlign.End
                )
            )
        }

        // Renewal text
        if (renewalText != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = renewalText,
                style = TextStyle(
                    fontSize = 11.sp,
                    color = textColorTertiary
                )
            )
        }
    }
}
