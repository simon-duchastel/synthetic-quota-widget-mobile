package com.duchastel.simon.syntheticwidget.widget

import kotlinx.serialization.Serializable

@Serializable
data class QuotaData(
    val subscriptionLimit: Int,
    val subscriptionRequests: Int,
    val toolLimit: Int,
    val toolRequests: Int,
    val subscriptionRenewsAt: String? = null,
    val toolRenewsAt: String? = null,
) {
    val subscriptionRemaining: Int
        get() = subscriptionLimit - subscriptionRequests

    val toolRemaining: Int
        get() = toolLimit - toolRequests

    val subscriptionProgress: Float
        get() = if (subscriptionLimit > 0) subscriptionRequests.toFloat() / subscriptionLimit.toFloat() else 0f

    val toolProgress: Float
        get() = if (toolLimit > 0) toolRequests.toFloat() / toolLimit.toFloat() else 0f
}

@Serializable
data class QuotaWidgetState(
    val quotaData: QuotaData? = null,
    val isLoading: Boolean = false,
    val isClearBackground: Boolean = false,
    val apiKeyId: String? = null,
)
