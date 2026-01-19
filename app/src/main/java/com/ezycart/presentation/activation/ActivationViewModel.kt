package com.ezycart.presentation.activation


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ezycart.BuildConfig
import com.ezycart.data.datastore.PreferencesManager
import com.ezycart.data.remote.dto.DeviceDetailsResponse
import com.ezycart.data.remote.dto.NetworkResponse
import com.ezycart.domain.model.AppMode
import com.ezycart.domain.usecase.GetAuthDataUseCase
import com.ezycart.domain.usecase.LoadingManager
import com.ezycart.domain.usecase.LoginUseCase
import com.ezycart.presentation.common.data.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivationViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val getAuthDataUseCase: GetAuthDataUseCase,
    private val preferencesManager: PreferencesManager,
    private val loadingManager: LoadingManager
) : ViewModel() {

    private val _stateFlow = MutableStateFlow(ActivationState())
    val stateFlow: StateFlow<ActivationState> = _stateFlow.asStateFlow()
    val isDeviceActivated: StateFlow<Boolean?> = getAuthDataUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    init {
        viewModelScope.launch {
            val savedAppMode = preferencesManager.getAppMode()
            _stateFlow.update {
                it.copy(
                    activationCode = BuildConfig.ACTIVATION_CODE,
                    trolleyNumber = "01",
                    appMode = savedAppMode
                )
            }
        }
    }


    val authState = getAuthDataUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    fun onActivationCodeChange(activationCode: String) {
        _stateFlow.value = _stateFlow.value.copy(activationCode = activationCode)
    }

    fun onTrolleyNumberChange(trolleyNumber: String) {
        _stateFlow.value = _stateFlow.value.copy(trolleyNumber = trolleyNumber)
    }
    fun onAppModeChange(appMode: AppMode) {
        _stateFlow.value = _stateFlow.value.copy(appMode = appMode)
    }
    fun onErrorShown() {
        _stateFlow.value = _stateFlow.value.copy(error = null)
    }

    fun activateDevice() {
        loadingManager.show()
        viewModelScope.launch {
            _stateFlow.value = _stateFlow.value.copy(isLoading = true, error = null)

            when (val result = loginUseCase.activate(
                _stateFlow.value.activationCode,
                _stateFlow.value.trolleyNumber
            )) {
                is NetworkResponse.Success -> {
                    _stateFlow.value = _stateFlow.value.copy(
                        isLoading = false,
                        isActivationSuccessful = true
                    )
                    saveActivationDetails("${result.data.id}", "${result.data.merchantId}")
                    loadingManager.hide()
                }
                is NetworkResponse.Error -> {
                    _stateFlow.value = _stateFlow.value.copy(
                        isLoading = false,
                        error = result.message ?: "Activation failed",
                        isActivationSuccessful = false
                    )
                    if(result.message.contains("Error: Already a device activated with same device Id")){
                        getDeviceInfo()
                    }
                    loadingManager.hide()
                }
            }
        }
    }

    fun getDeviceInfo() {
        loadingManager.show()
        viewModelScope.launch {
            _stateFlow.value = _stateFlow.value.copy(isLoading = true, error = null)

            when (val result = loginUseCase.deviceDetails(Constants.deviceId)) {
                is NetworkResponse.Success -> {
                    _stateFlow.value = _stateFlow.value.copy(
                        isLoading = false,
                        isActivationSuccessful = true
                    )
                    saveActivationDetails("${result.data.outletId}", "${result.data.merchantId}")
                    loadingManager.hide()
                }
                is NetworkResponse.Error -> {
                    _stateFlow.value = _stateFlow.value.copy(
                        isLoading = false,
                        error = result.message ?: "Device info fetch failed",
                        isActivationSuccessful = false
                    )
                    loadingManager.hide()
                }
            }
        }
    }



    private suspend  fun saveActivationDetails(outletId:String,merchantId:String) {
        preferencesManager.setAppMode(stateFlow.value.appMode)
        preferencesManager.setDeviceActivated()
        preferencesManager.saveOutletId(outletId)
        preferencesManager.saveMerchantId(merchantId)
    }
}
