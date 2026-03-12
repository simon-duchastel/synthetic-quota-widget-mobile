package com.duchastel.simon.syntheticwidget.widget

object WidgetPreviewData {
    val FAKE_QUOTA_DATA_NORMAL = QuotaWidgetState(
        quotaData = QuotaData(
            subscriptionLimit = 1000,
            subscriptionRequests = 350,
            toolLimit = 100,
            toolRequests = 25,
            subscriptionRenewsAt = "2026-04-01T00:00:00Z",
            toolRenewsAt = "2026-04-01T00:00:00Z"
        ),
        isLoading = false,
    )
}