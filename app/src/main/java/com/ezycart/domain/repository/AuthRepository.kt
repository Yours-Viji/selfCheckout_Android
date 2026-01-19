package com.ezycart.domain.repository

import com.ezycart.data.remote.dto.CreateCartResponse
import com.ezycart.data.remote.dto.CreateJwtTokenRequest
import com.ezycart.data.remote.dto.DeviceDetailsResponse
import com.ezycart.data.remote.dto.JwtTokenResponse
import com.ezycart.data.remote.dto.NearPaymentSessionResponse
import com.ezycart.data.remote.dto.NetworkResponse
import com.ezycart.data.remote.dto.PaymentRequest
import com.ezycart.data.remote.dto.PaymentResponse
import com.ezycart.data.remote.dto.PaymentStatusResponse
import com.ezycart.data.remote.dto.ShoppingCartDetails
import com.ezycart.data.remote.dto.UpdatePaymentRequest
import com.ezycart.data.remote.dto.WavPayQrPaymentRequest
import com.ezycart.data.remote.dto.WavPayQrPaymentStatus
import com.ezycart.data.remote.dto.WavPayQrResponse
import com.ezycart.model.CartActivationResponse
import com.ezycart.model.EmployeeLoginResponse
import com.ezycart.model.ProductInfo
import com.ezycart.model.ProductPriceInfo
import kotlinx.coroutines.flow.Flow
import org.json.JSONObject

interface AuthRepository {
    suspend fun login(employeePin: String): NetworkResponse<EmployeeLoginResponse>
    suspend fun activateDevice(activationCode: String, trolleyNumber: String): NetworkResponse<CartActivationResponse>
    suspend fun getDeviceDetails(deviceId: String): NetworkResponse<DeviceDetailsResponse>
    suspend fun getProductDetails(barCode: String): NetworkResponse<ProductInfo>
    suspend fun getPriceDetails(barCode: String): NetworkResponse<ProductPriceInfo>
    suspend fun createNewShoppingCart(): NetworkResponse<CreateCartResponse>
    suspend fun getShoppingCartDetails(): NetworkResponse<ShoppingCartDetails>
    suspend fun getPaymentSummary(): NetworkResponse<ShoppingCartDetails>
    suspend fun addProductToShoppingCart(barCode: String,quantity:Int): NetworkResponse<ShoppingCartDetails>
    suspend fun deleteProductFromShoppingCart(barCode: String,id:Int): NetworkResponse<ShoppingCartDetails>
    suspend fun editProductInCart(barCode: String,id:Int,quantity:Int): NetworkResponse<ShoppingCartDetails>
    suspend fun updatePaymentStatus(status: UpdatePaymentRequest): NetworkResponse<PaymentStatusResponse>
    suspend fun makePayment(paymentRequest: PaymentRequest): NetworkResponse<PaymentResponse>
    suspend fun createJwtToken(jwtTokenRequest: CreateJwtTokenRequest): NetworkResponse<JwtTokenResponse>
    suspend fun createNearPaySession(): NetworkResponse<NearPaymentSessionResponse>
    suspend fun initWavPayQRPayment(): NetworkResponse<WavPayQrResponse>
    suspend fun getWavPayQRPaymentStatus(): NetworkResponse<WavPayQrPaymentStatus>
    suspend fun saveAuthToken(token: String)
    suspend fun getAuthToken(): String?
    //suspend fun refreshInterceptor()
    fun isDeviceActivated(): Flow<Boolean>
    fun getCartId(): Flow<String>
}