package com.duchastel.simon.syntheticwidget.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
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
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.duchastel.simon.syntheticwidget.R
import com.duchastel.simon.syntheticwidget.data.WidgetDataStore
import com.duchastel.simon.syntheticwidget.data.WidgetDataStore.Companion.IS_LOADING
import com.duchastel.simon.syntheticwidget.data.WidgetDataStore.Companion.SUB_LIMIT
import com.duchastel.simon.syntheticwidget.data.WidgetDataStore.Companion.SUB_RENEWS_AT
import com.duchastel.simon.syntheticwidget.data.WidgetDataStore.Companion.SUB_REQUESTS
import com.duchastel.simon.syntheticwidget.data.WidgetDataStore.Companion.TOOL_LIMIT
import com.duchastel.simon.syntheticwidget.data.WidgetDataStore.Companion.TOOL_RENEWS_AT
import com.duchastel.simon.syntheticwidget.data.WidgetDataStore.Companion.TOOL_REQUESTS
import com.duchastel.simon.syntheticwidget.data.getWidgetDataStore
import com.duchastel.simon.syntheticwidget.worker.QuotaSyncWorker
import java.io.File

class QuotaWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                QuotaWidgetContent()
            }
        }
    }

    override val stateDefinition: GlanceStateDefinition<Preferences>
        get() = object : GlanceStateDefinition<Preferences> {
            override suspend fun getDataStore(
                context: Context,
                fileKey: String
            ): androidx.datastore.core.DataStore<Preferences> {
                return getWidgetDataStore(context).dataStore
            }

            override fun getLocation(context: Context, fileKey: String): File {
                return context.preferencesDataStoreFile("quota_preferences")
            }
        }
}

@Composable
fun QuotaWidgetContent() {
    // Read individual preference values using currentState with keys
    val subscriptionLimit = currentState(SUB_LIMIT) ?: 135
    val subscriptionRequests = currentState(SUB_REQUESTS) ?: 0
    val toolLimit = currentState(TOOL_LIMIT) ?: 500
    val toolRequests = currentState(TOOL_REQUESTS) ?: 34
    val subscriptionRenewsAt = currentState(SUB_RENEWS_AT)
    val toolRenewsAt = currentState(TOOL_RENEWS_AT)
    val isLoading = currentState(IS_LOADING) ?: false

    // Compute derived values
    val subscriptionProgress = if (subscriptionLimit > 0) subscriptionRequests.toFloat() / subscriptionLimit.toFloat() else 0f
    val toolProgress = if (toolLimit > 0) toolRequests.toFloat() / toolLimit.toFloat() else 0f

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
                used = subscriptionRequests,
                limit = subscriptionLimit,
                progress = subscriptionProgress,
                barColor = Color(0xFF6366F1),
                backgroundColor = Color(0xFFA5B4FC)
            )

            Spacer(modifier = GlanceModifier.height(8.dp))

            // Tools Section (Green theme)
            QuotaBar(
                title = "Tools",
                used = toolRequests,
                limit = toolLimit,
                progress = toolProgress,
                barColor = Color(0xFF10B981),
                backgroundColor = Color(0xFFA7F3D0),
                showRenewal = toolRenewsAt != null,
                renewalText = toolRenewsAt?.let { "Renews in 23 hours and 21 minutes" } ?: ""
            )

            Spacer(modifier = GlanceModifier.height(4.dp))

            // Refresh button
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Horizontal.End
            ) {
                if (isLoading) {
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
        val widgetDataStore = getWidgetDataStore(context)

        // Set loading state to true using updateAppWidgetState
        widgetDataStore.setLoadingInWidget(context, true)

        // Trigger widget update to show loading state
        QuotaWidget().updateAll(context)

        // Start the sync worker
        QuotaSyncWorker.runImmediately(context)
    }
}

class QuotaWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuotaWidget()
}
