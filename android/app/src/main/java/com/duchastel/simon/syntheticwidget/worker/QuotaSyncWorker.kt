package com.duchastel.simon.syntheticwidget.worker

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.Constraints
import androidx.work.OneTimeWorkRequestBuilder
import com.duchastel.simon.syntheticwidget.data.NetworkClient
import com.duchastel.simon.syntheticwidget.data.QuotaDataStore
import com.duchastel.simon.syntheticwidget.widget.QuotaWidget
import java.util.concurrent.TimeUnit

class QuotaSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // Fetch data from API
            val quotaResponse = NetworkClient.fetchQuotaData(applicationContext)

            // Save to DataStore
            QuotaDataStore.saveFromResponse(applicationContext, quotaResponse)

            // Trigger widget update for all instances
            val manager = GlanceAppWidgetManager(applicationContext)
            val widget = QuotaWidget()
            val glanceIds = manager.getGlanceIds(QuotaWidget::class.java)
            
            // Update each widget instance individually to ensure fresh data is read
            glanceIds.forEach { glanceId ->
                widget.update(applicationContext, glanceId)
            }
            
            Result.success()
        } catch (_: Exception) {
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
