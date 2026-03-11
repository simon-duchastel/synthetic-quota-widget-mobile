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
