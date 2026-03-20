package com.duchastel.simon.syntheticwidget.data
 
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.duchastel.simon.syntheticwidget.widget.QuotaWidgetState
import dagger.hilt.android.qualifiers.ApplicationContext
import okio.IOException
import javax.inject.Inject

private val USE_DARK_MODE = booleanPreferencesKey("use_dark_mode")

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
