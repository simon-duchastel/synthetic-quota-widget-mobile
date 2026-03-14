package com.duchastel.simon.syntheticwidget.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.ColorFilter
import androidx.glance.ExperimentalGlanceApi
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.duchastel.simon.syntheticwidget.R
import com.duchastel.simon.syntheticwidget.data.toQuotaWidgetState
import com.duchastel.simon.syntheticwidget.widget.WidgetPreviewData
import com.duchastel.simon.syntheticwidget.utils.formatRenewalTime
import com.duchastel.simon.syntheticwidget.worker.QuotaSyncWorker

class QuotaWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                val quotaWidgetState = currentState<Preferences>().toQuotaWidgetState()
                QuotaWidgetContent(quotaWidgetState)
            }
        }
    }

    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        provideContent {
            GlanceTheme {
                QuotaWidgetContent(WidgetPreviewData.FAKE_QUOTA_DATA_NORMAL)
            }
        }
    }
}

@Composable
fun QuotaWidgetContent(quotaWidgetState: QuotaWidgetState) {
    val quotaData = quotaWidgetState.quotaData
    val isInitialized = quotaData != null

    // Compute derived values - use 0 if not initialized
    val subscriptionProgress = if (isInitialized) {
        quotaData.subscriptionProgress
    } else 0f

    val toolProgress = if (isInitialized) {
        quotaData.toolProgress
    } else 0f

    // Grey colors for uninitialized state
    val greyBarColor = Color(0xFF9CA3AF)
    val greyBackgroundColor = Color(0xFFE5E7EB)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(
                day = Color(0xFFF5F5F5),
                night = Color(0xFF1A1A1A)
            ))
            .padding(12.dp)
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            horizontalAlignment = Alignment.Horizontal.Start
        ) {
            // Subscription Quota Section (Purple theme, grey if uninitialized)
            QuotaBar(
                title = "Requests",
                used = if (isInitialized) quotaData.subscriptionRequests else null,
                limit = if (isInitialized) quotaData.subscriptionLimit else null,
                progress = subscriptionProgress,
                barColor = if (isInitialized) Color(0xFF6366F1) else greyBarColor,
                backgroundColor = if (isInitialized) Color(0xFFA5B4FC) else greyBackgroundColor,
                renewalText = if (isInitialized) {
                    remember(quotaWidgetState.quotaData.subscriptionRenewsAt) {
                        formatRenewalTime(quotaWidgetState.quotaData.subscriptionRenewsAt)
                    }
                } else {
                    ""
                }
            )

            Spacer(modifier = GlanceModifier.height(8.dp))

            // Tools Section (Green theme, grey if uninitialized)
            QuotaBar(
                title = "Tools",
                used = if (isInitialized) quotaData.toolRequests else null,
                limit = if (isInitialized) quotaData.toolLimit else null,
                progress = toolProgress,
                barColor = if (isInitialized) Color(0xFF10B981) else greyBarColor,
                backgroundColor = if (isInitialized) Color(0xFFA7F3D0) else greyBackgroundColor,
                renewalText = if (isInitialized) {
                    remember(quotaWidgetState.quotaData.toolRenewsAt) {
                        formatRenewalTime(quotaWidgetState.quotaData.toolRenewsAt)
                    }
                } else {
                    ""
                }
            )

            Spacer(modifier = GlanceModifier.height(4.dp))

            // Refresh button - only show when initialized
            if (isInitialized) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Horizontal.End
                ) {
                    if (quotaWidgetState.isLoading) {
                        // Show loading text
                        Text(
                            text = "Loading...",
                            modifier = GlanceModifier.width(56.dp),
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                color = ColorProvider(
                                    day = Color(0xFF1F2937),
                                    night = Color(0xFFF3F4F6)
                                )
                            )
                        )
                    } else {
                        // Show refresh button
                        Image(
                            provider = ImageProvider(R.drawable.ic_refresh),
                            contentDescription = "Refresh",
                            modifier = GlanceModifier
                                .size(24.dp)
                                .clickable(actionRunCallback<RefreshAction>()),
                            colorFilter = ColorFilter.tint(
                                ColorProvider(
                                    day = Color(0xFF6B7280),
                                    night = Color(0xFF9CA3AF)
                                )
                            )
                        )
                    }
                }
            }
        }

        // Loading overlay button - show when not initialized (centered over the bars)
        if (!isInitialized) {
            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_refresh),
                    contentDescription = "Load data",
                    modifier = GlanceModifier
                        .size(48.dp)
                        .clickable(actionRunCallback<RefreshAction>()),
                    colorFilter = ColorFilter.tint(
                        ColorProvider(
                            day = Color(0xFF6B7280),
                            night = Color(0xFF9CA3AF)
                        )
                    )
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
    renewalText: String? = null
) {
    val width = LocalSize.current.width
    val progressBarWidth = remember(width, progress) { width * progress }
    // Use grey text color when data is not available
    val textColor = if (used != null && limit != null) {
        ColorProvider(
            day = Color(0xFF1F2937),
            night = Color(0xFFF3F4F6)
        )
    } else {
        ColorProvider(
            day = Color(0xFF9CA3AF),
            night = Color(0xFF6B7280)
        )
    }

    Column(modifier = GlanceModifier.fillMaxWidth()) {
        // Title row with count
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Title with fixed width for alignment
            Text(
                text = title,
                modifier = GlanceModifier.width(64.dp),
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = ColorProvider(
                        day = Color(0xFF374151),
                        night = Color(0xFFD1D5DB)
                    )
                )
            )

            Spacer(modifier = GlanceModifier.width(8.dp))

            // Progress bar container
            Box(
                modifier = GlanceModifier
                    .defaultWeight()
                    .height(8.dp)
                    .cornerRadius(4.dp)
                    .background(backgroundColor)
            ) {
                Box(
                    modifier = GlanceModifier
                        .fillMaxHeight()
                        .width(progressBarWidth)
                        .background(barColor)
                        .cornerRadius(4.dp)
                ) {}
            }

            Spacer(modifier = GlanceModifier.width(8.dp))

            // Count display with fixed width for alignment - show ?/? when null
            val countText = if (used != null && limit != null) "$used/$limit" else "?/?"
            Text(
                text = countText,
                modifier = GlanceModifier.width(56.dp),
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            )
        }

        // Renewal text
        if (renewalText != null) {
            Spacer(modifier = GlanceModifier.height(2.dp))
            Text(
                text = renewalText,
                style = TextStyle(
                    fontSize = 11.sp,
                    color = ColorProvider(
                        day = Color(0xFF6B7280),
                        night = Color(0xFF9CA3AF)
                    )
                )
            )
        }
    }
}

@OptIn(ExperimentalGlanceApi::class)
class RefreshAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val appWidgetManager = GlanceAppWidgetManager(context)
        val appWidgetId = appWidgetManager.getAppWidgetId(glanceId)
        QuotaSyncWorker.runImmediately(context, appWidgetId)
    }
}

class QuotaWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuotaWidget()
}
