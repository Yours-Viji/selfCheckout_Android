package com.ezycart.presentation.landing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class LandingViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LandingUiState())
    val uiState: StateFlow<LandingUiState> = _uiState.asStateFlow()

    init {
        startAutoScroll()
    }

    private fun startAutoScroll() {
        viewModelScope.launch {
            while (isActive) {
                delay(5000) // Scroll every 5 seconds
                val nextIndex = (_uiState.value.currentBannerIndex + 1) % _uiState.value.banners.size
                _uiState.value = _uiState.value.copy(currentBannerIndex = nextIndex)
            }
        }
    }

    fun onStartClicked() {
        _uiState.value = _uiState.value.copy(isStarted = true)
    }
}