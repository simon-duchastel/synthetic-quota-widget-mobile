package com.duchastel.simon.syntheticwidget.worker

import android.content.Context
import androidx.glance.ExperimentalGlanceApi
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
) : CoroutineWorker(context, params) {
    private val appWidgetManager by lazy { GlanceAppWidgetManager(applicationContext) }

    override suspend fun doWork(): Result {
        // Get the target appWidgetId from input data
        val targetAppWidgetId = inputData.getInt(KEY_APP_WIDGET_ID, -1)
        if (targetAppWidgetId == -1) return Result.failure()
        
        return try {
            // Fetch data from API
            val quotaResponse = networkClient.fetchQuotaData()

            val targetGlanceId = appWidgetManager.getGlanceIdBy(targetAppWidgetId)
            updateAppWidgetState(applicationContext, targetGlanceId) { preferences ->
                preferences[IS_LOADING] = false
                preferences[SUB_LIMIT] = quotaResponse.subscription.limit
                preferences[SUB_REQUESTS] = quotaResponse.subscription.requests
                preferences[TOOL_LIMIT] = quotaResponse.freeToolCalls.limit
                preferences[TOOL_REQUESTS] = quotaResponse.freeToolCalls.requests
                preferences[SUB_RENEWS_AT] = quotaResponse.subscription.renewsAt ?: "Never!"
                preferences[TOOL_RENEWS_AT] = quotaResponse.freeToolCalls.renewsAt ?: "Never!"
            }

            // Trigger widget update
            QuotaWidget().update(applicationContext, targetGlanceId)

            Result.success()
        } catch (_: Exception) {
            // Set loading state to false
            val targetGlanceId = appWidgetManager.getGlanceIdBy(targetAppWidgetId)
            updateAppWidgetState(applicationContext, targetGlanceId) { preferences ->
                preferences[IS_LOADING] = false
            }

            // Trigger widget update to show error state
            QuotaWidget().update(applicationContext, targetGlanceId)

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
