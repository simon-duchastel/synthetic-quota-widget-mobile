package com.duchastel.simon.syntheticwidget.worker

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
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
import com.duchastel.simon.syntheticwidget.data.WidgetDataStore
import com.duchastel.simon.syntheticwidget.widget.QuotaWidget
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class QuotaSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val networkClient: NetworkClient,
    private val widgetDataStore: WidgetDataStore
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // Fetch data from API
            val quotaResponse = networkClient.fetchQuotaData()

            // Save to DataStore
            widgetDataStore.saveFromResponse(quotaResponse)

            // Set loading state to false in widget state
            updateAppWidgetState(applicationContext, QuotaWidget()) { preferences ->
                preferences[WidgetDataStore.IS_LOADING] = false
            }

            // Sync all data to widget state
            val widgetData = com.duchastel.simon.syntheticwidget.data.QuotaWidgetState(
                subscriptionLimit = quotaResponse.subscription.limit,
                subscriptionRequests = quotaResponse.subscription.requests,
                toolLimit = quotaResponse.freeToolCalls.limit,
                toolRequests = quotaResponse.freeToolCalls.requests,
                subscriptionRenewsAt = quotaResponse.subscription.renewsAt,
                toolRenewsAt = quotaResponse.freeToolCalls.renewsAt,
                isLoading = false
            )
            widgetDataStore.saveToWidgetState(applicationContext, widgetData)

            // Trigger widget update
            QuotaWidget().updateAll(applicationContext)

            Result.success()
        } catch (_: Exception) {
            // Set loading state to false even on error
            updateAppWidgetState(applicationContext, QuotaWidget()) { preferences ->
                preferences[WidgetDataStore.IS_LOADING] = false
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
