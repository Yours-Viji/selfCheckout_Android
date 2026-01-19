package com.ezycart.data.remote.dto

data class PaymentResponse(
    val order_id: String,
    val qr_code: String,
    val redirect_url: String,
    val reference: String,
    val customerMobileNo:String,
    val customerEmail:String,
     val referenceNo: String,
    val returnUrl: String,

)


