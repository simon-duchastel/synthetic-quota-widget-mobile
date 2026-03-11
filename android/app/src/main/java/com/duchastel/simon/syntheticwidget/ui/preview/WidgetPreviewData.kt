package com.duchastel.simon.syntheticwidget.ui.preview

import com.duchastel.simon.syntheticwidget.widget.QuotaData
import com.duchastel.simon.syntheticwidget.widget.QuotaWidgetState

object WidgetPreviewData {

    val FAKE_QUOTA_DATA_NORMAL = QuotaData(
        subscriptionLimit = 1000,
        subscriptionRequests = 350,
        toolLimit = 100,
        toolRequests = 25,
        subscriptionRenewsAt = "2026-04-01T00:00:00Z",
        toolRenewsAt = "2026-04-01T00:00:00Z"
    )

    val FAKE_QUOTA_DATA_HIGH_USAGE = QuotaData(
        subscriptionLimit = 1000,
        subscriptionRequests = 850,
        toolLimit = 100,
        toolRequests = 78,
        subscriptionRenewsAt = "2026-03-15T00:00:00Z",
        toolRenewsAt = "2026-03-15T00:00:00Z"
    )

    val FAKE_QUOTA_DATA_LOW_USAGE = QuotaData(
        subscriptionLimit = 1000,
        subscriptionRequests = 45,
        toolLimit = 100,
        toolRequests = 3,
        subscriptionRenewsAt = "2026-04-10T00:00:00Z",
        toolRenewsAt = "2026-04-10T00:00:00Z"
    )

    val FAKE_QUOTA_DATA_NEAR_LIMIT = QuotaData(
        subscriptionLimit = 1000,
        subscriptionRequests = 980,
        toolLimit = 100,
        toolRequests = 95,
        subscriptionRenewsAt = "2026-03-12T00:00:00Z",
        toolRenewsAt = "2026-03-12T00:00:00Z"
    )

    val FAKE_WIDGET_STATE_NORMAL = QuotaWidgetState(
        quotaData = FAKE_QUOTA_DATA_NORMAL,
        isLoading = false
    )

    val FAKE_WIDGET_STATE_HIGH_USAGE = QuotaWidgetState(
        quotaData = FAKE_QUOTA_DATA_HIGH_USAGE,
        isLoading = false
    )

    val FAKE_WIDGET_STATE_LOW_USAGE = QuotaWidgetState(
        quotaData = FAKE_QUOTA_DATA_LOW_USAGE,
        isLoading = false
    )

    val FAKE_WIDGET_STATE_NEAR_LIMIT = QuotaWidgetState(
        quotaData = FAKE_QUOTA_DATA_NEAR_LIMIT,
        isLoading = false
    )

    val FAKE_WIDGET_STATE_LOADING = QuotaWidgetState(
        quotaData = FAKE_QUOTA_DATA_NORMAL,
        isLoading = true
    )

    val FAKE_WIDGET_STATE_UNINITIALIZED = QuotaWidgetState(
        quotaData = null,
        isLoading = false
    )

    val allPreviewStates = listOf(
        FAKE_WIDGET_STATE_NORMAL,
        FAKE_WIDGET_STATE_HIGH_USAGE,
        FAKE_WIDGET_STATE_LOW_USAGE,
        FAKE_WIDGET_STATE_NEAR_LIMIT
    )
}
