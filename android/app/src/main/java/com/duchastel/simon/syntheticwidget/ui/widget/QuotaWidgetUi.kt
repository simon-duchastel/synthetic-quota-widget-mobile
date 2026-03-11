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
import com.duchastel.simon.syntheticwidget.data.QuotaWidgetState
import com.duchastel.simon.syntheticwidget.utils.formatRenewalTime

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
    // Compute derived values
    val subscriptionProgress = remember(quotaWidgetState.subscriptionRequests, quotaWidgetState.subscriptionLimit) {
        if (quotaWidgetState.subscriptionLimit > 0) {
            quotaWidgetState.subscriptionRequests.toFloat() / quotaWidgetState.subscriptionLimit.toFloat()
        } else {
            0f
        }
    }
    val toolProgress = remember(quotaWidgetState.toolLimit, quotaWidgetState.toolRequests) {
        if (quotaWidgetState.toolLimit > 0) {
            quotaWidgetState.toolRequests.toFloat() / quotaWidgetState.toolLimit.toFloat()
        } else 0f
    }

    val backgroundColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color(0xFFF5F5F5)
    val textColorPrimary = if (isDarkTheme) Color(0xFFF3F4F6) else Color(0xFF1F2937)
    val textColorSecondary = if (isDarkTheme) Color(0xFFD1D5DB) else Color(0xFF374151)
    val textColorTertiary = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)

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
            // Subscription Quota Section (Purple theme)
            QuotaBar(
                title = "Requests",
                used = quotaWidgetState.subscriptionRequests,
                limit = quotaWidgetState.subscriptionLimit,
                progress = subscriptionProgress,
                barColor = Color(0xFF6366F1),
                backgroundColor = Color(0xFFA5B4FC),
                isDarkTheme = isDarkTheme,
                renewalText = remember(quotaWidgetState.subscriptionRenewsAt) {
                    formatRenewalTime(quotaWidgetState.subscriptionRenewsAt)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tools Section (Green theme)
            QuotaBar(
                title = "Tools",
                used = quotaWidgetState.toolRequests,
                limit = quotaWidgetState.toolLimit,
                progress = toolProgress,
                barColor = Color(0xFF10B981),
                backgroundColor = Color(0xFFA7F3D0),
                isDarkTheme = isDarkTheme,
                renewalText = remember(quotaWidgetState.toolRenewsAt) {
                    formatRenewalTime(quotaWidgetState.toolRenewsAt)
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Refresh button
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
}

@Composable
fun QuotaBar(
    title: String,
    used: Int,
    limit: Int,
    progress: Float,
    barColor: Color,
    backgroundColor: Color,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    renewalText: String? = null
) {
    val textColorPrimary = if (isDarkTheme) Color(0xFFF3F4F6) else Color(0xFF1F2937)
    val textColorSecondary = if (isDarkTheme) Color(0xFFD1D5DB) else Color(0xFF374151)
    val textColorTertiary = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)

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

            // Count display with fixed width for alignment
            Text(
                text = "$used/$limit",
                modifier = Modifier.width(56.dp),
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColorPrimary
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
