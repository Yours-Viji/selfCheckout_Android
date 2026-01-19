package com.ezycart.data.remote.api

import com.ezycart.data.remote.dto.AddProductToCartRequest
import com.ezycart.data.remote.dto.ApiResponse
import com.ezycart.data.remote.dto.CreateCartRequest
import com.ezycart.data.remote.dto.CreateCartResponse
import com.ezycart.data.remote.dto.CreateJwtTokenRequest
import com.ezycart.data.remote.dto.DeleteProductInCartRequest
import com.ezycart.data.remote.dto.DeviceDetailsResponse
import com.ezycart.data.remote.dto.EditProductRequest
import com.ezycart.data.remote.dto.JwtTokenResponse
import com.ezycart.data.remote.dto.LoginRequest
import com.ezycart.data.remote.dto.LoginResponse
import com.ezycart.data.remote.dto.NearPaymentSessionResponse
import com.ezycart.data.remote.dto.PaymentRequest
import com.ezycart.data.remote.dto.PaymentResponse
import com.ezycart.data.remote.dto.PaymentStatusResponse
import com.ezycart.data.remote.dto.ShoppingCartDetails
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


    @HTTP(method = "DELETE", path = "/v1/ezyCart/support/cart/{cart_Id}", hasBody = true)
    suspend fun deleteProductFromCart(
        @Path(value = "cart_Id", encoded = true) cartId: String,
        @Body deleteProductInCartRequest: DeleteProductInCartRequest
    ): Response<ShoppingCartDetails>

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

    /* @POST("/Login")
    fun loginApi(@Body loginRequest: LoginRequest): Call<Any>

    @GET("ezcart/product")
    @Headers("Content-Type:application/x-www-form-urlencoded; charset=utf-8")
    fun scannedProductDetails(@QueryMap params: Map<String, String>): Call<Any>


    //@GET("/ezyCart/product/{product_qr_code}")
    @GET("v2/ezyCart/product/{product_qr_code}")
    @Headers("Content-Type:application/x-www-form-urlencoded; charset=utf-8")
    fun scannedProductDetail(
        @Path(value = "product_qr_code", encoded = true) productQrCode: String,
        @QueryMap params: Map<String, String>
    ): Call<Any>

    @GET("search/product/{product_qr_code}")
    fun searchProductInfo(
        @Path(value = "product_qr_code", encoded = true) productQrCode: String,
        @QueryMap params: Map<String, String>
    ): Call<Any>

    @GET("/search/product/price/{product_qr_code}")
    fun searchPriceInfo(
        @Path(value = "product_qr_code", encoded = true) productQrCode: String,
        @QueryMap params: Map<String, String>
    ): Call<Any>

    //@POST("/ezyCart/cart")
    //@POST("/v1/ezyCart/ai/cart")
    @POST("/v2/ezyCart/cart")
    fun createCartApi(@Body createNewCartRequest: CreateNewCartRequest): Call<Any>

    @POST("/v1/ezyCart/member/login")
    fun memberLoginApi(@Body redtickMemberLoginRequest: RedtickMemberLoginRequest): Call<Any>

    @GET("/v1/ezyCart/member/info/{memberNo}")
    fun getMemberInfo(
        @Path(value = "memberNo", encoded = true) memberNo: String): Call<Any>

    //@POST("/v1/ezyCart/cart/item")
    @POST("/v2/ezyCart/cart/item")
    fun addProductToCartApi(@Body dddProductToCartRequest: AddProductToCartRequest): Call<Any>

    @Multipart
    @POST("/v1/ezyCart/ai/cart/item")
    fun addProductToCartWithAiApi(
        @Part("barcode") barcode: RequestBody,
        @Part imageFore: MultipartBody.Part,
        @Part imageBack: MultipartBody.Part,
        @Part("weighscaleWeight") weighScaleWeight: RequestBody,
        @Part("productWeight") productWeight: RequestBody,
    ): Call<Any>

    @Multipart
    // @DELETE("/v1/ezyCart/ai/cart/item")  /v1/ezyCart/ai/cart/item
    @HTTP(method = "DELETE", path = "/v1/ezyCart/ai/cart/item", hasBody = true)
    fun deleteProductAiApi(
        @Part("id") id: RequestBody,
        @Part("barcode") barcode: RequestBody
    ): Call<Any>

    // @DELETE("/ezyCart/cart/item")
    @HTTP(method = "DELETE", path = "/v2/ezyCart/cart/item", hasBody = true)
    fun deleteProductInCartApi(@Body deleteProductInCartRequest: DeleteProductInCartRequest): Call<Any>

    @HTTP(method = "DELETE", path = "/v1/ezyCart/support/cart/{cart_Id}", hasBody = true)
    fun deleteFullProductQuantity(
        @Path(value = "cart_Id", encoded = true) cartId: String,
        @Body deleteProductInCartRequest: DeleteProductInCartRequest
    ): Call<Any>

    @GET("/promotions?")
    fun promotionList(@QueryMap params: Map<String, String>): Call<Any>

    @GET("/v2/ezyCart/product-promo")
    fun getProductPromo(@QueryMap params: Map<String, String>): Call<Any>

    @GET("/promotions/products?")
    fun productListByPromotion(@QueryMap params: Map<String, String>): Call<Any>

    @POST("/ezyCart/cs/ticket")
    fun createNewHelpRequestApi(@Body helpRequest: HelpRequest): Call<Any>

    @POST("/ezyCart/support/cart/verify")
    fun ageVerificationRequestApi(@Body ageVerificationRequest: AgeVerificationRequest): Call<Any>

    @POST("/ezyCart/cs/login")
    fun employeeLoginApi(@Body employeeLoginRequest: EmployeeLoginRequest): Call<Any>


    @PUT("/ezyCart/cs/ticket/{ticketId}")
    fun completeHelpTicket(
        @Path(value = "ticketId", encoded = true) ticketId: String,
        @Body completeHelpTicketRequest: CompleteHelpTicketRequest
    ): Call<Any>

    @GET("/ezyCart/cs/masterhelp")
    @Headers("Content-Type:application/x-www-form-urlencoded; charset=utf-8")
    fun HelpTypeList(): Call<Any>


    @PUT("/v1/ezyCart/support/cart/{cart_Id}")
    fun editProductQuantity(
        @Path(value = "cart_Id", encoded = true) cartId: String,
        @Body editProductRequest: EditProductRequest
    ): Call<Any>

    @GET
    @Headers("Content-Type:application/x-www-form-urlencoded; charset=utf-8")
    fun getMyCartDetails(@Url url: String): Call<Any>


    @POST
    fun cancelPayment(@Url url: String): Call<Any>

    // @POST("/ezyCart/payment")
    //@POST("/v1/ezyCart/ai/payment")
    @POST("/ezyCart/payment")
    fun paymentApi(@Body paymentRequest: PaymentRequest): Call<Any>

    @GET("/cms/zones?")
    @Headers("Content-Type:application/x-www-form-urlencoded; charset=utf-8")
    fun ZonesWithBeaconsDetails(@QueryMap params: Map<String, String>): Call<Any>

    @GET("/cms/config")
    @Headers("Content-Type:application/x-www-form-urlencoded; charset=utf-8")
    fun cmsConfigApi(): Call<Any>

    @POST("/ezyCart/customer/login")
    fun customerLoginApi(@Body customerLoginRequest: CustomerLoginRequest): Call<Any>

    @PUT("/ezyCart/customer/update")
    fun updateCustomerOtpApi(@Body customerLoginRequest: UpdateCustomerOTP): Call<Any>

    @POST("/ezyCart/customer/register")
    fun registerNewCustomerApi(@Body customerRegistrationRequest: CustomerRegistrationRequest): Call<Any>

    @GET("/ezyCart/payment/options")
    @Headers("Content-Type:application/x-www-form-urlencoded; charset=utf-8")
    fun paymentOptions(@QueryMap params: Map<String, String>): Call<Any>


    @POST("/ezyCart/cart/member")
    fun updateMemberDetails(
        @Body updateMemberDetailsRequest: UpdateMemberDetailsRequest
    ): Call<Any>



    @GET("/cms/banners?")
    @Headers("Content-Type:application/x-www-form-urlencoded; charset=utf-8")
    fun getBannersList(@QueryMap params: Map<String, String>): Call<Any>

    @GET("/cms/top-promotions?")
    @Headers("Content-Type:application/x-www-form-urlencoded; charset=utf-8")
    fun getTopPromotionsList(@QueryMap params: Map<String, String>): Call<Any>

    @POST
    fun sendPaymentReceipt(
        @Url url: String,
        @Body sendPaymentReceiptRequest: SendPaymentReceiptRequest
    ): Call<Any>

    @GET
    @Headers("Content-Type:application/x-www-form-urlencoded; charset=utf-8")
    fun getDeviceDetails(@Url url: String): Call<Any>

    @POST("/ezyCart/paymentNotify")
    fun paymentNotifyApi(@Body transactionOutcome: MPOSTransactionOutcome?): Call<Any>

    *//*@GET("/user?")
    fun qrLoginApi(@QueryMap params: Map<String, String>): Call<Any>

    @GET("/ezyList/mylists")
    fun getUserShoppingList(): Call<Any>*//*

    @GET
    fun qrLoginApi(@Url url: String, @QueryMap params: Map<String, String>): Call<Any>

    *//*@GET("/ezyList/mylists")
    fun getUserShoppingList(): Call<Any>*//*

    @GET
    fun getUserShoppingList(@HeaderMap params: Map<String, String>, @Url url: String): Call<Any>

    @PUT("/ezyList/{listId}/items")
    fun getUserListDetails(
        @Path(value = "listId", encoded = true) listId: String
    ): Call<Any>

    @GET("/v1/ezyCart/ai/cart/status")
    @Headers("Content-Type:application/x-www-form-urlencoded; charset=utf-8")
    fun getBackOfficeStatus(): Call<Any>

    @Multipart
    @POST
    @Headers("ContentType: application/multipart/form-data; charset=utf-8")
    fun paymentReview(@Url url: String, @Part finalImage: MultipartBody.Part): Call<Any>

    @POST("/v1/ezyCart/ai/cart/reject")
    fun productRejectByAiApi(@Body request: String): Call<Any>

    @POST("ezyRetail/feedback")
    fun feedBackApi(@Body sendFeedBackRequest: SendFeedBackRequest): Call<Any>

    @GET("/recipes/categories")
    fun getRecipeCategories(): Call<Any>

    @GET("recipes/{categoryId}")
    fun getRecipeList(
        @Path("categoryId") categoryId: Int?,
        @Query("page") page: Int?,
        @Query("pageSize") pageSize: Int?,
    ): Call<Any>

    @GET("/recipes/detail/{recipeId}")
    fun getRecipeDetails(
        @Path("recipeId") recipeId: Int?,
    ): Call<Any>

    @GET("/v1/ezyList/promotions")
    fun getPromotions(
        @Header("Authorization") token: String?
    ): Call<Any>

    @GET("/promotions")
    fun getPromotions(
        @Query("merchantId") merchantId: Int,
        @Query("outletId") outletId: Int,
    ): Call<Any>

    @GET("/promotions/products")
    fun getProductByPromotion(
        @Query("id") Id: Int,
        @Query("merchantId") merchantId: Int,
        @Query("outletId") outletId: Int,
    ): Call<Any>

    @GET
    @Headers("Content-Type:application/x-www-form-urlencoded; charset=utf-8")
    fun getCartStatus(@Url url: String): Call<Any>

    @GET("/promotions/special-promotions")
    fun getSpecialPromotions(
        @QueryMap params: Map<String, String>
    ): Call<Any>

    @POST("/v2/ezyCart/cart/item/promo")
    fun applySpecialPromotionApi(@Body applySpecialPromotionRequest: ApplySpecialPromotionRequest): Call<Any>

    //@GET("/logs/clear-cache")

    @GET("/v1/clear-cache")
    @Headers("Content-Type:application/x-www-form-urlencoded; charset=utf-8")
    fun clearLogsApi(): Call<Any>

    @POST("/script/update-product")
    fun updateProductScriptApi(@Body updateProductScriptRequest: UpdateProductScriptRequest): Call<Any>


    @POST
    fun sendStatusToMonitor(@Url url : String, @Body monitorRequestData:MonitorRequest): Call<Any>

    @POST
    fun sendLogsToCms(@Url url : String, @Body cmsLogRequest:CmsLogRequest): Call<Any>

    @POST
    fun rejectLogs(@Url url : String, @Body requestData:String): Call<Any>

    @POST("/v2/ezyCart/cart/coupon-voucher/{cart_id}")
    fun applyCouponVoucher(
        @Path(value = "cart_id", encoded = true) cartId: String,
        @Body applyCouponVoucherRequest: ApplyCouponVoucherRequest
    ): Call<Any>

    @DELETE("/v2/ezyCart/cart/coupon-voucher/{cart_id}/{voucher_code}")
    fun deleteCouponVoucher(
        @Path(value = "cart_id", encoded = true) cartId: String,
        @Path(value = "voucher_code", encoded = true) voucherCode: String
    ): Call<Any>

    @GET("/advertisement/ad")
    @Headers("Content-Type:application/x-www-form-urlencoded; charset=utf-8")
    fun getAllAdvertisementApi(@Query("merchantId") merchantId: Int,
                               @Query("outletId") outletId: Int,): Call<Any>

    @POST
    fun createNewJwtToken(
        @Url url: String, @Body createJwtTokenRequest:CreateJwtTokenRequest): Call<Any>

    @GET
    fun createPaymentSessionUsingJwtToken(
        @Url url: String): Call<Any>

    @GET("/promotions/active")
    fun getNewPromotionDetails(@QueryMap params: Map<String, String>): Call<Any>

    //@POST("/send-otp") //SMS
    @POST("/send-whatsapp-otp") // Whatsapp
    fun sentOtpApi(@Body sendOtpRequest: SendOtpRequest): Call<Any>

    @POST("/verify-otp")
    fun verifyOtpApi(@Body verifyOtpRequest: VerifyOtpRequest): Call<Any>*/
}