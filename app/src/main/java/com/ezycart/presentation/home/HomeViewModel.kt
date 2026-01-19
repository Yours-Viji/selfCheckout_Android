package com.ezycart.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ezycart.data.datastore.PreferencesManager
import com.ezycart.data.remote.dto.CartItem
import com.ezycart.data.remote.dto.CreateJwtTokenRequest
import com.ezycart.data.remote.dto.NetworkResponse
import com.ezycart.data.remote.dto.PaymentRequest
import com.ezycart.data.remote.dto.ShoppingCartDetails
import com.ezycart.data.remote.dto.UpdatePaymentRequest
import com.ezycart.domain.model.AppMode
import com.ezycart.domain.usecase.GetCartIdUseCase
import com.ezycart.domain.usecase.LoadingManager
import com.ezycart.domain.usecase.PaymentUseCase
import com.ezycart.domain.usecase.ShoppingUseCase
import com.ezycart.model.ProductInfo
import com.ezycart.model.ProductPriceInfo
import com.ezycart.presentation.common.data.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val shoppingUseCase: ShoppingUseCase,
    private val paymentUseCase: PaymentUseCase,
    private val getCartIdUseCase: GetCartIdUseCase,
    private val preferencesManager: PreferencesManager,
    private val loadingManager: LoadingManager,
) : ViewModel() {
    private val _stateFlow = MutableStateFlow(HomeState())
    val stateFlow: StateFlow<HomeState> = _stateFlow.asStateFlow()

    private val _cartDataList = MutableStateFlow<List<CartItem>>(emptyList())
    val cartDataList: StateFlow<List<CartItem>> = _cartDataList.asStateFlow()

    private val _cartCount = MutableStateFlow(0)
    val cartCount: StateFlow<Int> = _cartCount.asStateFlow()

    private val _shoppingCartInfo = MutableStateFlow<ShoppingCartDetails?>(null)
    val shoppingCartInfo: StateFlow<ShoppingCartDetails?> = _shoppingCartInfo.asStateFlow()

    private val _productInfo = MutableStateFlow<ProductInfo?>(null)
    val productInfo: StateFlow<ProductInfo?> = _productInfo.asStateFlow()

    private val _priceDetails = MutableStateFlow<ProductPriceInfo?>(null)
    val priceDetails: StateFlow<ProductPriceInfo?> = _priceDetails.asStateFlow()

    private val _isPickerModel = MutableStateFlow<Boolean>(false)
    val isPickerModel: StateFlow<Boolean> = _isPickerModel.asStateFlow()

    private val _canShowPriceChecker = MutableStateFlow<Boolean>(false)
    val canShowPriceChecker: StateFlow<Boolean> = _canShowPriceChecker.asStateFlow()

    private val _appMode = MutableStateFlow(AppMode.EzyLite)
    val appMode: StateFlow<AppMode> = _appMode

    private val _employeeName = MutableStateFlow("")
    val employeeName: StateFlow<String> = _employeeName.asStateFlow()

    private val _wavPayQrPaymentUrl = MutableStateFlow("")
    val wavPayQrPaymentUrl: StateFlow<String> = _wavPayQrPaymentUrl.asStateFlow()

    private val _canShowQrPaymentDialog = MutableStateFlow<Boolean>(false)
    val canShowQrPaymentDialog: StateFlow<Boolean> = _canShowQrPaymentDialog.asStateFlow()

    private val _canShowPaymentErrorDialog = MutableStateFlow<Boolean>(false)
    val canShowPaymentErrorDialog: StateFlow<Boolean> = _canShowPaymentErrorDialog.asStateFlow()

    private val _canShowPaymentSuccessDialog = MutableStateFlow<Boolean>(false)
    val canShowPaymentSuccessDialog: StateFlow<Boolean> = _canShowPaymentSuccessDialog.asStateFlow()

    private var pollingJob: Job? = null
    private val _paymentStatusState = MutableStateFlow<PaymentStatusState>(PaymentStatusState.Idle)
    val paymentStatusState: StateFlow<PaymentStatusState> = _paymentStatusState.asStateFlow()
var tempcounter =0
    sealed class PaymentStatusState {
        object Idle : PaymentStatusState()
        object Loading : PaymentStatusState()
        data class Success(val data: Any) : PaymentStatusState()
        data class Error(val message: String) : PaymentStatusState()
        object Polling : PaymentStatusState()
    }

    var cartId = ""
    var isJwtTokenCreated =false
    init {
        viewModelScope.launch {
            val savedAppMode = preferencesManager.getAppMode()
            _appMode.update { savedAppMode }
          //  _isPickerModel.update { savedAppMode.name == AppMode.EzyLite.name }
            _employeeName.update { preferencesManager.getEmployeeName() }
            _canShowPriceChecker.update { preferencesManager.canShowPriceChecker() }
        }

    }

    fun setPriceCheckerView(canShow: Boolean){
        viewModelScope.launch {
            preferencesManager.setPriceCheckerStatus(canShow)
        }
        _canShowPriceChecker.update { canShow }
    }
    fun onAppModeChange(selectedAppMode:AppMode) {
        viewModelScope.launch {
          //  _isPickerModel.update {selectedAppMode.name == AppMode.EzyLite.name}
            preferencesManager.setAppMode(selectedAppMode)
        }
    }
    fun initNewShopping(){
        clearCartDetails()
        createNewShoppingCart()
    }
   private fun clearCartDetails(){
       isJwtTokenCreated = false
        _productInfo.value = null
        _priceDetails.value = null
        _shoppingCartInfo.value = null
        _cartDataList.value=emptyList()
        _cartCount.value=0
    }
    private fun createNewShoppingCart() {
        loadingManager.show()
        clearCartDetails()
        viewModelScope.launch {
            _stateFlow.value = _stateFlow.value.copy(isLoading = true, error = null)

            when (val result = shoppingUseCase.createNewShoppingCart()) {
                is NetworkResponse.Success -> {
                    _stateFlow.value = _stateFlow.value.copy(
                        isLoading = false,
                    )
                    loadingManager.hide()
                    cartId = preferencesManager.getShoppingCartId()
                }
                is NetworkResponse.Error -> {
                    _stateFlow.value = _stateFlow.value.copy(
                        isLoading = false,
                        error = result.message ?: "Unable to create cart",
                    )
                    loadingManager.hide()
                }
            }
        }
    }

    fun getShoppingCartDetails() {
        loadingManager.show()
        viewModelScope.launch {
            _stateFlow.value = _stateFlow.value.copy(isLoading = true, error = null)
            when (val result = shoppingUseCase()) {
                is NetworkResponse.Success -> {
                    _stateFlow.value = _stateFlow.value.copy(
                        isLoading = false
                    )
                    _cartDataList.value=result.data.cartItems
                    loadingManager.hide()
                }
                is NetworkResponse.Error -> {
                    _stateFlow.value = _stateFlow.value.copy(
                        isLoading = false,
                        error = result.message,
                    )
                    loadingManager.hide()
                }
            }
        }
    }

    private fun getPaymentSummary() {
        loadingManager.show()
        viewModelScope.launch {
            _stateFlow.value = _stateFlow.value.copy(isLoading = true, error = null)

            when (val result = shoppingUseCase.getPaymentSummary()) {
                is NetworkResponse.Success -> {
                    _stateFlow.value = _stateFlow.value.copy(
                        isLoading = false
                    )
                    _shoppingCartInfo.value=result.data
                    loadingManager.hide()
                  /*  if(!isJwtTokenCreated){
                        isJwtTokenCreated = true
                        createNewJwtToken()
                    }*/

                }
                is NetworkResponse.Error -> {
                    _stateFlow.value = _stateFlow.value.copy(
                        isLoading = false,
                        error = result.message,
                    )
                    loadingManager.hide()
                }
            }
        }
    }

    fun addProductToShoppingCart(barCode: String,quantity:Int) {
        loadingManager.show()
        resetProductInfoDetails()
        viewModelScope.launch {
            _stateFlow.value = _stateFlow.value.copy(isLoading = true, error = null)
            when (val result = shoppingUseCase.addToCart(barCode,quantity)) {
                is NetworkResponse.Success -> {
                    _stateFlow.value = _stateFlow.value.copy(
                        isLoading = false
                    )
                    _cartDataList.value=result.data.cartItems
                    _cartCount.value = result.data.totalItems
                    loadingManager.hide()
                    getPaymentSummary()

                }
                is NetworkResponse.Error -> {
                    _stateFlow.value = _stateFlow.value.copy(
                        isLoading = false,
                        error = result.message,
                    )
                    loadingManager.hide()
                }
            }
        }
    }

    fun editProductInShoppingCart(barCode: String,quantity:Int,id:Int) {
        loadingManager.show()
        resetProductInfoDetails()
        viewModelScope.launch {
            _stateFlow.value = _stateFlow.value.copy(isLoading = true, error = null)
            when (val result = shoppingUseCase.editProductInCart(barCode,quantity,id)) {
                is NetworkResponse.Success -> {
                    _stateFlow.value = _stateFlow.value.copy(
                        isLoading = false
                    )
                    _cartDataList.value=result.data.cartItems
                    _cartCount.value = result.data.totalItems
                    loadingManager.hide()
                    getPaymentSummary()
                }
                is NetworkResponse.Error -> {
                    _stateFlow.value = _stateFlow.value.copy(
                        isLoading = false,
                        error = result.message,
                    )
                    loadingManager.hide()
                }
            }
        }
    }


    fun deleteProductFromShoppingCart(barCode: String,id:Int) {
        loadingManager.show()
        resetProductInfoDetails()
        viewModelScope.launch {
            _stateFlow.value = _stateFlow.value.copy(isLoading = true, error = null)
            when (val result = shoppingUseCase.deleteProductFromCart(barCode,id)) {
                is NetworkResponse.Success -> {
                    _stateFlow.value = _stateFlow.value.copy(
                        isLoading = false
                    )
                    _cartDataList.value=result.data.cartItems
                    _cartCount.value = result.data.totalItems
                    loadingManager.hide()
                    getPaymentSummary()
                }
                is NetworkResponse.Error -> {
                    _stateFlow.value = _stateFlow.value.copy(
                        isLoading = false,
                        error = result.message,
                    )
                    loadingManager.hide()
                }
            }
        }
    }

    fun getProductDetails(barCode:String) {
        loadingManager.show()
        viewModelScope.launch {
            _stateFlow.value = _stateFlow.value.copy(isLoading = true, error = null)
            _productInfo.value = null
            when (val result = shoppingUseCase.getProductDetails(barCode)) {
                is NetworkResponse.Success -> {
                    _stateFlow.value = _stateFlow.value.copy(
                        isLoading = false
                    )
                    _productInfo.value=result.data
                    getPriceDetails(barCode)
                }
                is NetworkResponse.Error -> {
                    var message = result.message
                    if (message.contains("End of input at line 1 column 1 path")) {
                        message = "API Error: Server returned empty response"
                    }
                    _stateFlow.value = _stateFlow.value.copy(
                        isLoading = false,
                        error = message
                    )
                    loadingManager.hide()
                    //End of input at line 1 column 1 path
                }
            }
        }
    }
    fun clearError() {
        _stateFlow.value = _stateFlow.value.copy(error = null)
    }
    fun resetProductInfoDetails() {
        _productInfo.value = null
        _priceDetails.value = null
    }

    private fun getPriceDetails(barCode:String) {
        viewModelScope.launch {
            _stateFlow.value = _stateFlow.value.copy(isLoading = true, error = null)
            _priceDetails.value = null
            when (val result = shoppingUseCase.getPriceDetails(barCode)) {
                is NetworkResponse.Success -> {
                    _stateFlow.value = _stateFlow.value.copy(
                        isLoading = false
                    )
                    _priceDetails.value=result.data
                    loadingManager.hide()
                }
                is NetworkResponse.Error -> {
                    _stateFlow.value = _stateFlow.value.copy(
                        isLoading = false,
                        error = result.message,
                    )
                    loadingManager.hide()
                }
            }
        }
    }
     fun updatePaymentStatus(reference: String) {
        viewModelScope.launch {
            _stateFlow.value = _stateFlow.value.copy(isLoading = true, error = null)
            _priceDetails.value = null
            when (val result = shoppingUseCase.updatePaymentStatus(getMockPaymentResponse(reference))) {
                is NetworkResponse.Success -> {
                    _stateFlow.value = _stateFlow.value.copy(
                        isLoading = false
                    )
                    initNewShopping()
                    loadingManager.hide()
                }
                is NetworkResponse.Error -> {
                    _stateFlow.value = _stateFlow.value.copy(
                        isLoading = false,
                        error = result.message,
                    )
                    loadingManager.hide()
                }
            }
        }
    }
    fun makePayment(type:Int) {
        viewModelScope.launch {
            _stateFlow.value = _stateFlow.value.copy(isLoading = true, error = null)
            _priceDetails.value = null
            when (val result = shoppingUseCase.makePayment(if(type==1)getPaymentRequest() else getTapToPayPaymentRequest())) {
                is NetworkResponse.Success -> {
                    _stateFlow.value = _stateFlow.value.copy(
                        isLoading = false
                    )
                    result.data.referenceNo.let {
                        updatePaymentStatus(result.data.referenceNo)
                    }
                    loadingManager.hide()
                }
                is NetworkResponse.Error -> {
                    _stateFlow.value = _stateFlow.value.copy(
                        isLoading = false,
                        error = result.message,
                    )
                    loadingManager.hide()
                }
            }
        }
    }

    private fun createNewJwtToken() {
        loadingManager.show()
        viewModelScope.launch {
            _stateFlow.value = _stateFlow.value.copy(isLoading = true, error = null)

            when (val result = paymentUseCase.createNewJwtToken(
                CreateJwtTokenRequest("8d1cbffb-1e18-4e1e-9aca-b3dc842de74e","0211206300112063","240419","0211206300112063"))) {
                is NetworkResponse.Success -> {
                    _stateFlow.value = _stateFlow.value.copy(
                        isLoading = false,
                    )
                    preferencesManager.saveJwtToken(result.data.token)
                    loadingManager.hide()

                    createNewNearPaySession()
                }
                is NetworkResponse.Error -> {
                    _stateFlow.value = _stateFlow.value.copy(
                        isLoading = false,
                        error = result.message ?: "Unable to create jwt token",
                    )
                    loadingManager.hide()
                }
            }
        }
    }

    private fun createNewNearPaySession() {
        loadingManager.show()
        viewModelScope.launch {
            _stateFlow.value = _stateFlow.value.copy(isLoading = true, error = null)

            when (val result = paymentUseCase.createNearPaySession()) {
                is NetworkResponse.Success -> {
                    _stateFlow.value = _stateFlow.value.copy(
                        isLoading = false,
                        isReadyToInitializePaymentSdk = true
                    )

                    loadingManager.hide()
                }
                is NetworkResponse.Error -> {
                    _stateFlow.value = _stateFlow.value.copy(
                        isLoading = false,
                        error = result.message ?: "Unable to create near Pay session",
                    )
                    loadingManager.hide()
                }
            }
        }
    }


    fun initWavPayQrPayment(barCode:String) {
        Constants.paymentCode = barCode
        loadingManager.show()
        viewModelScope.launch {
            _stateFlow.value = _stateFlow.value.copy(isLoading = true, error = null)

            when (val result = paymentUseCase.initWavPayQRPayment()) {
                is NetworkResponse.Success -> {
                    _stateFlow.value = _stateFlow.value.copy(
                        isLoading = false,
                    )
                    _wavPayQrPaymentUrl.value=result.data.qr_code
                  //  _canShowQrPaymentDialog.value=true
                    startWavPayQrPaymentStatusPolling()
                    loadingManager.hide()
                }
                is NetworkResponse.Error -> {
                    _stateFlow.value = _stateFlow.value.copy(
                        isLoading = false,
                        error = result.message ?: "Unable to start wavpay QR payment",
                    )
                    loadingManager.hide()
                }
            }
        }
    }

    fun startWavPayQrPaymentStatusPolling() {
        stopPaymentStatusPolling()

        pollingJob = viewModelScope.launch {
            _paymentStatusState.value = PaymentStatusState.Polling
            pollPaymentStatusRecursively()
        }

      /*  _canShowQrPaymentDialog.value = false
        _canShowPaymentSuccessDialog.value = true*/
    }

    fun stopPaymentStatusPolling() {
        pollingJob?.cancel()
        pollingJob = null
        _paymentStatusState.value = PaymentStatusState.Idle
    }

    private suspend fun pollPaymentStatusRecursively() {
        try {
            when (val result = paymentUseCase.getWavPayQRPaymentStatus()) {
                is NetworkResponse.Success -> {
                    // Check if payment is successful
                    if (result.data.status == "100") {
                        // Payment successful - stop polling
                        _paymentStatusState.value = PaymentStatusState.Success(result.data)
                        _canShowPaymentSuccessDialog.value = true
                        return // Stop recursion
                    }
                    // Payment still pending - continue polling

                    else if (result.data.status == "NOT FOUND") {

                        _paymentStatusState.value = PaymentStatusState.Polling
                        // Wait for 2 seconds before next call
                        delay(2000)
                        // Recursive call
                        pollPaymentStatusRecursively()

                    }
                    //  if payment failed
                    else {
                        _paymentStatusState.value = PaymentStatusState.Error("Payment Failed, Please Try Again")
                        _canShowPaymentSuccessDialog.value = false
                        _canShowPaymentErrorDialog.value = true
                        //Show Payment Error message
                        return // Stop recursion
                    }
                }
                is NetworkResponse.Error -> {
                    // API error but continue polling
                    _paymentStatusState.value = PaymentStatusState.Error(
                        result.message ?: "Unable to get payment status"
                    )
                    // Wait for 2 seconds before retry
                    delay(2000)
                    // Recursive call to retry
                    pollPaymentStatusRecursively()
                }
            }
        } catch (e: Exception) {
            // Handle coroutine cancellation
            if (e is kotlinx.coroutines.CancellationException) {
                throw e // Re-throw cancellation to properly stop
            }
            // Other exceptions - continue polling
            _paymentStatusState.value = PaymentStatusState.Error("Network error: ${e.message}")
            delay(2000)
            pollPaymentStatusRecursively()
        }
    }


    fun hideQrPaymentAlertView(){
        _canShowQrPaymentDialog.value=false
        stopPaymentStatusPolling()

    }
    fun hidePaymentSuccessAlertView(){
        _canShowPaymentSuccessDialog.value=false
// Send receipt
    }
    fun hidePaymentErrorAlertView(){
        _canShowPaymentErrorDialog.value=false
// Send receipt
    }
    private fun getMockPaymentResponse(reference: String): UpdatePaymentRequest {
        return UpdatePaymentRequest(reference,"100","Approved")
    }

    private fun getPaymentRequest(): PaymentRequest {
       return PaymentRequest("DUITNOW","DUITNOW@123456789")
    }
    private fun getTapToPayPaymentRequest(): PaymentRequest {
        return PaymentRequest("HLB","HLB@123456789")
    }

}