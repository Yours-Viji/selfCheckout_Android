package com.ezycart.presentation.landing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ezycart.data.datastore.PreferencesManager
import com.ezycart.data.remote.dto.HelpRequest
import com.ezycart.data.remote.dto.NetworkResponse
import com.ezycart.domain.usecase.LoginUseCase
import com.ezycart.domain.usecase.ShoppingUseCase
import com.ezycart.model.EmployeeLoginResponse
import com.ezycart.services.usb.LoadCellSerialPort

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LandingViewModel @Inject constructor(
   /* private val shoppingUseCase: ShoppingUseCase,
    private val loginUseCase: LoginUseCase,*/
    private val preferencesManager: PreferencesManager

) : ViewModel() {
    private val _uiState = MutableStateFlow(LandingUiState())
    val uiState: StateFlow<LandingUiState> = _uiState.asStateFlow()


    private val _openLoadCellTerminalDialog = MutableStateFlow<Boolean>(false)
    val openLoadCellTerminalDialog: StateFlow<Boolean> = _openLoadCellTerminalDialog.asStateFlow()

    private val _openLedTerminalDialog = MutableStateFlow<Boolean>(false)
    val openLedTerminalDialog: StateFlow<Boolean> = _openLedTerminalDialog.asStateFlow()

    private val _openPrinterTerminalDialog = MutableStateFlow<Boolean>(false)
    val openPrinterTerminalDialog: StateFlow<Boolean> = _openPrinterTerminalDialog.asStateFlow()

    private val _canShowHelpDialog = MutableStateFlow<Boolean>(false)
    val canShowHelpDialog: StateFlow<Boolean> = _canShowHelpDialog.asStateFlow()

    private val _canStartShopping = MutableStateFlow<Boolean>(false)
    val canStartShopping: StateFlow<Boolean> = _canStartShopping.asStateFlow()

    private val _employeeLoginData = MutableStateFlow<EmployeeLoginResponse?>(null)
    val employeeLoginData: StateFlow<EmployeeLoginResponse?> = _employeeLoginData.asStateFlow()

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
    fun setStartShopping(value: Boolean){
        _canStartShopping.value = value
    }
    fun clearSystemAlert() {
        //LoadCellSerialPort.sendMessageToWeightScale("2\r\n")
        _canStartShopping.value = false
        _canShowHelpDialog.value = false
    }
    fun showHelpDialog(){
        _canShowHelpDialog.value = true
       /* try {
            createNewHelpTicket()
        } catch (e: Exception) {
        }*/
    }
    fun activateLoadCellTerminal(){
        _openLoadCellTerminalDialog.value = !openLoadCellTerminalDialog.value
    }
    fun activateLedTerminal(){
        _openLedTerminalDialog.value = !openLedTerminalDialog.value
    }
    fun activatePrinterTerminal(){
        _openPrinterTerminalDialog.value = !openPrinterTerminalDialog.value
    }
    fun onStartClicked() {
        _uiState.value = _uiState.value.copy(isStarted = true)
    }
     fun getWeightThreshold(): Double {
       var weightThreshold = 25.00
        viewModelScope.launch {
            preferencesManager.getWeightThreshold().collect { value ->
                weightThreshold = value
            }
        }

        return weightThreshold
    }

    fun updateThreshold(newValue: Double) {
        viewModelScope.launch {
            preferencesManager.setWeightThreshold(newValue)
        }
    }

   /* fun createNewHelpTicket() {
        viewModelScope.launch {
            // _stateFlow.value = _stateFlow.value.copy(isLoading = true, error = null)
            // _priceDetails.value = null

            when (val result =
                shoppingUseCase.createHelpTicket(HelpRequest(
                    cartId = "", description = "Assist Customer", deviceId = "50", requestType = "self_checkout_issue",
                    trolleyId = "50", userId = 0, cartZone = "Self Checkout", merchantId = preferencesManager.getMerchantId(),
                    outletId = preferencesManager.getOutletId(), barcode = "", productName = ""
                ))) {
                is NetworkResponse.Success -> {
                    *//* _stateFlow.value = _stateFlow.value.copy(
                         isLoading = false
                     )
                     *//*
                    // loadingManager.hide()
                }

                is NetworkResponse.Error -> {
                    *//*_stateFlow.value = _stateFlow.value.copy(
                        isLoading = false,
                        error = result.message,
                    )
                    loadingManager.hide()*//*
                }
            }
        }
    }

    fun employeeLogin(pinNumber: String) {
        clearSystemAlert()
        try {
            viewModelScope.launch {
              //  _stateFlow.value = _stateFlow.value.copy(isLoading = true, error = null)

                when (val result = loginUseCase(pinNumber)) {
                    is NetworkResponse.Success -> {
                       *//* _stateFlow.value = _stateFlow.value.copy(
                            isLoading = false
                        )*//*
                        _employeeLoginData.value = result.data
                       // loadingManager.hide()
                    }

                    is NetworkResponse.Error -> {
                       *//* _stateFlow.value = _stateFlow.value.copy(
                            isLoading = false,
                            error = result.message,
                        )*//*
                      //  loadingManager.hide()
                    }
                }
            }
        } catch (e: Exception) {
        }
    }*/
}