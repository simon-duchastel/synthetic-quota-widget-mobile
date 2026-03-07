package com.duchastel.simon.syntheticwidget

import android.app.Application
import com.duchastel.simon.syntheticwidget.worker.QuotaSyncWorker

class SyntheticWidgetApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()

        QuotaSyncWorker.schedule(this)
    }
}
