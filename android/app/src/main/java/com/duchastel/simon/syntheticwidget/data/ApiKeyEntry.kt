package com.duchastel.simon.syntheticwidget.data

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class ApiKeyEntry(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val apiKey: String,
) {
    fun getMaskedKey(): String = ApiKeyMasker.mask(apiKey)
}
