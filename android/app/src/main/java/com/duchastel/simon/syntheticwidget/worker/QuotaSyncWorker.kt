package com.duchastel.simon.syntheticwidget.worker

import android.content.Context
import androidx.glance.ExperimentalGlanceApi
import androidx.glance.appwidget.GlanceAppWidgetManager
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
import com.duchastel.simon.syntheticwidget.data.QuotaWidgetRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
@OptIn(ExperimentalGlanceApi::class)
class QuotaSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val quotaWidgetRepository: QuotaWidgetRepository,
) : CoroutineWorker(context, params) {
    private val appWidgetManager by lazy { GlanceAppWidgetManager(applicationContext) }

    override suspend fun doWork(): Result {
        // Get the target appWidgetId from input data
        val targetAppWidgetId = inputData.getInt(KEY_APP_WIDGET_ID, -1)
        if (targetAppWidgetId == -1) return Result.failure()

        val targetGlanceId = appWidgetManager.getGlanceIdBy(targetAppWidgetId)
        val result = quotaWidgetRepository.refreshData(targetGlanceId)
       return if (result) {
            Result.success()
        } else {
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
