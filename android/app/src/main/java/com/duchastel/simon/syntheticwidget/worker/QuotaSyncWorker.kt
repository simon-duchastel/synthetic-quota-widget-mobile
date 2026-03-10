package com.duchastel.simon.syntheticwidget.worker

import android.content.Context
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
class QuotaSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val networkClient: NetworkClient,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val appWidgetManager = GlanceAppWidgetManager(applicationContext)
        val widgetIds = appWidgetManager.getGlanceIds(QuotaWidget::class.java)
        return try {
            // Fetch data from API
            val quotaResponse = networkClient.fetchQuotaData()

            // Set loading state to false in widget state
            widgetIds.forEach { id ->
                updateAppWidgetState(applicationContext, id) { preferences ->
                    preferences[IS_LOADING] = false
                    preferences[SUB_LIMIT] = quotaResponse.subscription.limit
                    preferences[SUB_REQUESTS] = quotaResponse.subscription.requests
                    preferences[TOOL_LIMIT] = quotaResponse.freeToolCalls.limit
                    preferences[TOOL_REQUESTS] = quotaResponse.freeToolCalls.requests
                    preferences[SUB_RENEWS_AT] = quotaResponse.subscription.renewsAt ?: "Never!"
                    preferences[TOOL_RENEWS_AT] = quotaResponse.freeToolCalls.renewsAt ?: "Never!"
                    preferences[IS_LOADING] = false
                }
            }

            // Trigger widget update
            QuotaWidget().updateAll(applicationContext)

            Result.success()
        } catch (_: Exception) {
            // Set loading state to false even on error
            widgetIds.forEach { id ->
                updateAppWidgetState(applicationContext,  id) { preferences ->
                    preferences[IS_LOADING] = false
                }
            }

            // Trigger widget update to show error state
            QuotaWidget().updateAll(applicationContext)

            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "quota_sync_worker"

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

        fun runImmediately(context: Context) {
            val syncWorkRequest = OneTimeWorkRequestBuilder<QuotaSyncWorker>().build()
            WorkManager.getInstance(context).enqueue(syncWorkRequest)
        }
    }
}
