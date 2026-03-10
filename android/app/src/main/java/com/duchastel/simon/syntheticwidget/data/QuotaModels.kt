package com.duchastel.simon.syntheticwidget.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuotaResponse(
    @SerialName("subscription")
    val subscription: QuotaDetail,
    @SerialName("freeToolCalls")
    val freeToolCalls: QuotaDetail,
)

@Serializable
data class QuotaDetail(
    @SerialName("limit")
    val limit: Int,
    @SerialName("requests")
    val requests: Int,
    @SerialName("renewsAt")
    val renewsAt: String? = null
)

@Serializable
data class QuotaWidgetState(
    val subscriptionLimit: Int = 135,
    val subscriptionRequests: Int = 0,
    val toolLimit: Int = 500,
    val toolRequests: Int = 34,
    val subscriptionRenewsAt: String? = null,
    val toolRenewsAt: String? = null,
    val isLoading: Boolean = false
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
