package com.duchastel.simon.syntheticwidget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duchastel.simon.syntheticwidget.data.ApiKeyEntry
import com.duchastel.simon.syntheticwidget.data.AuthRepository
import com.duchastel.simon.syntheticwidget.data.QuotaWidgetRepository
import com.duchastel.simon.syntheticwidget.widget.QuotaWidget
import com.duchastel.simon.syntheticwidget.widget.QuotaWidgetState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WidgetInfo(
    val glanceId: GlanceId,
    val appWidgetId: Int,
    val state: QuotaWidgetState
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val quotaWidgetRepository: QuotaWidgetRepository,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val _apiKeys = MutableStateFlow<List<ApiKeyEntry>>(emptyList())
    val apiKeys: StateFlow<List<ApiKeyEntry>> = _apiKeys.asStateFlow()

    private val _widgets = MutableStateFlow<List<WidgetInfo>>(emptyList())
    val widgets: StateFlow<List<WidgetInfo>> = _widgets.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.getApiKeysFlow().collect { keys ->
                _apiKeys.value = keys
            }
        }
        loadWidgets()
    }

    fun loadWidgets() {
        viewModelScope.launch {
            val glanceManager = GlanceAppWidgetManager(context)
            val glanceIds = glanceManager.getGlanceIds(QuotaWidget::class.java)
            val widgetList = glanceIds.map { glanceId ->
                val state = quotaWidgetRepository.getWidgetState(glanceId)
                val appWidgetId = glanceManager.getAppWidgetId(glanceId)
                WidgetInfo(
                    glanceId = glanceId,
                    appWidgetId = appWidgetId,
                    state = state
                )
            }
            _widgets.value = widgetList
        }
    }

    fun refreshWidget(glanceId: GlanceId) {
        viewModelScope.launch {
            quotaWidgetRepository.refreshData(glanceId)
            loadWidgets()
        }
    }

    fun addApiKey(name: String, apiKey: String) {
        viewModelScope.launch {
            val entry = ApiKeyEntry(name = name, apiKey = apiKey)
            authRepository.saveApiKey(entry)
        }
    }

    fun deleteApiKey(id: String) {
        viewModelScope.launch {
            authRepository.deleteApiKey(id)
        }
    }

    fun setWidgetApiKey(glanceId: GlanceId, apiKeyId: String?) {
        viewModelScope.launch {
            quotaWidgetRepository.setWidgetApiKeyId(glanceId, apiKeyId)
            loadWidgets()
        }
    }

    fun setClearBackground(glanceId: GlanceId, isClearBackground: Boolean) {
        viewModelScope.launch {
            quotaWidgetRepository.setClearBackground(glanceId, isClearBackground)
            loadWidgets()
        }
    }

    fun getApiKeyForWidget(widgetInfo: WidgetInfo): ApiKeyEntry? {
        val apiKeyId = widgetInfo.state.apiKeyId ?: return null
        return apiKeys.value.find { it.id == apiKeyId }
    }
}
