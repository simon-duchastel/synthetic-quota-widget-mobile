package com.duchastel.simon.syntheticwidget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duchastel.simon.syntheticwidget.data.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _maskedApiKey = MutableStateFlow("")
    val maskedApiKey: StateFlow<String> = _maskedApiKey.asStateFlow()
    
    init {
        viewModelScope.launch {
            authRepository.getMaskedApiKey().collect { maskedKey ->
                _maskedApiKey.value = maskedKey
            }
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
