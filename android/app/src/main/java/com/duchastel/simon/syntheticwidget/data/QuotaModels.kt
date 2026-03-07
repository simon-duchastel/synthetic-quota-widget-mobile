package com.duchastel.simon.syntheticwidget.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuotaResponse(
    @SerialName("subscription")
    val subscription: QuotaDetail,
    @SerialName("free_tool_calls")
    val freeToolCalls: QuotaDetail,
    @SerialName("search")
    val search: SearchQuota? = null
)

@Serializable
data class QuotaDetail(
    @SerialName("limit")
    val limit: Int,
    @SerialName("requests")
    val requests: Int,
    @SerialName("renews_at")
    val renewsAt: String? = null
)

@Serializable
data class SearchQuota(
    @SerialName("limit")
    val limit: Int,
    @SerialName("requests")
    val requests: Int
)

@Serializable
data class QuotaData(
    val subscriptionLimit: Int = 135,
    val subscriptionRequests: Int = 0,
    val toolLimit: Int = 500,
    val toolRequests: Int = 34,
    val subscriptionRenewsAt: String? = null,
    val toolRenewsAt: String? = null
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
