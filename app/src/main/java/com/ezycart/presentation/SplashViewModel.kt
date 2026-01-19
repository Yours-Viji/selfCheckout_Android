package com.ezycart.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ezycart.MainActivity
import com.ezycart.data.datastore.PreferencesManager
import com.ezycart.domain.usecase.GetAuthDataUseCase
import com.ezycart.presentation.common.data.Constants
import com.ezycart.presentation.common.data.DeviceUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val getAuthDataUseCase: GetAuthDataUseCase,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    val isDeviceActivated: StateFlow<Boolean?> = getAuthDataUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun getDeviceId(mainActivity: MainActivity) {
        val deviceId = DeviceUtils.getDeviceId(mainActivity)
        Constants.deviceId = deviceId
    }

    fun clearUserPreference(){
        CoroutineScope(Dispatchers.IO).launch {
            preferencesManager.clearEmployeeDetails()
        }
    }
}