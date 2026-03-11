package com.duchastel.simon.syntheticwidget.ui.widget

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duchastel.simon.syntheticwidget.R
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
    val quotaData = quotaWidgetState.quotaData
    val isInitialized = quotaData != null

    // Compute derived values - use 0 if not initialized
    val subscriptionProgress = if (isInitialized) {
        quotaData.subscriptionProgress
    } else 0f

    val toolProgress = if (isInitialized) {
        quotaData.toolProgress
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
            .fillMaxSize()
            .background(backgroundColor)
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.Start
        ) {
            // Subscription Quota Section (Purple theme, grey if uninitialized)
            QuotaBar(
                title = "Requests",
                used = if (isInitialized) quotaData.subscriptionRequests else null,
                limit = if (isInitialized) quotaData.subscriptionLimit else null,
                progress = subscriptionProgress,
                barColor = if (isInitialized) Color(0xFF6366F1) else greyBarColor,
                backgroundColor = if (isInitialized) Color(0xFFA5B4FC) else greyBackgroundColor,
                isDarkTheme = isDarkTheme,
                showRenewal = isInitialized && quotaData.subscriptionRenewsAt != null,
                renewalText = if (isInitialized) quotaData.subscriptionRenewsAt?.let { "Renews at $it" } ?: "" else ""
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tools Section (Green theme, grey if uninitialized)
            QuotaBar(
                title = "Tools",
                used = if (isInitialized) quotaData.toolRequests else null,
                limit = if (isInitialized) quotaData.toolLimit else null,
                progress = toolProgress,
                barColor = if (isInitialized) Color(0xFF10B981) else greyBarColor,
                backgroundColor = if (isInitialized) Color(0xFFA7F3D0) else greyBackgroundColor,
                isDarkTheme = isDarkTheme,
                showRenewal = isInitialized && quotaData.toolRenewsAt != null,
                renewalText = if (isInitialized) quotaData.toolRenewsAt?.let { "Renews at $it" } ?: "" else ""
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
                            text = "Loading...",
                            modifier = Modifier.width(56.dp),
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                color = textColorPrimary
                            )
                        )
                    } else {
                        // Show refresh button
                        Icon(
                            painter = painterResource(id = R.drawable.ic_refresh),
                            contentDescription = "Refresh",
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { onRefreshClick() },
                            tint = textColorTertiary
                        )
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
                    contentDescription = "Load data",
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
    used: Int?,
    limit: Int?,
    progress: Float,
    barColor: Color,
    backgroundColor: Color,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    showRenewal: Boolean = false,
    renewalText: String = ""
) {
    val textColorPrimary = if (isDarkTheme) Color(0xFFF3F4F6) else Color(0xFF1F2937)
    val textColorSecondary = if (isDarkTheme) Color(0xFFD1D5DB) else Color(0xFF374151)
    val textColorTertiary = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    // Use grey text color when data is not available
    val countTextColor = if (used != null && limit != null) textColorPrimary else textColorTertiary

    Column(modifier = Modifier.fillMaxWidth()) {
        // Title row with count
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

            // Count display with fixed width for alignment - show ?/? when null
            val countText = if (used != null && limit != null) "$used/$limit" else "?/?"
            Text(
                text = countText,
                modifier = Modifier.width(56.dp),
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = countTextColor
                )
            )
        }

        // Renewal text
        if (showRenewal && renewalText.isNotEmpty()) {
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
