package com.duchastel.simon.syntheticwidget.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
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
import com.duchastel.simon.syntheticwidget.data.QuotaWidgetState
import com.duchastel.simon.syntheticwidget.data.WidgetRepository.Companion.IS_LOADING
import com.duchastel.simon.syntheticwidget.data.WidgetRepository.Companion.SUB_LIMIT
import com.duchastel.simon.syntheticwidget.data.WidgetRepository.Companion.SUB_RENEWS_AT
import com.duchastel.simon.syntheticwidget.data.WidgetRepository.Companion.SUB_REQUESTS
import com.duchastel.simon.syntheticwidget.data.WidgetRepository.Companion.TOOL_LIMIT
import com.duchastel.simon.syntheticwidget.data.WidgetRepository.Companion.TOOL_RENEWS_AT
import com.duchastel.simon.syntheticwidget.data.WidgetRepository.Companion.TOOL_REQUESTS
import com.duchastel.simon.syntheticwidget.data.WidgetRepository.Companion.WIDGET_ID
import com.duchastel.simon.syntheticwidget.worker.QuotaSyncWorker
import java.util.UUID

class QuotaWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Generate and store widget ID if not already present
        val preferences = getAppWidgetState<androidx.datastore.preferences.core.Preferences>(context, id)
        val currentWidgetId = preferences[WIDGET_ID]
        if (currentWidgetId.isNullOrEmpty()) {
            val newWidgetId = UUID.randomUUID().toString()
            updateAppWidgetState(context, id) { prefs ->
                prefs[WIDGET_ID] = newWidgetId
            }
        }

        provideContent {
            GlanceTheme {
                QuotaWidgetContent(
                    QuotaWidgetState(
                        subscriptionLimit = currentState(SUB_LIMIT) ?: 135,
                        subscriptionRequests = currentState(SUB_REQUESTS) ?: 0,
                        toolLimit = currentState(TOOL_LIMIT) ?: 500,
                        toolRequests = currentState(TOOL_REQUESTS) ?: 34,
                        subscriptionRenewsAt = currentState(SUB_RENEWS_AT),
                        toolRenewsAt = currentState(TOOL_RENEWS_AT),
                        isLoading = currentState(IS_LOADING) ?: false,
                        widgetId = currentState(WIDGET_ID) ?: "",
                    )
                )
            }
        }
    }
}

@Composable
fun QuotaWidgetContent(quotaWidgetState: QuotaWidgetState) {

    // Compute derived values
    val subscriptionProgress = remember(quotaWidgetState.subscriptionRequests, quotaWidgetState.subscriptionLimit) {
        if (quotaWidgetState.subscriptionLimit > 0) {
            quotaWidgetState.subscriptionRequests.toFloat() / quotaWidgetState.subscriptionLimit.toFloat()
        } else {
            0f
        }
    }
    val toolProgress = remember (quotaWidgetState.toolLimit, quotaWidgetState.toolRequests) {
        if (quotaWidgetState.toolLimit > 0) {
            quotaWidgetState.toolRequests.toFloat() / quotaWidgetState.toolLimit.toFloat()
        } else 0f
    }

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
            // Subscription Quota Section (Purple theme)
            QuotaBar(
                title = "Requests",
                used = quotaWidgetState.subscriptionRequests,
                limit = quotaWidgetState.subscriptionLimit,
                progress = subscriptionProgress,
                barColor = Color(0xFF6366F1),
                backgroundColor = Color(0xFFA5B4FC)
            )

            Spacer(modifier = GlanceModifier.height(8.dp))

            // Tools Section (Green theme)
            QuotaBar(
                title = "Tools",
                used = quotaWidgetState.toolRequests,
                limit = quotaWidgetState.toolLimit,
                progress = toolProgress,
                barColor = Color(0xFF10B981),
                backgroundColor = Color(0xFFA7F3D0),
                showRenewal = quotaWidgetState.toolRenewsAt != null,
                renewalText = quotaWidgetState.toolRenewsAt?.let { "Renews in 23 hours and 21 minutes" } ?: ""
            )

            Spacer(modifier = GlanceModifier.height(4.dp))

            // Refresh button
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
}

@Composable
fun QuotaBar(
    title: String,
    used: Int,
    limit: Int,
    progress: Float,
    barColor: Color,
    backgroundColor: Color,
    showRenewal: Boolean = false,
    renewalText: String = ""
) {
    val width = LocalSize.current.width
    val progressBarWidth = remember(width, progress) { width * progress }
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

            // Count display with fixed width for alignment
            Text(
                text = "$used/$limit",
                modifier = GlanceModifier.width(56.dp),
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(
                        day = Color(0xFF1F2937),
                        night = Color(0xFFF3F4F6)
                    )
                )
            )
        }

        // Renewal text
        if (showRenewal && renewalText.isNotEmpty()) {
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

class RefreshAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        // Get the widget ID from the current state
        val preferences = QuotaWidget().getAppWidgetState<androidx.datastore.preferences.core.Preferences>(context, glanceId)
        val widgetId = preferences[WIDGET_ID] ?: ""
        
        // Trigger widget update to show loading state
        updateAppWidgetState(context, glanceId) {
            it.apply {
                this[IS_LOADING] = true
            }
        }
        QuotaWidget().update(context, glanceId)

        // Start the sync worker with the widget ID
        QuotaSyncWorker.runImmediately(context, widgetId)
    }
}

class QuotaWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuotaWidget()
}
