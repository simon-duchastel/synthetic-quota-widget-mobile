package com.duchastel.simon.syntheticwidget

import android.app.Application
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo.WIDGET_CATEGORY_HOME_SCREEN
import android.content.ComponentName
import android.content.Context
import android.content.Context.APPWIDGET_SERVICE
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.duchastel.simon.syntheticwidget.worker.QuotaSyncWorker
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltAndroidApp
class SyntheticWidgetApplication : Application(), Configuration.Provider {
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    
    override fun onCreate() {
        super.onCreate()

        QuotaSyncWorker.schedule(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            MainScope().launch {
                registerWidgetPreviews(applicationContext)
            }
        }
    }
}

val APP_WIDGET_RECEIVER_CLASSES = listOf(GlanceAppWidgetReceiver::class.java)

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
context(@ApplicationContext applicationContext: Context)
fun Class<GlanceAppWidgetReceiver>.hasPreviewForCategory(widgetCategory: Int): Boolean {
    val component = ComponentName(applicationContext, this)
    val providerInfo =
        (applicationContext.getSystemService(APPWIDGET_SERVICE) as AppWidgetManager)
            .installedProviders
            .first { providerInfo -> providerInfo.provider == component }
    return providerInfo.generatedPreviewCategories.and(widgetCategory) != 0
}

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
suspend fun registerWidgetPreviews(@ApplicationContext applicationContext: Context) {
    val glanceAppWidgetManager = GlanceAppWidgetManager(applicationContext)
    withContext(Dispatchers.Default) {
        for (receiver in APP_WIDGET_RECEIVER_CLASSES) {
            with(applicationContext) {
                if (receiver.hasPreviewForCategory(WIDGET_CATEGORY_HOME_SCREEN)) {
                    continue
                }
                glanceAppWidgetManager.setWidgetPreviews(receiver.kotlin)
            }
        }
    }
}
