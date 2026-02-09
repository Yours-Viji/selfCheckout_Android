package com.ezycart.presentation.landing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ezycart.data.datastore.PreferencesManager
import com.ezycart.data.remote.dto.HelpRequest
import com.ezycart.data.remote.dto.MemberLoginResponse
import com.ezycart.data.remote.dto.NetworkResponse
import com.ezycart.domain.usecase.LoginUseCase
import com.ezycart.domain.usecase.ShoppingUseCase
import com.ezycart.model.EmployeeLoginResponse
import com.ezycart.payment.terminal.GhlPaymentRepository
import com.ezycart.presentation.AppLogger
import com.ezycart.presentation.LogEvent
import com.ezycart.presentation.common.data.Constants
import com.ezycart.services.usb.LoadCellSerialPort

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class LandingViewModel @Inject constructor(
    private val shoppingUseCase: ShoppingUseCase,
    private val loginUseCase: LoginUseCase,
    private val preferencesManager: PreferencesManager,
    private val appLogger:AppLogger,
    private val repo: GhlPaymentRepository

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

    private val _memberLoginData = MutableStateFlow<MemberLoginResponse?>(null)
    val memberLoginData: StateFlow<MemberLoginResponse?> = _memberLoginData.asStateFlow()

    private val _canShowMemberDialog = MutableStateFlow<Boolean>(false)
    val canShowMemberDialog: StateFlow<Boolean> = _canShowMemberDialog.asStateFlow()

    private val _isMemberLoginSuccess = MutableStateFlow<Boolean>(false)
    val isMemberLoginSuccess: StateFlow<Boolean> = _isMemberLoginSuccess.asStateFlow()

    private val _canViewAdminSettings = MutableStateFlow<Boolean>(false)
    val canViewAdminSettings: StateFlow<Boolean> = _canViewAdminSettings.asStateFlow()

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage.asStateFlow()

    init {
        startAutoScroll()
    }
    fun onQrPayClicked(amount: String) {
        viewModelScope.launch { repo.payByCard(amount) }
    }

    fun resetPayment() {
       // paymentViewModel.paymentState.value = PaymentState.Idle
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
    fun showMemberDialog(){
        _canShowMemberDialog.value = true
    }
    fun hideAdminSettings(){
        Constants.clearAdminData()
        _canViewAdminSettings.value = false
    }
    fun clearMemberData(){
        Constants.clearMemberData()
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
    fun isProbablyQRCode(barCodeData: String): Boolean {
        return barCodeData.contains(":") ||
                barCodeData.contains("http", ignoreCase = true) ||
                barCodeData.length < 4 ||
                barCodeData.length > 20 ||
                barCodeData.lowercase().matches("[a-zA-Z]+".toRegex())
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

    */
   fun employeeLogin(pinNumber: String) {
       clearSystemAlert()
       try {
           viewModelScope.launch {
               //  _stateFlow.value = _stateFlow.value.copy(isLoading = true, error = null)

               when (val result = loginUseCase(pinNumber)) {
                   is NetworkResponse.Success -> {

                       _employeeLoginData.value = result.data
                       Constants.isAdminLogin = true
                       _canViewAdminSettings.value = true
                       employeeLoginData.value?.let {
                           Constants.adminPin = it.employeePin
                           Constants.employeeToken = it.token
                       }
                       appLogger.sendLogData(preferencesManager.getMerchantId(),preferencesManager.getOutletId(),"","",
                           "-",
                           LogEvent.ADMIN_LOGIN,"",0,"","")
                       // loadingManager.hide()
                   }

                   is NetworkResponse.Error -> {

                   }
               }
           }
       } catch (e: Exception) {
       }
   }
   fun memberLogin(memberNumber: String) {
       //loadingManager.show()
       try {
           viewModelScope.launch {
              // _stateFlow.value = _stateFlow.value.copy(isLoading = true, error = null)

               when (val result = shoppingUseCase.memberLogin(memberNumber)) {
                   is NetworkResponse.Success -> {
                      /* _stateFlow.value = _stateFlow.value.copy(
                           isLoading = false
                       )*/
                       hideAdminSettings()
                       _memberLoginData.value = result.data
                       Constants.isMemberLogin = true
                       memberLoginData.value?.let {
                           Constants.memberPin = it.memberNo
                       }
                       _canShowMemberDialog.value = false
                       _isMemberLoginSuccess.value = true
                       appLogger.sendLogData(preferencesManager.getMerchantId(),preferencesManager.getOutletId(),"","",
                           "-",
                           LogEvent.MEMBER_LOGIN,"",0,"","")
                      // loadingManager.hide()
                   }

                   is NetworkResponse.Error -> {
                       _errorMessage.value = result.message
                     /*  _stateFlow.value = _stateFlow.value.copy(
                           isLoading = false,
                           error = result.message,
                       )
                       loadingManager.hide()*/
                   }
               }
           }
       } catch (e: Exception) {
       }
   }
}