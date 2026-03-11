package com.duchastel.simon.syntheticwidget.worker

import android.content.Context
import androidx.glance.ExperimentalGlanceApi
import androidx.glance.appwidget.AdaptersGlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.duchastel.simon.syntheticwidget.data.NetworkClient
import com.duchastel.simon.syntheticwidget.data.WidgetRepository.Companion.IS_LOADING
import com.duchastel.simon.syntheticwidget.data.WidgetRepository.Companion.SUB_LIMIT
import com.duchastel.simon.syntheticwidget.data.WidgetRepository.Companion.SUB_RENEWS_AT
import com.duchastel.simon.syntheticwidget.data.WidgetRepository.Companion.SUB_REQUESTS
import com.duchastel.simon.syntheticwidget.data.WidgetRepository.Companion.TOOL_LIMIT
import com.duchastel.simon.syntheticwidget.data.WidgetRepository.Companion.TOOL_RENEWS_AT
import com.duchastel.simon.syntheticwidget.data.WidgetRepository.Companion.TOOL_REQUESTS
import com.duchastel.simon.syntheticwidget.widget.QuotaWidget
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
@OptIn(ExperimentalGlanceApi::class)
class QuotaSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val networkClient: NetworkClient,
) : CoroutineWorker(applicationContext, params) {

    override suspend fun doWork(): Result {
        val appWidgetManager = GlanceAppWidgetManager(applicationContext)
        val glanceIds = appWidgetManager.getGlanceIds(QuotaWidget::class.java)
        
        // Get the target appWidgetId from input data (if specified)
        // -1 means no specific widget targeted, update all
        val targetAppWidgetId = inputData.getInt(KEY_APP_WIDGET_ID, -1)
        
        return try {
            // Fetch data from API
            val quotaResponse = networkClient.fetchQuotaData()

            // Update widgets - either specific one or all
            if (targetAppWidgetId != -1) {
                // Update specific widget by converting appWidgetId back to GlanceId
                val targetGlanceId = AdaptersGlanceId(targetAppWidgetId)
                updateAppWidgetState(applicationContext, targetGlanceId) { preferences ->
                    preferences[IS_LOADING] = false
                    preferences[SUB_LIMIT] = quotaResponse.subscription.limit
                    preferences[SUB_REQUESTS] = quotaResponse.subscription.requests
                    preferences[TOOL_LIMIT] = quotaResponse.freeToolCalls.limit
                    preferences[TOOL_REQUESTS] = quotaResponse.freeToolCalls.requests
                    preferences[SUB_RENEWS_AT] = quotaResponse.subscription.renewsAt ?: "Never!"
                    preferences[TOOL_RENEWS_AT] = quotaResponse.freeToolCalls.renewsAt ?: "Never!"
                }
            } else {
                // Update all widgets
                glanceIds.forEach { glanceId ->
                    updateAppWidgetState(applicationContext, glanceId) { preferences ->
                        preferences[IS_LOADING] = false
                        preferences[SUB_LIMIT] = quotaResponse.subscription.limit
                        preferences[SUB_REQUESTS] = quotaResponse.subscription.requests
                        preferences[TOOL_LIMIT] = quotaResponse.freeToolCalls.limit
                        preferences[TOOL_REQUESTS] = quotaResponse.freeToolCalls.requests
                        preferences[SUB_RENEWS_AT] = quotaResponse.subscription.renewsAt ?: "Never!"
                        preferences[TOOL_RENEWS_AT] = quotaResponse.freeToolCalls.renewsAt ?: "Never!"
                    }
                }
            }

            // Trigger widget update
            QuotaWidget().updateAll(applicationContext)

            Result.success()
        } catch (_: Exception) {
            // Set loading state to false even on error
            if (targetAppWidgetId != -1) {
                val targetGlanceId = AdaptersGlanceId(targetAppWidgetId)
                updateAppWidgetState(applicationContext, targetGlanceId) { preferences ->
                    preferences[IS_LOADING] = false
                }
            } else {
                glanceIds.forEach { id ->
                    updateAppWidgetState(applicationContext, id) { preferences ->
                        preferences[IS_LOADING] = false
                    }
                }
            }

            // Trigger widget update to show error state
            QuotaWidget().updateAll(applicationContext)

            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "quota_sync_worker"
        const val KEY_APP_WIDGET_ID = "app_widget_id"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncWorkRequest = PeriodicWorkRequestBuilder<QuotaSyncWorker>(1, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncWorkRequest
            )
        }

        fun runImmediately(context: Context, appWidgetId: Int = -1) {
            val inputData = workDataOf(KEY_APP_WIDGET_ID to appWidgetId)
            val syncWorkRequest = OneTimeWorkRequestBuilder<QuotaSyncWorker>()
                .setInputData(inputData)
                .build()
            WorkManager.getInstance(context).enqueue(syncWorkRequest)
        }
    }
}
