package com.duchastel.simon.syntheticwidget.worker

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
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
import com.duchastel.simon.syntheticwidget.data.WidgetRepository
import com.duchastel.simon.syntheticwidget.data.WidgetRepository.Companion.WIDGET_ID
import com.duchastel.simon.syntheticwidget.widget.QuotaWidget
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class QuotaSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val networkClient: NetworkClient,
    private val widgetRepository: WidgetRepository,
) : CoroutineWorker(applicationContext, params) {

    override suspend fun doWork(): Result {
        val appWidgetManager = GlanceAppWidgetManager(applicationContext)
        val glanceIds = appWidgetManager.getGlanceIds(QuotaWidget::class.java)
        
        // Get the target widget ID from input data (if specified)
        val targetWidgetId = inputData.getString(KEY_WIDGET_ID)
        
        return try {
            // Fetch data from API
            val quotaResponse = networkClient.fetchQuotaData()

            // Update all widgets, but only apply changes if widget ID matches (or no target specified)
            glanceIds.forEach { glanceId ->
                updateAppWidgetState(applicationContext, glanceId) { preferences ->
                    // Only update if no target specified or widget ID matches
                    val shouldUpdate = targetWidgetId.isNullOrEmpty() || 
                        preferences[WIDGET_ID] == targetWidgetId

                    if (shouldUpdate) {
                        preferences[WidgetRepository.IS_LOADING] = false
                        preferences[WidgetRepository.SUB_LIMIT] = quotaResponse.subscription.limit
                        preferences[WidgetRepository.SUB_REQUESTS] = quotaResponse.subscription.requests
                        preferences[WidgetRepository.TOOL_LIMIT] = quotaResponse.freeToolCalls.limit
                        preferences[WidgetRepository.TOOL_REQUESTS] = quotaResponse.freeToolCalls.requests
                        preferences[WidgetRepository.SUB_RENEWS_AT] = quotaResponse.subscription.renewsAt ?: "Never!"
                        preferences[WidgetRepository.TOOL_RENEWS_AT] = quotaResponse.freeToolCalls.renewsAt ?: "Never!"
                    }
                }
            }

            // Trigger widget update
            QuotaWidget().updateAll(applicationContext)

            Result.success()
        } catch (_: Exception) {
            // Set loading state to false even on error
            glanceIds.forEach { id ->
                updateAppWidgetState(applicationContext, id) { preferences ->
                    preferences[WidgetRepository.IS_LOADING] = false
                }
            }

            // Trigger widget update to show error state
            QuotaWidget().updateAll(applicationContext)

            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "quota_sync_worker"
        const val KEY_WIDGET_ID = "widget_id"

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

        fun runImmediately(context: Context, widgetId: String = "") {
            val inputData = workDataOf(KEY_WIDGET_ID to widgetId)
            val syncWorkRequest = OneTimeWorkRequestBuilder<QuotaSyncWorker>()
                .setInputData(inputData)
                .build()
            WorkManager.getInstance(context).enqueue(syncWorkRequest)
        }
    }
}
