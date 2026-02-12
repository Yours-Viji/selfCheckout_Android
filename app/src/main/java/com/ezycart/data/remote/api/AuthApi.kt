package com.ezycart.data.remote.api

import com.ezycart.data.remote.dto.AddProductToCartRequest
import com.ezycart.data.remote.dto.ApiResponse
import com.ezycart.data.remote.dto.ApplyCouponVoucherRequest
import com.ezycart.data.remote.dto.CmsLogRequest
import com.ezycart.data.remote.dto.CreateCartRequest
import com.ezycart.data.remote.dto.CreateCartResponse
import com.ezycart.data.remote.dto.CreateJwtTokenRequest
import com.ezycart.data.remote.dto.DeleteProductInCartRequest
import com.ezycart.data.remote.dto.DeviceDetailsResponse
import com.ezycart.data.remote.dto.EditProductRequest
import com.ezycart.data.remote.dto.HelpRequest
import com.ezycart.data.remote.dto.HelpResponse
import com.ezycart.data.remote.dto.InvoiceResponse
import com.ezycart.data.remote.dto.JwtTokenResponse
import com.ezycart.data.remote.dto.LoginRequest
import com.ezycart.data.remote.dto.LoginResponse
import com.ezycart.data.remote.dto.MemberLoginRequest
import com.ezycart.data.remote.dto.MemberLoginResponse
import com.ezycart.data.remote.dto.NearPaymentSessionResponse
import com.ezycart.data.remote.dto.PaymentRequest
import com.ezycart.data.remote.dto.PaymentResponse
import com.ezycart.data.remote.dto.PaymentStatusResponse
import com.ezycart.data.remote.dto.ShoppingCartDetails
import com.ezycart.data.remote.dto.TransferCartInformation
import com.ezycart.data.remote.dto.UpdatePaymentRequest
import com.ezycart.data.remote.dto.WavPayQrPaymentRequest
import com.ezycart.data.remote.dto.WavPayQrPaymentStatus
import com.ezycart.data.remote.dto.WavPayQrResponse
import com.ezycart.model.CartActivationRequest
import com.ezycart.model.CartActivationResponse
import com.ezycart.model.EmployeeLoginRequest
import com.ezycart.model.EmployeeLoginResponse
import com.ezycart.model.ProductInfo
import com.ezycart.model.ProductPriceInfo
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.HeaderMap
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap
import retrofit2.http.Url

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @POST("/ezyRetail/activateDevice")
    suspend fun cartActivationApi(@Body cartActivationRequest: CartActivationRequest): Response<ApiResponse<CartActivationResponse>>

    @POST("/ezyCart/cs/login")
    suspend fun employeeLoginApi(@Body employeeLoginRequest: EmployeeLoginRequest): Response<ApiResponse<EmployeeLoginResponse>>

    @GET("/ezyRetail/devices/{deviceId}")
    suspend fun getDeviceDetails(
        @Path("deviceId") deviceId: String
    ): Response<DeviceDetailsResponse>


    @GET("search/product/{product_qr_code}")
    suspend fun searchProductInfo(
        @Path(value = "product_qr_code", encoded = true) productQrCode: String,
        @QueryMap params: Map<String, String>
    ): Response<ProductInfo>

    //@GET("/search/product/price/{product_qr_code}")
    @GET("/search/product/v1/price/{product_qr_code}")
    suspend fun searchPriceInfo(
        @Path(value = "product_qr_code", encoded = true) productQrCode: String,
        @QueryMap params: Map<String, String>
    ): Response<ProductPriceInfo>

    @POST("/v2/ezyCart/cart")
   suspend fun createShoppingCart(@Body createNewCartRequest: CreateCartRequest): Response<CreateCartResponse>

    @GET("/ezyCart/cart/{cart_id}")
    suspend fun getCartShoppingDetails(
        @Path(value = "cart_id", encoded = true) cartId: String): Response<ShoppingCartDetails>

    @POST("/v2/ezyCart/cart/item")
    suspend fun addProductToCartApi(@Body dddProductToCartRequest: AddProductToCartRequest): Response<ShoppingCartDetails>

    @GET("/v1/ezyCart/cart/review/{cart_id}")
    suspend fun getPaymentSummary(
        @Path(value = "cart_id", encoded = true) cartId: String): Response<ShoppingCartDetails>

    @HTTP(method = "DELETE", path = "/v2/ezyCart/cart/item", hasBody = true)
    suspend fun deleteProductFromCart(@Body deleteProductInCartRequest: DeleteProductInCartRequest): Response<ShoppingCartDetails>

    @GET("/v2/ezyCart/cart/member/{memberNumber}/refresh")
    suspend fun refreshCartByMemberId(
        @Path(value = "memberNumber", encoded = true) memberNumber: String): Response<ShoppingCartDetails>

   /* @HTTP(method = "DELETE", path = "/v1/ezyCart/support/cart/{cart_Id}", hasBody = true)
    suspend fun deleteProductFromCart(
        @Path(value = "cart_Id", encoded = true) cartId: String,
        @Body deleteProductInCartRequest: DeleteProductInCartRequest
    ): Response<ShoppingCartDetails>*/

    @PUT("/v1/ezyCart/support/cart/{cart_Id}")
    suspend fun editProductQuantity(
        @Path(value = "cart_Id", encoded = true) cartId: String,
        @Body editProductRequest: EditProductRequest
    ): Response<ShoppingCartDetails>

    @POST("/ezyCart/paymentNotify")
    suspend fun paymentNotifyApi(@Body paymentResponse: UpdatePaymentRequest):  Response<PaymentStatusResponse>

    @POST("/ezyCart/payment")
    suspend fun paymentApi(@Body paymentRequest: PaymentRequest): Response<PaymentResponse>


    @POST("/payment/jwt/{cart_Id}")
    suspend fun createNewJwtToken(
        @Path(value = "cart_Id", encoded = true) cartId: String,@Body createJwtTokenRequest:CreateJwtTokenRequest): Response<JwtTokenResponse>

    @GET("/payment/session/{cart_Id}")
    suspend fun createPaymentSessionUsingJwtToken(
        @Header("jwt-Authorization") authToken: String,
        @Path(value = "cart_Id", encoded = true) cartId: String
    ): Response<NearPaymentSessionResponse>

    @POST("/ezyCart/payment")
    suspend fun initWavPayQRPayment(
       @Body wavPayQrPaymentRequest: WavPayQrPaymentRequest): Response<WavPayQrResponse>

    @GET("ezyCart/payment/{order_Id}/status")
    suspend fun getWavPayQRPaymentStatus(
        @Path(value = "order_Id", encoded = true) orderId: String): Response<WavPayQrPaymentStatus>

    @GET("ezycart/invoice/{reference_no}/pdf-url")
    suspend fun getPdfInvoice(
        @Path(value = "reference_no", encoded = true) referenceNo: String): Response<InvoiceResponse>

    @POST("/v1/ezyCart/member/login")
    suspend fun memberLoginApi(@Body memberLoginRequest: MemberLoginRequest): Response<MemberLoginResponse>

    @POST("/v2/ezyCart/cart/coupon-voucher/{cart_id}")
    suspend fun applyCouponVoucher(
        @Path(value = "cart_id", encoded = true) cartId: String,
        @Body applyCouponVoucherRequest: ApplyCouponVoucherRequest
    ): Response<ShoppingCartDetails>

    @DELETE("/v2/ezyCart/cart/coupon-voucher/{cart_id}/{voucher_code}")
    suspend fun deleteCouponVoucher(
        @Path(value = "cart_id", encoded = true) cartId: String,
        @Path(value = "voucher_code", encoded = true) voucherCode: String
    ): Response<ShoppingCartDetails>

    @POST("/ezyCart/cs/ticket")
    suspend fun createNewHelpApi(@Body helpRequest: HelpRequest): Response<HelpResponse>

    @POST
    suspend fun sendLogsToCms(@Url url : String,@Body cmsLogRequest:CmsLogRequest): Response<Any>

    @GET
    @Headers("Content-Type:application/x-www-form-urlencoded; charset=utf-8")
    suspend fun retrieveCartTransaction(@Url url: String): Response<ShoppingCartDetails>

    @POST("/v2/ezyCart/cart/information")
    suspend fun sendTransferCartInformation(@Body transferCartInformation: TransferCartInformation): Response<Any>


}