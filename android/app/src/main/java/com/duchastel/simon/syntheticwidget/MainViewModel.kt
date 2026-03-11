package com.duchastel.simon.syntheticwidget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duchastel.simon.syntheticwidget.data.AuthRepository
import com.duchastel.simon.syntheticwidget.data.QuotaWidgetRepository
import com.duchastel.simon.syntheticwidget.data.QuotaWidgetState
import com.duchastel.simon.syntheticwidget.widget.QuotaWidget
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WidgetInfo(
    val glanceId: String,
    val state: QuotaWidgetState
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val quotaWidgetRepository: QuotaWidgetRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _maskedApiKey = MutableStateFlow("")
    val maskedApiKey: StateFlow<String> = _maskedApiKey.asStateFlow()

    private val _widgets = MutableStateFlow<List<WidgetInfo>>(emptyList())
    val widgets: StateFlow<List<WidgetInfo>> = _widgets.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.getMaskedApiKey().collect { maskedKey ->
                _maskedApiKey.value = maskedKey
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
                WidgetInfo(
                    glanceId = glanceId.toString(),
                    state = state
                )
            }
            _widgets.value = widgetList
        }
    }

    fun saveApiKey(apiKey: String) {
        viewModelScope.launch {
            authRepository.saveApiKey(apiKey)
            authRepository.getMaskedApiKey().collect { maskedKey ->
                _maskedApiKey.value = maskedKey
            }
        }
    }
}
