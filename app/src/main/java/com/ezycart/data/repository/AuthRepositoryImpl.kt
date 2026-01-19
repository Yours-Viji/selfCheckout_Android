package com.ezycart.data.repository

import android.util.Log
import com.ezycart.data.datastore.PreferencesManager
import com.ezycart.data.remote.api.AuthApi
import com.ezycart.data.remote.dto.*
import com.ezycart.domain.model.User
import com.ezycart.domain.repository.AuthRepository
import com.ezycart.model.CartActivationRequest
import com.ezycart.model.CartActivationResponse
import com.ezycart.model.EmployeeLoginRequest
import com.ezycart.model.EmployeeLoginResponse
import com.ezycart.model.ProductInfo
import com.ezycart.model.ProductPriceInfo
import com.ezycart.presentation.common.data.Constants
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import retrofit2.Response
import retrofit2.Retrofit
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val preferencesManager: PreferencesManager,
) : AuthRepository {

    override suspend fun login(employeePin: String): NetworkResponse<EmployeeLoginResponse> {
        return safeApiCall {
            authApi.employeeLoginApi(
                EmployeeLoginRequest(
                    employeePin,
                    preferencesManager.getMerchantId(),
                    preferencesManager.getOutletId()
                )
            )
        }.also { result ->
            if (result is NetworkResponse.Success) {
                preferencesManager.saveEmployeeDetails(result.data)
            }
        }
    }

    override suspend fun activateDevice(
        activationCode: String,
        trolleyNumber: String
    ): NetworkResponse<CartActivationResponse> {
        return safeApiCall {
            authApi.cartActivationApi(CartActivationRequest(activationCode, trolleyNumber))
        }.also { result ->
            if (result is NetworkResponse.Success) {
                preferencesManager.setDeviceActivated()
            }
        }
    }

    override suspend fun getDeviceDetails(deviceId: String): NetworkResponse<DeviceDetailsResponse> {
        return safeApiCallRaw { authApi.getDeviceDetails(deviceId) }
            .also { result ->
                if (result is NetworkResponse.Success) {
                    preferencesManager.setDeviceActivated()
                }
            }
    }

    override suspend fun getProductDetails(barCode: String): NetworkResponse<ProductInfo> {
        return safeApiCallRaw { authApi.searchProductInfo(barCode,getMerchantParam()) }
            .also { result ->
                if (result is NetworkResponse.Success) {
                }
            }
    }


    override suspend fun getPriceDetails(barCode: String): NetworkResponse<ProductPriceInfo> {
        return safeApiCallRaw { authApi.searchPriceInfo(barCode,getMerchantParam()) }
            .also { result ->
                if (result is NetworkResponse.Success) {
                }
            }
    }

    override suspend fun createNewShoppingCart(): NetworkResponse<CreateCartResponse> {
        return safeApiCallRaw { authApi.createShoppingCart(getCreateCartRequestData()) }
            .also { result ->
                if (result is NetworkResponse.Success) {
                    preferencesManager.saveCartId(result.data.cartId)
                    preferencesManager.saveAuthToken(result.data.token)
                }
            }
    }


    override suspend fun getShoppingCartDetails(): NetworkResponse<ShoppingCartDetails> {
        return safeApiCallRaw { authApi.getCartShoppingDetails(preferencesManager.getShoppingCartId()) }
            .also { result ->
                if (result is NetworkResponse.Success) {
                }
            }
    }
    override suspend fun getPaymentSummary(): NetworkResponse<ShoppingCartDetails> {
        return safeApiCallRaw { authApi.getPaymentSummary(preferencesManager.getShoppingCartId()) }
            .also { result ->
                if (result is NetworkResponse.Success) {
                }
            }
    }
    override suspend fun addProductToShoppingCart(barCode: String,quantity:Int): NetworkResponse<ShoppingCartDetails> {
        return safeApiCallRaw { authApi.addProductToCartApi(AddProductToCartRequest(barCode,quantity)) }
            .also { result ->
                if (result is NetworkResponse.Success) {
                }
            }
    }

    override suspend fun editProductInCart(barCode: String,id:Int,quantity:Int): NetworkResponse<ShoppingCartDetails> {
        return safeApiCallRaw { authApi.editProductQuantity(preferencesManager.getShoppingCartId(),EditProductRequest(id,barCode,quantity)) }
            .also { result ->
                if (result is NetworkResponse.Success) {
                }
            }
    }

    override suspend fun deleteProductFromShoppingCart(barCode: String,id:Int): NetworkResponse<ShoppingCartDetails> {
        return safeApiCallRaw { authApi.deleteProductFromCart(preferencesManager.getShoppingCartId(),DeleteProductInCartRequest(id,barCode)) }
            .also { result ->
                if (result is NetworkResponse.Success) {
                }
            }
    }
    override suspend fun updatePaymentStatus(status: UpdatePaymentRequest): NetworkResponse<PaymentStatusResponse> {
        return safeApiCallRaw { authApi.paymentNotifyApi(status) }
            .also { result ->
                if (result is NetworkResponse.Success) {
                }
            }
    }
    override suspend fun makePayment(paymentRequest: PaymentRequest): NetworkResponse<PaymentResponse> {
        return safeApiCallRaw { authApi.paymentApi(paymentRequest) }
            .also { result ->
                if (result is NetworkResponse.Success) {
                }
            }
    }

    override suspend fun createJwtToken(jwtTokenRequest: CreateJwtTokenRequest): NetworkResponse<JwtTokenResponse> {
        return safeApiCallRaw { authApi.createNewJwtToken(preferencesManager.getShoppingCartId(),jwtTokenRequest) }
            .also { result ->
                if (result is NetworkResponse.Success) {
                    preferencesManager.saveJwtToken(result.data.token)
                    Constants.jwtToken = result.data.token
                    Log.i("Result","${result.data}")
                }
            }
    }


    override suspend fun createNearPaySession(): NetworkResponse<NearPaymentSessionResponse> {
        return safeApiCallRaw { authApi.createPaymentSessionUsingJwtToken(authToken = preferencesManager.getJwtToken(), cartId = preferencesManager.getShoppingCartId()) }
            .also { result ->
                if (result is NetworkResponse.Success) {
                    Constants.nearPaySessionID=result.data.sessionId
                }
            }
    }

    override suspend fun initWavPayQRPayment(): NetworkResponse<WavPayQrResponse> {
        return safeApiCallRaw { authApi.initWavPayQRPayment(WavPayQrPaymentRequest("WAVPAY","WAVPAY@1234567890",
            Constants.paymentCode)) }
            .also { result ->
                if (result is NetworkResponse.Success) {
                    Constants.paymentOrderId=result.data.order_id
                }
            }
    }

    override suspend fun getWavPayQRPaymentStatus(): NetworkResponse<WavPayQrPaymentStatus> {
        return safeApiCallRaw { authApi.getWavPayQRPaymentStatus(orderId = Constants.paymentOrderId) }
            .also { result ->
                if (result is NetworkResponse.Success) {
                    //Constants.nearPaySessionID=result.data.sessionId
                }
            }
    }

    private suspend fun getMerchantParam(): HashMap<String, String> {
        val params = HashMap<String, String>()
        params["merchantId"] = "" + preferencesManager.getMerchantId()
        params["outletId"] = "" + preferencesManager.getOutletId()
        params["isMemberLogin"] = "false"

        return params
    }


        private suspend fun getCreateCartRequestData(): CreateCartRequest {
            val prefs = preferencesManager.userPreferencesFlow.first()
            val outletId = preferencesManager.getOutletId()
            val merchantId = preferencesManager.getMerchantId()
            val appMode = preferencesManager.getAppMode()
            val employeeId = preferencesManager.getEmployeeId()

            return CreateCartRequest(
                employeeId = prefs.employeeId.toString(),
                memberNumber = "12345678",
                userId = "$employeeId",
                name = prefs.employeeName,
                deviceId = Constants.deviceId,
                outletId = outletId,
                merchantId = merchantId,
                appMode = appMode.name,
                trolleyNo = "01"
            )
        }

    override suspend fun saveAuthToken(token: String) {
        preferencesManager.saveAuthToken(token)
    }

    override suspend fun getAuthToken(): String? {
        return preferencesManager.getAuthToken()
    }

    override fun isDeviceActivated(): Flow<Boolean> {
        return preferencesManager.isDeviceActivated()
    }

    override fun getCartId(): Flow<String> {
        return preferencesManager.getCartId()
    }

    private fun LoginResponse.toUser(): User {
        return User(
            id = id,
            email = email,
            name = name,
            token = token
        )
    }

    /**
     * For APIs that return ApiResponse<T>.
     */
    private suspend fun <T> safeApiCall(
        apiCall: suspend () -> Response<ApiResponse<T>>
    ): NetworkResponse<T> {
        return try {
            val response = apiCall()
            val body = response.body()

            if (response.isSuccessful && body != null) {
                when {
                    body.data != null -> NetworkResponse.Success(body.data)
                    body.error != null -> NetworkResponse.Error(
                        body.error.message ?: "Unknown API error",
                        body.error.code
                    )
                    body.message != null -> NetworkResponse.Error(body.message, response.code())
                    else -> NetworkResponse.Error("Empty response body", response.code())
                }
            } else {
                val rawJson = response.errorBody()?.string()
                NetworkResponse.Error(parseErrorMessage(rawJson), response.code())
            }
        } catch (e: Exception) {
            NetworkResponse.Error(e.localizedMessage ?: "Unexpected error")
        }
    }

    /**
     * For APIs that return raw model (not wrapped).
     */
    private suspend fun <T> safeApiCallRaw(
        apiCall: suspend () -> Response<T>
    ): NetworkResponse<T> {
        return try {
            val response = apiCall()
            val body = response.body()

            if (response.isSuccessful && body != null) {
                NetworkResponse.Success(body)
            } else {
                val rawJson = response.errorBody()?.string()
                NetworkResponse.Error(parseErrorMessage(rawJson), response.code())
            }
        } catch (e: Exception) {
            NetworkResponse.Error(e.localizedMessage ?: "Unexpected error")
        }
    }

    /**
     * Tries to parse error JSON into a human-readable message.
     */
    private fun parseErrorMessage(rawJson: String?): String {
        if (rawJson.isNullOrBlank()) return "Unknown error"

        return try {
            // Try parsing as ApiResponse
            val apiResponse = Gson().fromJson(rawJson, ApiResponse::class.java)
            apiResponse?.error?.message
                ?: apiResponse?.message
                ?: "Unknown API error"
        } catch (_: Exception) {
            try {
                // Try parsing as GenericErrorResponse
                val genericError = Gson().fromJson(rawJson, GenericErrorResponse::class.java)
                genericError?.message
                    ?: genericError?.publicMessage
                    ?: genericError?.error
                    ?: "Unknown server error"
            } catch (_: Exception) {
                rawJson // fallback: return raw JSON
            }
        }
    }
}
